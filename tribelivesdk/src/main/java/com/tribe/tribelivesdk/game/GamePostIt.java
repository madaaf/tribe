package com.tribe.tribelivesdk.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import com.google.android.gms.vision.face.Face;
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
import java.util.Random;

/**
 * Created by tiago on 23/05/2017.
 */

public class GamePostIt extends Game {

  private int WIDTH_FACE_REFERENCE = 500;
  private float SCALE_POST_IT = 0.5f;

  private List<String> nameList;
  private String currentPostItName = "?";
  private OpenCVWrapper openCVWrapper;
  private LibYuvConverter libYuvConverter;
  private Frame localFrame, remoteFrame;
  private PointF pointOG, pointLocal, pointRemote;
  private Bitmap bitmapOGLocalPostIt, bitmapOGRemotePostIt, bitmapLocalPostIt, bitmapRemotePostIt;
  private Rect frameRect;
  private Matrix transform;
  private int[] localPostIt, remotePostIt;
  private byte[] yuvOutLocal, yuvOutRemote, argbOutLocal, argbInRemote, argbOutRemote;
  private ByteBuffer byteBufferYuv;
  private ByteBuffer[] yuvPlanes;
  private int[] yuvStrides;
  private VisionAPIManager visionAPIManager;
  private float newFaceWidth, currentPostItScale = SCALE_POST_IT;

  public GamePostIt(Context context, @GameType String id, String name, int drawableRes,
      boolean isAvailable) {
    super(context, id, name, drawableRes, isAvailable);
    openCVWrapper = new OpenCVWrapper();
    libYuvConverter = new LibYuvConverter();
    visionAPIManager = VisionAPIManager.getInstance(context);
    nameList = new ArrayList<>();
    pointLocal = new PointF();
    pointRemote = new PointF();

    initSubscriptions();
  }

  private void initSubscriptions() {
    subscriptions.add(visionAPIManager.onFaceWidthChange().subscribe(faceWidth -> {
      newFaceWidth = faceWidth;
      refactorPostItScales();
    }));
  }

  @Override public void apply(Frame originalFrame) {
    localFrame = originalFrame;
    remoteFrame = originalFrame.copy(argbInRemote);

    pointOG = null;

    if (visionAPIManager.getFace() != null) {
      pointOG = findXYForPostIt(visionAPIManager.getFace());
    } else if (!visionAPIManager.isFaceTrackerEnabled()) {
      pointOG = new PointF(originalFrame.getWidth() >> 1, originalFrame.getHeight() >> 1);
    }

    if (pointOG != null) {
      if (visionAPIManager.isFaceTrackerEnabled()) {
        pointLocal = pointOG;
        pointLocal = computePointLocal(pointLocal, localFrame, bitmapOGLocalPostIt);
      } else {
        pointLocal = rotate(pointLocal, localFrame, originalFrame.getRotation());
        currentPostItScale = 0.5f;
      }

      openCVWrapper.addPostIt(localFrame.getData(), localFrame.getWidth(), localFrame.getHeight(),
          localPostIt, bitmapLocalPostIt.getWidth(), bitmapLocalPostIt.getHeight(),
          currentPostItScale, pointLocal.x, pointLocal.y, argbOutLocal);

      if (visionAPIManager.isFaceTrackerEnabled()) {
        pointRemote = pointOG;
        pointRemote = computePointRemote(pointRemote, remoteFrame, bitmapOGRemotePostIt);
      } else {
        pointRemote = pointOG;
        currentPostItScale = 0.5f;
      }

      openCVWrapper.addPostIt(remoteFrame.getData(), remoteFrame.getWidth(),
          remoteFrame.getHeight(), remotePostIt, bitmapRemotePostIt.getWidth(),
          bitmapRemotePostIt.getHeight(), currentPostItScale, pointRemote.x, pointRemote.y,
          argbOutRemote);
    } else {
      System.arraycopy(localFrame.getData(), 0, argbOutLocal, 0, localFrame.getData().length);
      System.arraycopy(remoteFrame.getData(), 0, argbOutRemote, 0, remoteFrame.getData().length);
    }

    libYuvConverter.ARGBToI420(argbOutLocal, localFrame.getWidth(), localFrame.getHeight(),
        yuvOutLocal);
    byteBufferYuv.put(yuvOutLocal);
    byteBufferYuv.flip();

    onLocalFrame.onNext(
        new TribeI420Frame(localFrame.getWidth(), localFrame.getHeight(), localFrame.getRotation(),
            yuvStrides, yuvPlanes));

    libYuvConverter.ARGBToYUV(argbOutRemote, remoteFrame.getWidth(), remoteFrame.getHeight(),
        yuvOutRemote);
    remoteFrame.setDataOut(yuvOutRemote);
    onRemoteFrame.onNext(remoteFrame);
  }

