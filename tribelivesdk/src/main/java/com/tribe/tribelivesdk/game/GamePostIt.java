package com.tribe.tribelivesdk.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import com.tribe.tribelivesdk.R;
import com.tribe.tribelivesdk.facetracking.UlseeManager;
import com.tribe.tribelivesdk.libyuv.LibYuvConverter;
import com.tribe.tribelivesdk.opencv.OpenCVWrapper;
import com.tribe.tribelivesdk.util.BitmapUtils;
import com.tribe.tribelivesdk.util.ByteBuffers;
import com.tribe.tribelivesdk.view.opengl.filter.FilterManager;
import com.tribe.tribelivesdk.view.opengl.filter.mask.HeadsUpMaskFilter;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by tiago on 23/05/2017.
 */

public class GamePostIt extends Game {

  private int WIDTH_FACE_REFERENCE = 500;
  private float SCALE_POST_IT = 0.5f;

  private List<String> nameList;
  private String currentPostItName = "?";
  private boolean shouldGenerateNewName = false;
  private UlseeManager ulseeManager;
  private OpenCVWrapper openCVWrapper;
  private LibYuvConverter libYuvConverter;
  private FilterManager filterManager;
  private Frame remoteFrame;
  private PointF pointRemote;
  private Bitmap bitmapOGLocalPostIt, bitmapOGRemotePostIt, bitmapLocalPostIt, bitmapRemotePostIt;
  private Rect frameRect;
  private Matrix transform;
  private int[] localPostIt, remotePostIt;
  private byte[] yuvOutRemote, argbInRemote, argbOutRemote;
  private ByteBuffer byteBufferYuv;
  private ByteBuffer[] yuvPlanes;
  private int[] yuvStrides;
  private float[] shape;
  private float newFaceWidth, currentPostItScale = SCALE_POST_IT;

  public GamePostIt(Context context, @GameType String id, String name, int drawableRes) {
    super(context, id, name, drawableRes);
    ulseeManager = UlseeManager.getInstance(context);
    openCVWrapper = OpenCVWrapper.getInstance();
    libYuvConverter = LibYuvConverter.getInstance();
    filterManager = FilterManager.getInstance(context);
    nameList = new ArrayList<>();

    initSubscriptions();
  }

  private void initSubscriptions() {
    refactorPostItScales();
  }

  @Override public void apply(Frame originalFrame) {
    remoteFrame = originalFrame.copy(argbInRemote);

    float[][] shape = ulseeManager.getShape();

    for (int i = 0; i < UlseeManager.MAX_TRACKER; i++) {
      if (shape[i] != null) this.shape = Arrays.copyOf(shape[i], shape[i].length);
    }

    float realX, realY;

    if (this.shape == null) {
      realX = originalFrame.getWidth() >> 1;
      realY = originalFrame.getHeight() >> 1;
    } else {
      realX = this.shape[2 * 91];
      realY = this.shape[2 * 91 + 1];
      currentPostItScale = 0.5f;
    }

    pointRemote = new PointF(realX, realY);
    pointRemote = computePointRemote(pointRemote, remoteFrame, bitmapRemotePostIt);

    //Timber.d("Real X : " + realX);
    //Timber.d("Real Y : " + realY);

    openCVWrapper.addPostIt(remoteFrame.getData(), remoteFrame.getWidth(), remoteFrame.getHeight(),
        remotePostIt, bitmapRemotePostIt.getWidth(), bitmapRemotePostIt.getHeight(),
        currentPostItScale, pointRemote.x, pointRemote.y, argbOutRemote);

    libYuvConverter.ARGBToYUV(argbOutRemote, remoteFrame.getWidth(), remoteFrame.getHeight(),
        yuvOutRemote);
    remoteFrame.setDataOut(yuvOutRemote);
    onRemoteFrame.onNext(remoteFrame);
  }

