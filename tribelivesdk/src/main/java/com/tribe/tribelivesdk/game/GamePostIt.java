package com.tribe.tribelivesdk.game;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import com.tribe.tribelivesdk.R;
import com.tribe.tribelivesdk.libyuv.LibYuvConverter;
import com.tribe.tribelivesdk.opencv.OpenCVWrapper;
import com.tribe.tribelivesdk.util.BitmapUtils;
import com.tribe.tribelivesdk.util.ByteBuffers;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.tribe.tribelivesdk.webrtc.TribeI420Frame;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by tiago on 23/05/2017.
 */

public class GamePostIt extends Game {

  private OpenCVWrapper openCVWrapper;
  private LibYuvConverter libYuvConverter;
  private Bitmap bitmapLocalPostIt, bitmapRemotePostIt, tempBitmap, canvasBitmap;
  private int[] localPostIt, remotePostIt;
  private byte[] yuvOutLocal, argbOut;
  private ByteBuffer byteBufferYuv;
  private ByteBuffer[] yuvPlanes;
  private int[] yuvStrides;
  private Canvas canvas;

  public GamePostIt(Context context, @GameType String id, String name, int drawableRes) {
    super(context, id, name, drawableRes);
    openCVWrapper = new OpenCVWrapper();
    libYuvConverter = new LibYuvConverter();
  }

  @Override public void apply(Frame frame) {
    openCVWrapper.addPostIt(frame.getData(), frame.getWidth(), frame.getHeight(), localPostIt,
        bitmapLocalPostIt.getWidth(), bitmapLocalPostIt.getHeight(), argbOut);
    libYuvConverter.ARGBToI420(argbOut, frame.getWidth(), frame.getHeight(), yuvOutLocal);
    byteBufferYuv.put(yuvOutLocal);
    byteBufferYuv.flip();
    onLocalFrame.onNext(
        new TribeI420Frame(frame.getWidth(), frame.getHeight(), frame.getRotation(), yuvStrides,
            yuvPlanes));
  }

  @Override public void onFrameSizeChange(Frame frame) {
    tempBitmap = Bitmap.createBitmap(frame.getWidth(), frame.getHeight(), Bitmap.Config.ARGB_8888);
    yuvOutLocal = new byte[frame.getData().length];
    argbOut = new byte[frame.getWidth() * frame.getHeight() * 4];
    yuvStrides = new int[3];
    yuvStrides[0] = frame.getWidth();
    yuvStrides[1] = (frame.getWidth() + 1) / 2;
    yuvStrides[2] = (frame.getWidth() + 1) / 2;
    int chroma_height = (frame.getHeight() + 1) / 2;
    if (byteBufferYuv != null) byteBufferYuv.clear();

    byteBufferYuv = ByteBuffer.allocateDirect(frame.getDataOut().length);
    yuvPlanes = ByteBuffers.slice(byteBufferYuv, yuvStrides[0] * frame.getHeight(),
        yuvStrides[1] * chroma_height, yuvStrides[2] * chroma_height);

    Bitmap postIt = BitmapUtils.generateNewPostIt(context, "?",
        context.getResources().getDimensionPixelSize(R.dimen.textsize_post_it), Color.WHITE,
        R.drawable.bg_post_it);
    localPostIt = new int[postIt.getWidth() * postIt.getHeight()];
    remotePostIt = new int[postIt.getWidth() * postIt.getHeight()];

    Matrix localMatrix = new Matrix();
    if (frame.isFrontCamera()) {
      localMatrix.postScale(-1, 1);
    }

    localMatrix.postRotate(-frame.getRotation());

    bitmapLocalPostIt =
        Bitmap.createBitmap(postIt, 0, 0, postIt.getWidth(), postIt.getHeight(), localMatrix, true);
    bitmapLocalPostIt.getPixels(localPostIt, 0, postIt.getWidth(), 0, 0, postIt.getWidth(),
        postIt.getHeight());
    //bitmapLocalPostIt = BitmapUtils.fromByteArray(localPostIt);
    //savePNGImageToGallery(bitmapLocalPostIt, context, "lol.png");

    Matrix remoteMatrix = new Matrix();
    remoteMatrix.postRotate(-frame.getRotation());
    bitmapRemotePostIt =
        Bitmap.createBitmap(postIt, 0, 0, postIt.getWidth(), postIt.getHeight(), remoteMatrix,
            true);
    bitmapRemotePostIt.getPixels(remotePostIt, 0, postIt.getWidth(), 0, 0, postIt.getWidth(),
        postIt.getHeight());

    canvasBitmap =
        Bitmap.createBitmap(frame.getWidth(), frame.getHeight(), Bitmap.Config.ARGB_8888);
    canvas = new Canvas(canvasBitmap);
  }

  protected void savePNGImageToGallery(Bitmap bmp, Context context, String baseFilename) {
    try {
      // Get the file path to the SD card.
      File dir = Environment.getExternalStoragePublicDirectory("tribeapp");
      if (!dir.exists()) dir.mkdirs();

      String baseFolder = dir.getAbsolutePath() + "/";
      File file = new File(baseFolder + baseFilename);
      if (file.exists()) file.delete();
      Log.i("Save", "Saving the processed image to file [" + file.getAbsolutePath() + "]");

      // Open the file.
      OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
      // Save the image file as PNG.
      bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
      out.flush();    // Make sure it is saved to file soon, because we are about to add it to the Gallery.
      out.close();

      // Add the PNG file to the Android Gallery.
      ContentValues image = new ContentValues();
      image.put(MediaStore.Images.Media.TITLE, baseFilename);
      image.put(MediaStore.Images.Media.DISPLAY_NAME, baseFilename);
      image.put(MediaStore.Images.Media.DESCRIPTION, "Processed by the Cartoonifier App");
      image.put(MediaStore.Images.Media.DATE_TAKEN,
          System.currentTimeMillis()); // Milliseconds since 1970 UTC.
      image.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
      image.put(MediaStore.Images.Media.ORIENTATION, 0);
      image.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
      Uri result =
          context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, image);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
