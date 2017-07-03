package com.tribe.tribelivesdk.facetracking;

import android.content.Context;
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
  private boolean faceDetectionRunning = false, hasFacesFromCamera = false, firstFrame = true,
      faceTracked;
  private long timeDoFaceDet;
  private int lastFrameRotation, cameraRotation = 90, alive, displayRotation;
  private int[] rotations;
  private float[][] shape, confidence;

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

    ulsTracker = new UlsMultiTracker(context, MAX_TRACKER,
        UlsMultiTracker.UlsTrackerInterfaceType.NV21_BYTEARRAY);

    boolean activation = ulsTracker.activate(ACTIVATION_KEY);

    if (activation) {
      ulsTracker.initialise();
    }

    ulsTracker.setTrackerConfidenceThreshold(0.39f, 0.2f);
    ulsTracker.setSticky(true);
    ulsTracker.setTrackMode(UlsTrackerMode.TRACK_FACE_AND_POSE);
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
        .filter(frame1 -> {
          //Timber.d("Go ! : " + (alive < UlseeManager.MAX_TRACKER &&
          //    (System.currentTimeMillis() - timeDoFaceDet >= 1500 || alive == 0) &&
          //    !faceDetectionRunning));
          return (alive < UlseeManager.MAX_TRACKER &&
              (System.currentTimeMillis() - timeDoFaceDet >= 1500 || alive == 0) &&
              !faceDetectionRunning);
        })
        .flatMap(frame -> Observable.just(frame)
            .observeOn(Schedulers.from(frameExecutor))
            .map(frame1 -> {
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
              timeDoFaceDet = System.currentTimeMillis();
              faceDetectionRunning = false;
              return null;
            }), 1)
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