  @Override public void onFrameSizeChange(Frame frame) {
    shouldGenerateNewName = false;
    subscriptions.clear();
    initSubscriptions();

    yuvOutRemote = new byte[frame.getData().length];
    argbInRemote = new byte[frame.getWidth() * frame.getHeight() * 4];
    argbOutRemote = new byte[frame.getWidth() * frame.getHeight() * 4];
    yuvStrides = new int[3];
    yuvStrides[0] = frame.getWidth();
    yuvStrides[1] = (frame.getWidth() + 1) / 2;
    yuvStrides[2] = (frame.getWidth() + 1) / 2;
    frameRect = new Rect(0, 0, frame.getWidth(), frame.getHeight());
    transform = new Matrix();
    int chroma_height = (frame.getHeight() + 1) / 2;
    if (byteBufferYuv != null) byteBufferYuv.clear();

    byteBufferYuv = ByteBuffer.allocateDirect(frame.getDataOut().length);
    yuvPlanes = ByteBuffers.slice(byteBufferYuv, yuvStrides[0] * frame.getHeight(),
        yuvStrides[1] * chroma_height, yuvStrides[2] * chroma_height);

    // LOCAL POST-IT
    bitmapOGLocalPostIt = BitmapUtils.generateNewPostIt(context, "?",
        context.getResources().getDimensionPixelSize(R.dimen.textsize_post_it), Color.WHITE,
        R.drawable.bg_post_it);
    bitmapLocalPostIt =
        Bitmap.createBitmap(bitmapOGLocalPostIt, 0, 0, bitmapOGLocalPostIt.getWidth(),
            bitmapOGLocalPostIt.getHeight());
    localPostIt = new int[bitmapLocalPostIt.getWidth() * bitmapLocalPostIt.getHeight()];
    bitmapLocalPostIt.getPixels(localPostIt, 0, bitmapLocalPostIt.getWidth(), 0, 0,
        bitmapLocalPostIt.getWidth(), bitmapLocalPostIt.getHeight());

    new Thread(() -> savePNGImageToGallery(bitmapLocalPostIt,
        filterManager.getMaskAndGlassesPath() + HeadsUpMaskFilter.HEADS_UP_FILE)).start();

    // REMOTE POST-IT
    bitmapOGRemotePostIt = BitmapUtils.generateNewPostIt(context, currentPostItName,
        context.getResources().getDimensionPixelSize(R.dimen.textsize_post_it), Color.WHITE,
        R.drawable.bg_post_it);
    Matrix remoteMatrix = new Matrix();
    remoteMatrix.postRotate(-frame.getRotation());
    bitmapRemotePostIt =
        Bitmap.createBitmap(bitmapOGRemotePostIt, 0, 0, bitmapOGRemotePostIt.getWidth(),
            bitmapOGRemotePostIt.getHeight(), remoteMatrix, true);
    remotePostIt = new int[bitmapRemotePostIt.getWidth() * bitmapRemotePostIt.getHeight()];
    bitmapRemotePostIt.getPixels(remotePostIt, 0, bitmapRemotePostIt.getWidth(), 0, 0,
        bitmapRemotePostIt.getWidth(), bitmapRemotePostIt.getHeight());
  }

  public void setNameList(List<String> nameList) {
    this.nameList.clear();
    this.nameList.addAll(nameList);
    generateNewName();
  }

  public void generateNewName() {
    if (nameList == null || nameList.size() == 0) return;
    currentPostItName = nameList.get(new Random().nextInt(nameList.size()));
    shouldGenerateNewName = true;
  }

  public boolean shouldGenerateNewName() {
    return shouldGenerateNewName;
  }

  public boolean hasNames() {
    return nameList != null && nameList.size() > 0;
  }

  public List<String> getNameList() {
    return nameList;
  }

  private void refactorPostItScales() {
    currentPostItScale = Math.max(newFaceWidth / WIDTH_FACE_REFERENCE, SCALE_POST_IT);
  }

  private void savePNGImageToGallery(Bitmap bmp, String path) {
    try {
      File file = new File(path);

      if (file.exists()) file.delete();

      OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
      bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
      out.flush();
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private PointF computePointRemote(PointF pointF, Frame frame, Bitmap bitmapOverlay) {
    pointF.x = pointF.x - ((bitmapOverlay.getWidth() >> 1) * currentPostItScale);
    pointF.y = pointF.y - ((bitmapOverlay.getHeight() >> 1) * currentPostItScale);
    return pointF;
  }
}
