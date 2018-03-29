package com.tribe.tribelivesdk.facetracking;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PointF;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;
import com.tribe.tribelivesdk.back.FrameExecutor;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 05/30/2017.
 */

public class VisionAPIManager {

  private static final float THRESHOLD_FACE_WIDTH = 20;
  private static final float EYE_CLOSED_THRESHOLD = 0.5f;
  private static final float SMILE_THRESHOLD = 0.4f;

  private static VisionAPIManager instance;

  public static VisionAPIManager getInstance(Context context) {
    if (instance == null) {
      instance = new VisionAPIManager(context);
    }

    return instance;
  }

  // VARIABLES
  private Context context;
  private GameManager gameManager;
  private FrameExecutor frameExecutor;
  private boolean firstFrame = true, isReleased = false;
  private long t0, timeToDetect;
  private FaceDetector faceDetector;
  private boolean isFaceTrackerEnabled = true;
  private Face face;
  private PointF leftEye, rightEye;
  private float previousFaceWidth, newFaceWidth;
  private Frame lastFrame = null;
  private com.google.android.gms.vision.Frame inputFrame;
  private Map<Integer, PointF> previousProportions = new HashMap<>();
  private boolean rightEyeOpen = false, leftEyeOpen = false, smiling = false;
  private boolean previousLeftOpen = true, previousRightOpen = true, previousSmiling = true;
  private float eulerY;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Float> onFaceWidthChange = PublishSubject.create();
  private PublishSubject<Frame> onComputeFaceDone = PublishSubject.create();

  public VisionAPIManager(Context context) {
    this.context = context;
    this.frameExecutor = new FrameExecutor();
    this.gameManager = new GameManager(context);

    initFaceTracker(true);
  }

  /////////////
  // PRIVATE //
  /////////////

  private void initFaceTracker(boolean isFrontFacing) {
    if (faceDetector != null) {
      isReleased = true;
      faceDetector.release();
    }

    faceDetector = new FaceDetector.Builder(context).setTrackingEnabled(true)
        .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
        //.setMode(gameManager.getCurrentGame() != null &&
        //    gameManager.getCurrentGame().getId().equals(Game.GAME_COOL_CAMS)
        //    ? FaceDetector.ACCURATE_MODE : FaceDetector.FAST_MODE)
        .setMode(FaceDetector.ACCURATE_MODE)
        .setProminentFaceOnly(isFrontFacing)
        .setMinFaceSize(isFrontFacing ? 0.35f : 0.15f)
        .build();

    isReleased = false;

    //Detector.Processor<Face> processor;
    //if (isFrontFacing) {
    //  Tracker<Face> tracker = new TribeFaceTracker();
    //  processor = new LargestFaceFocusingProcessor.Builder(faceDetector, tracker).build();
    //} else {
    //  MultiProcessor.Factory<Face> factory = face -> new TribeFaceTracker();
    //  processor = new MultiProcessor.Builder<>(factory).build();
    //}

    faceDetector.setProcessor(
        new LargestFaceFocusingProcessor.Builder(faceDetector, new TribeFaceTracker()).build());

    //faceDetector.setProcessor(processor);

    if (!faceDetector.isOperational()) {
      isFaceTrackerEnabled = false;
    }
  }

  private class TribeFaceTrackerFactory implements MultiProcessor.Factory<Face> {
    @Override public Tracker<Face> create(Face face) {
      return new TribeFaceTracker();
    }
  }

  private class TribeFaceTracker extends Tracker<Face> {

    TribeFaceTracker() {

    }

    @Override public void onNewItem(int faceId, Face face) {
      Timber.d("onNewItem : " + face);
    }

    @Override public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
      //Timber.d("onUpdate : " + face);
      VisionAPIManager.this.face = face;
      computeFace(face);
    }

    @Override public void onMissing(FaceDetector.Detections<Face> detectionResults) {
      //Timber.d("onMissing : " + detectionResults);
      VisionAPIManager.this.face = null;
      VisionAPIManager.this.rightEye = VisionAPIManager.this.leftEye = null;
      onComputeFaceDone.onNext(lastFrame);
    }

