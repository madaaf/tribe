package com.tribe.tribelivesdk.facetracking;

import android.content.Context;
import android.graphics.RectF;
import android.support.v4.util.Pair;
import android.view.Surface;
import android.view.WindowManager;
import com.tribe.tribelivesdk.back.FrameExecutor;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.uls.multifacetrackerlib.UlsMultiTracker;
import com.uls.multifacetrackerlib.UlsTrackerMode;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 25/05/2017.
 */

public class UlseeManager {

  public static final int MAX_TRACKER = 5;
  private static final String ACTIVATION_KEY = "xIFRwx4cdrQfsWef4MiOFMOGTKxnOMRd";

  private static UlseeManager instance;

  public static UlseeManager getInstance(Context context) {
    if (instance == null) {
      instance = new UlseeManager(context);
    }

    return instance;
  }

  // VARIABLES
  private FrameExecutor frameExecutor;
  private UlsMultiTracker ulsTracker;
  private DeviceRotationDetector deviceRotationDetector;
  private boolean faceDetectionRunning = false, firstFrame = true, faceTracked;
  private long timeDoFaceDet;
  private int lastFrameRotation, cameraRotation = 90, alive, displayRotation;
  private float[][] shape, confidence, pose, pupils, gaze;
  private float[] poseQuality;
  private int[] rotations;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Boolean> onNewFaceDetected = PublishSubject.create();
  private PublishSubject<Boolean> onLostFace = PublishSubject.create();

  public UlseeManager(Context context) {
    frameExecutor = new FrameExecutor();
    initTracker(context);

    deviceRotationDetector = new DeviceRotationDetector(context);
    updateDisplayRotation(context);
  }

  /////////////
  // PRIVATE //
  /////////////

  private void initTracker(Context context) {
    shape = new float[MAX_TRACKER][];
    confidence = new float[MAX_TRACKER][];
    pose = new float[MAX_TRACKER][];
    poseQuality = new float[MAX_TRACKER];
    pupils = new float[MAX_TRACKER][];
    gaze = new float[MAX_TRACKER][];

    ulsTracker = new UlsMultiTracker(context, MAX_TRACKER,
        UlsMultiTracker.UlsTrackerInterfaceType.NV21_BYTEARRAY);
    ulsTracker.setTrackerConfidenceThreshold(0.39f, 0.2f);

    boolean activation = ulsTracker.activate(ACTIVATION_KEY);

    if (activation) {
      ulsTracker.initialise();
    }

    ulsTracker.setTrackMode(UlsTrackerMode.TRACK_FACE_AND_POSE);
    ulsTracker.setSticky(true);
    ulsTracker.setHighPrecision(true);
  }

