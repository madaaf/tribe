package com.tribe.tribelivesdk.facetracking;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PointF;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.tribe.tribelivesdk.back.FrameExecutor;
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

  private static VisionAPIManager instance;

  public static VisionAPIManager getInstance(Context context) {
    if (instance == null) {
      instance = new VisionAPIManager(context);
    }

    return instance;
  }

  // VARIABLES
  private Context context;
  private FrameExecutor frameExecutor;
  private boolean firstFrame = true;
  private long t0, timeToDetect;
  private FaceDetector faceDetector;
  private Face face;
  private PointF leftEye, rightEye;
  private float previousFaceWidth, newFaceWidth;
  private com.google.android.gms.vision.Frame inputFrame;
  private Map<Integer, PointF> previousProportions = new HashMap<>();

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Float> onFaceWidthChange = PublishSubject.create();

  public VisionAPIManager(Context context) {
    this.context = context;
    this.frameExecutor = new FrameExecutor();

    initFaceTracker(true);
  }

  /////////////
  // PRIVATE //
  /////////////

  private void initFaceTracker(boolean isFrontFacing) {
    if (faceDetector != null) faceDetector.release();

    faceDetector = new FaceDetector.Builder(context).setTrackingEnabled(true)
        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
        .setMode(FaceDetector.FAST_MODE)
        .setProminentFaceOnly(isFrontFacing)
        .setMinFaceSize(isFrontFacing ? 0.35f : 0.15f)
        .build();

    //Detector.Processor<Face> processor;
    //if (isFrontFacing) {
    //  Tracker<Face> tracker = new TribeFaceTracker();
    //  processor = new LargestFaceFocusingProcessor.Builder(faceDetector, tracker).build();
    //} else {
    //  MultiProcessor.Factory<Face> factory = face -> new TribeFaceTracker();
    //  processor = new MultiProcessor.Builder<>(factory).build();
    //}

    faceDetector.setProcessor(new MultiProcessor.Builder<>(new TribeFaceTrackerFactory()).build());

    //faceDetector.setProcessor(processor);

    if (!faceDetector.isOperational()) {
      Timber.w("Face detector dependencies are not yet available.");
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
      Timber.d("onUpdate : " + face);
      VisionAPIManager.this.face = face;
      computeFace(face);
    }

    @Override public void onMissing(FaceDetector.Detections<Face> detectionResults) {
      Timber.d("onMissing : " + detectionResults);
      VisionAPIManager.this.face = null;
      VisionAPIManager.this.rightEye = VisionAPIManager.this.leftEye = null;
    }

    @Override public void onDone() {
      Timber.d("onDone");
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
        .filter(frame1 -> (t0 - timeToDetect >= 5))
        .flatMap(frame -> Observable.just(frame)
            .observeOn(Schedulers.from(frameExecutor))
            .map(frame1 -> {
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

  public Face getFace() {
    return face;
  }

  public PointF getLeftEye() {
    return leftEye;
  }

  public PointF getRightEye() {
    return rightEye;
  }

  public void stopCapture() {
    firstFrame = true;
    dispose();
  }

  public void dispose() {
    subscriptions.clear();
    if (faceDetector != null) faceDetector.release();
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Float> onFaceWidthChange() {
    return onFaceWidthChange;
  }
}
