package com.tribe.tribelivesdk.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;
import com.tribe.tribelivesdk.R;
import com.tribe.tribelivesdk.facetracking.VisionAPIManager;
import com.tribe.tribelivesdk.libyuv.LibYuvConverter;
import com.tribe.tribelivesdk.opencv.OpenCVWrapper;
import com.tribe.tribelivesdk.util.BitmapUtils;
import com.tribe.tribelivesdk.util.ByteBuffers;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.tribe.tribelivesdk.webrtc.TribeI420Frame;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 23/05/2017.
 */

public class GamePostIt extends Game {

  private List<String> nameList;
  private OpenCVWrapper openCVWrapper;
  private LibYuvConverter libYuvConverter;
  private Bitmap bitmapLocalPostIt, bitmapRemotePostIt, tempBitmap, canvasBitmap;
  private int[] localPostIt, remotePostIt;
  private byte[] yuvOutLocal, yuvOutRemote, argbOutLocal, argbOutRemote;
  private ByteBuffer byteBufferYuv;
  private ByteBuffer[] yuvPlanes;
  private int[] yuvStrides;
  private Canvas canvas;
  private VisionAPIManager visionAPIManager;

  public GamePostIt(Context context, @GameType String id, String name, int drawableRes) {
    super(context, id, name, drawableRes);
    openCVWrapper = new OpenCVWrapper();
    libYuvConverter = new LibYuvConverter();
    visionAPIManager = VisionAPIManager.getInstance(context);
    nameList = new ArrayList<>();
  }

  @Override public void apply(Frame originalFrame) {
    Frame localFrame = originalFrame;
    Frame remoteFrame = originalFrame.copy();

    if (visionAPIManager.getFace() != null) {
      PointF point = findXYForPostIt(visionAPIManager.getFace());
      openCVWrapper.addPostIt(localFrame.getData(), localFrame.getWidth(), localFrame.getHeight(),
          localPostIt, bitmapLocalPostIt.getWidth(), bitmapLocalPostIt.getHeight(), point.x,
          point.y, argbOutRemote);
    } else {
      System.arraycopy(localFrame.getData(), 0, argbOutRemote, 0, localFrame.getData().length);
    }

    libYuvConverter.ARGBToI420(argbOutRemote, localFrame.getWidth(), localFrame.getHeight(),
        yuvOutLocal);
    byteBufferYuv.put(yuvOutLocal);
    byteBufferYuv.flip();

    onLocalFrame.onNext(
        new TribeI420Frame(localFrame.getWidth(), localFrame.getHeight(), localFrame.getRotation(),
            yuvStrides, yuvPlanes));

    libYuvConverter.ARGBToYUV(remoteFrame.getData(), remoteFrame.getWidth(),
        remoteFrame.getHeight(), yuvOutRemote);
    remoteFrame.setDataOut(yuvOutRemote);
    onRemoteFrame.onNext(remoteFrame);
  }

  @Override public void onFrameSizeChange(Frame frame) {
    tempBitmap = Bitmap.createBitmap(frame.getWidth(), frame.getHeight(), Bitmap.Config.ARGB_8888);
    yuvOutLocal = new byte[frame.getData().length];
    yuvOutRemote = new byte[frame.getData().length];
    argbOutLocal = new byte[frame.getWidth() * frame.getHeight() * 4];
    argbOutRemote = new byte[frame.getWidth() * frame.getHeight() * 4];
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

  public PointF findXYForPostIt(Face face) {
    Landmark leftEye = null, rightEye = null;

    if (face.getLandmarks() != null) {
      for (Landmark landmark : face.getLandmarks()) {
        if (landmark.getType() == Landmark.LEFT_EYE) {
          leftEye = landmark;
        } else if (landmark.getType() == Landmark.RIGHT_EYE) rightEye = landmark;
      }

      if (leftEye != null && rightEye != null) {
        PointF pLeftEye = leftEye.getPosition(), pRightEye = rightEye.getPosition();
        return new PointF((pLeftEye.x + pRightEye.x) / 2, (pLeftEye.y + pRightEye.y) / 2);
      }
    }

    return new PointF(face.getPosition().x + face.getWidth() / 2,
        face.getPosition().y + face.getHeight() / 2);
  }

  public void setNameList(List<String> nameList) {
    this.nameList.clear();
    this.nameList.addAll(nameList);
  }

  public boolean hasNames() {
    return nameList != null && nameList.size() > 0;
  }

  public List<String> getNameList() {
    return nameList;
  }
}