    @Override public void onDone() {
      //Timber.d("onDone");
      newFaceWidth = previousFaceWidth = 0;
    }
  }

  private void computeFace(Face face) {
    if (face != null) {
      updatePreviousProportions(face);

      leftEye = getLandmarkPosition(face, Landmark.LEFT_EYE);
      rightEye = getLandmarkPosition(face, Landmark.RIGHT_EYE);

      previousFaceWidth = newFaceWidth;
      newFaceWidth = face.getWidth();

      if (faceWidthChanged()) onFaceWidthChange.onNext(newFaceWidth);

      float leftOpenScore = face.getIsLeftEyeOpenProbability();
      if (leftOpenScore == Face.UNCOMPUTED_PROBABILITY) {
        leftEyeOpen = previousLeftOpen;
      } else {
        leftEyeOpen = (leftOpenScore > EYE_CLOSED_THRESHOLD);
        previousLeftOpen = leftEyeOpen;
      }

      float rightOpenScore = face.getIsRightEyeOpenProbability();
      if (rightOpenScore == Face.UNCOMPUTED_PROBABILITY) {
        rightEyeOpen = previousRightOpen;
      } else {
        rightEyeOpen = (rightOpenScore > EYE_CLOSED_THRESHOLD);
        previousRightOpen = rightEyeOpen;
      }

      float smilingScore = face.getIsSmilingProbability();
      if (smilingScore == Face.UNCOMPUTED_PROBABILITY) {
        smiling = previousSmiling;
      } else {
        smiling = (smilingScore > SMILE_THRESHOLD);
        previousSmiling = smiling;
      }

      eulerY = face.getEulerY();

      onComputeFaceDone.onNext(lastFrame);
    }
  }

  private boolean faceWidthChanged() {
    return Math.abs(previousFaceWidth - newFaceWidth) > THRESHOLD_FACE_WIDTH;
  }

  private void updatePreviousProportions(Face face) {
    for (Landmark landmark : face.getLandmarks()) {
      PointF position = landmark.getPosition();
      float xProp = (position.x - face.getPosition().x) / face.getWidth();
      float yProp = (position.y - face.getPosition().y) / face.getHeight();
      previousProportions.put(landmark.getType(), new PointF(xProp, yProp));
    }
  }

  private PointF getLandmarkPosition(Face face, int landmarkId) {
    for (Landmark landmark : face.getLandmarks()) {
      if (landmark.getType() == landmarkId) {
        return landmark.getPosition();
      }
    }

    PointF prop = previousProportions.get(landmarkId);
    if (prop == null) {
      return null;
    }

    float x = face.getPosition().x + (prop.x * face.getWidth());
    float y = face.getPosition().y + (prop.y * face.getHeight());
    return new PointF(x, y);
  }

  ////////////////
  ////  PUBLIC  //
  ////////////////

  public void initFrameSizeChangeObs(Observable<Frame> obs) {
    subscriptions.add(
        obs.onBackpressureDrop().subscribe(frame -> initFaceTracker(frame.isFrontCamera())));
  }

  public void initFrameSubscription(Observable<Frame> obs) {
    subscriptions.add(obs.onBackpressureDrop()
        .observeOn(Schedulers.computation())
        .map(frame -> {
          if (firstFrame) {
            firstFrame = false;
            initFaceTracker(frame.isFrontCamera());
          }

          t0 = System.currentTimeMillis();

          return frame;
        })
        .filter(frame1 -> (t0 - timeToDetect >= 250) && isFaceTrackerEnabled && !isReleased)
        .flatMap(frame -> Observable.just(frame)
            .observeOn(Schedulers.from(frameExecutor))
            .map(frame1 -> {
              lastFrame = frame1;
              inputFrame = new com.google.android.gms.vision.Frame.Builder().setImageData(
                  ByteBuffer.wrap(frame.getData()), frame.getWidth(), frame.getHeight(),
                  ImageFormat.NV21)
                  .setId((int) frame.getTimestamp())
                  .setTimestampMillis(frame.getTimestamp())
                  .setRotation(frame.getRotation() / 90)
                  .build();

              try {
                faceDetector.receiveFrame(inputFrame);
              } catch (IllegalStateException ex) {
                Timber.d("FaceDetector has no processor right now");
              }

              timeToDetect = System.currentTimeMillis();

              return frame;
            }), 1)
        .subscribe());
  }

  public boolean isFaceTrackerEnabled() {
    return isFaceTrackerEnabled;
  }

  public Face getFace() {
    return face;
  }

  public PointF getLeftEye() {
    return leftEye;
  }

  public PointF getRightEye() {
    return rightEye;
  }

  public boolean isSmiling() {
    return smiling;
  }

  public boolean isLeftEyeOpen() {
    return leftEyeOpen;
  }

  public boolean isRightEyeOpen() {
    return rightEyeOpen;
  }

  public float getEulerY() {
    return eulerY;
  }

  public PointF findXYMiddleEye() {
    if (face == null || (leftEye == null && rightEye == null)) return null;

    if (leftEye != null && rightEye != null) {
      return new PointF((leftEye.x + rightEye.x) / 2, (leftEye.y + rightEye.y) / 2);
    }

    return null;
  }

  public void stopCapture() {
    firstFrame = true;
    lastFrame = null;
    dispose();
  }

  public void dispose() {
    subscriptions.clear();
    isReleased = true;
    if (faceDetector != null) faceDetector.release();
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Float> onFaceWidthChange() {
    return onFaceWidthChange;
  }

  public Observable<Frame> onComputeFaceDone() {
    return onComputeFaceDone;
  }
}