  private int computeCameraRotation(int orientation, int displayRotation) {
    int degrees = 0;
    switch (displayRotation) {
      case Surface.ROTATION_0:
        degrees = 0;
        break;
      case Surface.ROTATION_90:
        degrees = 90;
        break;
      case Surface.ROTATION_180:
        degrees = 180;
        break;
      case Surface.ROTATION_270:
        degrees = 270;
        break;
    }

    int result;
    //        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
    result = (orientation + degrees) % 360;
    result = (360 - result) % 360;  // compensate the mirror

    return result;
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public UlsMultiTracker getTracker() {
    return ulsTracker;
  }

  public void initFrameSubscription(Observable<Frame> obs) {
    if (subscriptions.hasSubscriptions()) return;

    subscriptions.add(obs.onBackpressureDrop()
        .observeOn(Schedulers.computation())
        .map(frame -> {
          if (firstFrame || lastFrameRotation != frame.getRotation()) {
            firstFrame = false;
            lastFrameRotation = frame.getRotation();
            cameraRotation = computeCameraRotation(frame.getRotation(), displayRotation);
          }

          alive = ulsTracker.update(frame.getData(), frame.getWidth(), frame.getHeight(),
              UlsMultiTracker.ImageDataType.NV21);

          if (faceTracked == false && alive > 0) {
            onNewFaceDetected.onNext(true);
            faceTracked = true;
          } else if (faceTracked == true && alive == 0) {
            onLostFace.onNext(true);
            faceTracked = false;
          }

          //Timber.d("Alive : " + alive);
          if (alive > 0) {
            for (int k = 0; k < MAX_TRACKER; k++) {
              shape[k] = ulsTracker.getShape(k);

              //if (shape[k] != null) {
              //  for (int i = 0; i < shape[k].length; i++) {
              //    shape[k][i] = shape[k][i] + 100;
              //  }
              //}

              confidence[k] = ulsTracker.getConfidence(k);
              float[] xy = ulsTracker.getTranslationInImage(k);
              if (xy != null) {
                pose[k] = new float[6];
                float[] angles = ulsTracker.getRotationAngles(k);
                pose[k][0] = angles[0];
                pose[k][1] = angles[1];
                pose[k][2] = angles[2];
                pose[k][3] = xy[0];
                pose[k][4] = xy[1];
                pose[k][5] = ulsTracker.getScaleInImage(k);
              } else {
                pose[k] = null;
              }
              poseQuality[k] = ulsTracker.getPoseQuality(k);
              gaze[k] = ulsTracker.getGaze(k);
              pupils[k] = ulsTracker.getPupils(k);
            }
          } else {
            for (int k = 0; k < MAX_TRACKER; k++) {
              shape[k] = null;
              confidence[k] = null;
              pose[k] = null;
              poseQuality[k] = 0.0f;
              gaze[k] = null;
              pupils[k] = null;
            }
          }

          return frame;
        })
        .filter(frame1 -> (alive == 0 && !faceDetectionRunning))
        .flatMap(frame -> Observable.just(frame)
            .subscribeOn(Schedulers.from(frameExecutor))
            .map(frame1 -> {
              //Timber.d("Thread name : " + Thread.currentThread().getName());
              faceDetectionRunning = true;

              final int detectDegree =
                  (cameraRotation + 360 - deviceRotationDetector.getRotationDegree()) % 360;
              //Timber.d("cameraRotation : "
              //+ cameraRotation);
              //Timber.d("displayRotation : " + displayRotation);
              //Timber.d("frameRotation : " + frame.getRotation());
              //Timber.d("deviceRotationDetector.getRotationDegree : "
              //    + deviceRotationDetector.getRotationDegree());
              //Timber.d("Width : %d, Height : %d, Rotation : %d", frame.getWidth(), frame.getHeight(),
              //    detectDegree);
              ulsTracker.findFacesAndAdd(frame.getData(), frame.getWidth(), frame.getHeight(),
                  detectDegree, UlsMultiTracker.ImageDataType.NV21);
              timeDoFaceDet = System.currentTimeMillis();
              faceDetectionRunning = false;
              return null;
            }), 1)
        .subscribe());
  }

  public void initNewFacesSubscription(Observable<Pair<RectF[], int[]>> obs) {
    subscriptions.add(
        obs.onBackpressureDrop().observeOn(Schedulers.computation()).subscribe(pair -> {
          ulsTracker.addFaces(pair.first, pair.second);
        }));
  }

  public void stopCapture() {
    firstFrame = true;
    subscriptions.clear();
  }

  public void updateDisplayRotation(Context context) {
    WindowManager windowService = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    displayRotation = windowService.getDefaultDisplay().getRotation();
  }

  public float[][] getShape() {
    return shape;
  }

  public float[][] getPose() {
    return pose;
  }

  public float[] getPoseQuality() {
    return poseQuality;
  }

  public float[][] getConfidence() {
    return confidence;
  }

  public float[][] getGaze() {
    return gaze;
  }

  public float[][] getPupils() {
    return pupils;
  }

  public int getCameraRotation() {
    return cameraRotation;
  }

  public boolean isOpenMouth() {
    for (int k = 0; k < MAX_TRACKER; k++) {

      if (shape == null) {
        shape = new float[5][];
      }

      shape[k] = ulsTracker.getShape(k);
      float scale = ulsTracker.getScaleInImage(k);

      if (shape[k] != null && scale > 0) {
        double dist = Math.sqrt(
            ((shape[k][2 * 60] - shape[k][2 * 65]) * (shape[k][2 * 60] - shape[k][2 * 65]) +
                ((shape[k][2 * 60 + 1] - shape[k][2 * 65 + 1]) *
                    (shape[k][2 * 60 + 1] - shape[k][2 * 65 + 1]))) / (0.9 * scale));

        if (dist > 4) {
          return true;
        }
      }
    }

    return false;
  }

  public void dispose() {
    subscriptions.clear();
  }
}