  @Override public void onFrameSizeChange(Frame frame) {
    subscriptions.clear();
    initSubscriptions();

    yuvOutLocal = new byte[frame.getData().length];
    yuvOutRemote = new byte[frame.getData().length];
    argbOutLocal = new byte[frame.getWidth() * frame.getHeight() * 4];
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
    Matrix localMatrix = new Matrix();
    if (frame.isFrontCamera()) {
      localMatrix.postScale(-1, 1);
    }
    localMatrix.postRotate(-frame.getRotation());
    bitmapLocalPostIt =
        Bitmap.createBitmap(bitmapOGLocalPostIt, 0, 0, bitmapOGLocalPostIt.getWidth(),
            bitmapOGLocalPostIt.getHeight(), localMatrix, true);
    localPostIt = new int[bitmapLocalPostIt.getWidth() * bitmapLocalPostIt.getHeight()];
    bitmapLocalPostIt.getPixels(localPostIt, 0, bitmapLocalPostIt.getWidth(), 0, 0,
        bitmapLocalPostIt.getWidth(), bitmapLocalPostIt.getHeight());

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

  public PointF findXYForPostIt(Face face) {
    PointF leftEye = visionAPIManager.getLeftEye();
    PointF rightEye = visionAPIManager.getRightEye();

    if (leftEye != null && rightEye != null) {
      return new PointF((leftEye.x + rightEye.x) / 2, (leftEye.y + rightEye.y) / 2);
    }

    return new PointF(face.getPosition().x + face.getWidth() / 2,
        face.getPosition().y + face.getHeight() / 2);
  }

  private PointF computePointLocal(PointF pointF, Frame frame, Bitmap bitmapOverlay) {
    pointF = rotate(pointF, frame, frame.getRotation());

    if (frame.getRotation() == 270) {
      pointF.x = pointF.x - ((bitmapOverlay.getWidth() >> 1) - 100) * currentPostItScale;
      pointF.y = pointF.y - (bitmapOverlay.getHeight() >> 1) * currentPostItScale;
    } else if (frame.getRotation() == 90) {
      pointF.x = pointF.x - ((bitmapOverlay.getWidth() >> 1) + 100) * currentPostItScale;
      pointF.y = pointF.y - ((bitmapOverlay.getHeight() >> 1) * currentPostItScale);
    } else if (frame.getRotation() == 0) {
      pointF.x = pointF.x - ((bitmapOverlay.getWidth() >> 1)) * currentPostItScale;
      pointF.y = pointF.y - ((bitmapOverlay.getHeight() >> 1) + 100) * currentPostItScale;
    } else if (frame.getRotation() == 180) {
      pointF.x = pointF.x - ((bitmapOverlay.getWidth() >> 1)) * currentPostItScale;
      pointF.y = pointF.y - ((bitmapOverlay.getHeight() >> 1) - 100) * currentPostItScale;
    }

    return pointF;
  }

  private PointF computePointRemote(PointF pointF, Frame frame, Bitmap bitmapOverlay) {
    pointF = rotate(pointF, frame, frame.getRotation());

    if (frame.getRotation() == 270) {
      pointF.x = pointF.x - ((bitmapOverlay.getHeight() >> 1) - 100) * currentPostItScale;
      pointF.y = pointF.y - ((bitmapOverlay.getWidth() >> 1) * currentPostItScale);
    } else if (frame.getRotation() == 90) {
      pointF.x = pointF.x - ((bitmapOverlay.getHeight() >> 1) + 100) * currentPostItScale;
      pointF.y = pointF.y - ((bitmapOverlay.getWidth() >> 1) * currentPostItScale);
    } else if (frame.getRotation() == 0) {
      pointF.x = pointF.x - ((bitmapOverlay.getWidth() >> 1)) * currentPostItScale;
      pointF.y = pointF.y - ((bitmapOverlay.getHeight() >> 1) + 100) * currentPostItScale;
    } else if (frame.getRotation() == 180) {
      pointF.x = pointF.x - ((bitmapOverlay.getWidth() >> 1)) * currentPostItScale;
      pointF.y = pointF.y + ((bitmapOverlay.getHeight() >> 1) * currentPostItScale);
    }

    return pointF;
  }

  /**
   * Add the Rotation to our Transform matrix.
   *
   * A new point, with the rotated coordinates will be returned
   */
  public PointF rotate(PointF pointF, Frame frame, int degrees) {
    PointF newPoint = new PointF();

    if (degrees == 90) {
      newPoint.x = pointF.y;
      newPoint.y = frame.getHeight() - pointF.x;
    } else if (degrees == 270) {
      newPoint.x = frame.getWidth() - pointF.y;
      newPoint.y = pointF.x;
    } else if (degrees == 0) {
      newPoint.x = pointF.x;
      newPoint.y = pointF.y;
    } else if (degrees == 180) {
      newPoint.x = frame.getWidth() - pointF.x;
      newPoint.y = frame.getHeight() - pointF.y;
    }

    return newPoint;
  }

  public void setNameList(List<String> nameList) {
    this.nameList.clear();
    this.nameList.addAll(nameList);
    generateNewName();
  }

  public void generateNewName() {
    if (nameList == null || nameList.size() == 0) return;
    currentPostItName = nameList.get(new Random().nextInt(nameList.size()));
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
}
