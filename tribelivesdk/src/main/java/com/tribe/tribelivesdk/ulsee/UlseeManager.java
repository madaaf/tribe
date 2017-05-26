package com.tribe.tribelivesdk.ulsee;

import android.content.Context;
import android.graphics.RectF;
import android.hardware.Camera;
import android.view.Surface;
import android.view.WindowManager;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.uls.multifacetrackerlib.UlsMultiTracker;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 25/05/2017.
 */

public class UlseeManager {

  public static final int MAX_TRACKER = 1;
  private static final String ACTIVATION_KEY = "xIFRwx4cdrQfsWef4MiOFMOGTKxnOMRd";

  private static UlseeManager instance;

  public static UlseeManager getInstance(Context context) {
    if (instance == null) {
      instance = new UlseeManager(context);
    }

    return instance;
  }

  // VARIABLES
  private UlsMultiTracker ulsTracker;
  private DeviceRotationDetector deviceRotationDetector;
  private boolean faceDetectionRunning = false, isCountingEfficiency = false, hasFacesFromCamera =
      false, firstFrame = true;
  private long timingCounter, timeStartFrame, trackTime, timingMS, timeDoFaceDet, t0;
  private int trackCount = 0, trackFailCount = 0, lastFrameRotation, cameraRotation = 90, alive,
      displayRotation;
  private RectF[] rectFaces;
  private int[] rotations;
  private float[][] shape, confidence;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public UlseeManager(Context context) {
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

    ulsTracker = new UlsMultiTracker(context, MAX_TRACKER);
    boolean activation = ulsTracker.activate(ACTIVATION_KEY);

    if (activation) {
      ulsTracker.initialise();
    }

    ulsTracker.setTrackerConfidenceThreshold(0.35f, 0.2f);
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

  public void initNewFacesObs(Observable<Camera.Face[]> obs) {
    //subscriptions.add(obs.subscribe(faces -> {
    //  if (faces == null) return;
    //  hasFacesFromCamera = true;
    //  ulsTracker.resetTracker(-1);
    //  rectFaces = new RectF[faces.length];
    //  rotations = new int[faces.length];
    //
    //  int i = 0;
    //  for (Camera.Face face : faces) {
    //    rectFaces[i] = new RectF(face.rect);
    //    rotations[i] = lastFrameRotation;
    //  }
    //
    //  ulsTracker.addFaces(rectFaces, rotations);
    //}));
  }

  public void initFrameSubscription(Observable<Frame> obs) {
    subscriptions.add(obs.onBackpressureDrop()
        .observeOn(Schedulers.computation())
        .map(frame -> {
          if (firstFrame || lastFrameRotation != frame.getRotation()) {
            firstFrame = false;
            lastFrameRotation = frame.getRotation();
            cameraRotation = computeCameraRotation(frame.getRotation(), displayRotation);
          }

          t0 = System.currentTimeMillis();

          if (timingCounter == 0) timeStartFrame = t0;

          int alive = ulsTracker.update(frame.getData(), frame.getWidth(), frame.getHeight(),
              UlsMultiTracker.ImageDataType.NV21);

          //if (alive > 0) Timber.d("Alive : " + alive);

          long t1 = System.currentTimeMillis();

          if (isCountingEfficiency) {
            if (alive > 0) {
              trackCount++;
              trackTime = trackTime + (t1 - t0);
            } else {
              trackFailCount++;
            }
          }

          timingCounter++;
          timingMS += (t1 - t0);

          if (timingCounter == 10) {
            final float millisPerFrame = timingMS / (float) timingCounter;
            final int framePerSecond =
                (int) (1000.0 / ((t1 - timeStartFrame) / (double) (timingCounter - 1)));
            //Timber.d("Proc. time: %.2f ms, Frame rate: %d", millisPerFrame, framePerSecond);

            timingCounter = 0;
            timingMS = 0;

            // Performance test.
            //if (isCountingEfficiency) {
            //  updateEfficiencystats();
            //}
          }

          if (alive > 0) {
            for (int k = 0; k < MAX_TRACKER; k++) {
              shape[k] = ulsTracker.getShape(k);
              confidence[k] = ulsTracker.getConfidence(k);
              //Timber.d("Shape[" + k + "], : " + shape[k][18]);
              //Timber.d("Confidence[" + k + "], : " + confidence[k][18]);
            }
          } else {
            for (int k = alive; k < MAX_TRACKER; k++) {
              shape[k] = null;
              confidence[k] = null;
            }
          }

          return frame;
        })
        .filter(frame1 -> (alive < UlseeManager.MAX_TRACKER
            && (t0 - timeDoFaceDet >= 500 || alive == 0)
            && !faceDetectionRunning))
        .flatMap(frame -> Observable.just(frame).observeOn(Schedulers.computation()).map(frame1 -> {
          faceDetectionRunning = true;

          final int detectDegree =
              (cameraRotation + 360 - deviceRotationDetector.getRotationDegree()) % 360;
          //Timber.d("cameraRotation : " + cameraRotation);
          //Timber.d("displayRotation : " + displayRotation);
          //Timber.d("frameRotation : " + frame.getRotation());
          //Timber.d("deviceRotationDetector.getRotationDegree : "
          //    + deviceRotationDetector.getRotationDegree());
          //Timber.d("Width : %d, Height : %d, Rotation : %d", frame.getWidth(), frame.getHeight(),
          //    detectDegree);
          ulsTracker.findFacesAndAdd(frame.getData(), frame.getWidth(), frame.getHeight(),
              detectDegree, UlsMultiTracker.ImageDataType.NV21);
          faceDetectionRunning = false;
          return null;
        }))
        .subscribe());
  }

  public void stopCapture() {
    firstFrame = true;
    subscriptions.clear();
  }

  public void updateDisplayRotation(Context context) {
    WindowManager windowService = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    displayRotation = windowService.getDefaultDisplay().getRotation();
  }
}
