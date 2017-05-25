package com.tribe.tribelivesdk.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import com.tribe.tribelivesdk.R;
import com.tribe.tribelivesdk.util.BitmapUtils;
import com.tribe.tribelivesdk.util.ByteBuffers;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.nio.ByteBuffer;

/**
 * Created by tiago on 23/05/2017.
 */

public class GamePostIt extends Game {

  private Bitmap bitmapLocalPostIt, bitmapRemotePostIt, tempBitmap, canvasBitmap;
  private byte[] yuvOutLocal;
  private ByteBuffer byteBufferYuv;
  private ByteBuffer[] yuvPlanes;
  private int[] yuvStrides;
  private Canvas canvas;

  public GamePostIt(Context context, @GameType String id, String name, int drawableRes) {
    super(context, id, name, drawableRes);
  }

  @Override public void apply(Frame frame) {
    frame.setDataOut(yuvOutLocal);

  }

  @Override public void onFrameSizeChange(Frame frame) {
    tempBitmap = Bitmap.createBitmap(frame.getWidth(), frame.getHeight(), Bitmap.Config.ARGB_8888);
    yuvOutLocal = new byte[frame.getData().length];
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
    Matrix localMatrix = new Matrix();
    localMatrix.postScale(-1, 1);
    localMatrix.postRotate(-frame.getRotation());
    bitmapLocalPostIt =
        Bitmap.createBitmap(postIt, 0, 0, postIt.getWidth(), postIt.getHeight(), localMatrix, true);

    Matrix remoteMatrix = new Matrix();
    remoteMatrix.postRotate(-frame.getRotation());
    bitmapRemotePostIt =
        Bitmap.createBitmap(postIt, 0, 0, postIt.getWidth(), postIt.getHeight(), localMatrix, true);

    canvasBitmap =
        Bitmap.createBitmap(frame.getWidth(), frame.getHeight(), Bitmap.Config.ARGB_8888);
    canvas = new Canvas(canvasBitmap);
  }
}
