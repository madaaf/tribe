/*
 *  Copyright 2016 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.tribe.tribelivesdk.webrtc;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.Pair;
import com.tribe.tribelivesdk.entity.CameraInfo;
import com.tribe.tribelivesdk.stream.FrameManager;
import java.util.Arrays;
import org.webrtc.Logging;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.ThreadUtils;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

@SuppressWarnings("deprecation") public abstract class CameraCapturer
    implements CameraVideoCapturer {
  enum SwitchState {
    IDLE, // No switch requested.
    PENDING, // Waiting for previous capture session to open.
    IN_PROGRESS, // Waiting for new switched capture session to start.
  }

  private static final String TAG = "CameraCapturer";
  private final static int MAX_OPEN_CAMERA_ATTEMPTS = 3;
  private final static int OPEN_CAMERA_DELAY_MS = 500;
  private final static int OPEN_CAMERA_TIMEOUT = 10000;

  private final CameraEnumerator cameraEnumerator;
  private final CameraEventsHandler eventsHandler;
  private final Handler uiThreadHandler;
  private boolean frontFacing = true;
  private SurfaceTexture surfaceTexture;
  private int[] rotations;
  private RectF[] rectFs;
  private Pair<RectF[], int[]> pairFaceRotations;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private Subscription subscriptionCaptureByteBuffer;
  private PublishSubject<Frame> onFrame = PublishSubject.create();
  private PublishSubject<Frame> onPreviewFrame = PublishSubject.create();
  private PublishSubject<Pair<RectF[], int[]>> onFaces = PublishSubject.create();
  private PublishSubject<TribeI420Frame> onLocalFrame = PublishSubject.create();
  private PublishSubject<CameraInfo> onNewCameraInfo = PublishSubject.create();

  private final CameraSession.CreateSessionCallback createSessionCallback =
      new CameraSession.CreateSessionCallback() {
        @Override public void onDone(CameraSession session) {
          checkIsOnCameraThread();
          Logging.d(TAG, "Create session done");
          uiThreadHandler.removeCallbacks(openCameraTimeoutRunnable);
          synchronized (stateLock) {
            capturerObserver.onCapturerStarted(true /* success */);
            sessionOpening = false;
            currentSession = session;
            onNewCameraInfo.onNext(currentSession.getCameraInfo());
            cameraStatistics = new CameraStatistics(surfaceHelper, eventsHandler);
            firstFrameObserved = false;
            stateLock.notifyAll();

            if (switchState == SwitchState.IN_PROGRESS) {
              if (switchEventsHandler != null) {
                frontFacing = cameraEnumerator.isFrontFacing(cameraName);
                switchEventsHandler.onCameraSwitchDone(frontFacing);
                switchEventsHandler = null;
              }
              switchState = SwitchState.IDLE;
            } else if (switchState == SwitchState.PENDING) {
              switchState = SwitchState.IDLE;
              switchCameraInternal(switchEventsHandler);
            }

            if (surfaceTexture != null) setPreviewTexture(surfaceTexture);
          }
        }

        @Override public void onFailure(CameraSession.FailureType failureType, String error) {
          checkIsOnCameraThread();
          uiThreadHandler.removeCallbacks(openCameraTimeoutRunnable);
          synchronized (stateLock) {
            capturerObserver.onCapturerStarted(false /* success */);
            openAttemptsRemaining--;

            if (openAttemptsRemaining <= 0) {
              Logging.w(TAG, "Opening camera failed, passing: " + error);
              sessionOpening = false;
              stateLock.notifyAll();

              if (switchState != SwitchState.IDLE) {
                if (switchEventsHandler != null) {
                  switchEventsHandler.onCameraSwitchError(error);
                  switchEventsHandler = null;
                }
                switchState = SwitchState.IDLE;
              }

              if (failureType ==
                  com.tribe.tribelivesdk.webrtc.CameraSession.FailureType.DISCONNECTED) {
                eventsHandler.onCameraDisconnected();
              } else {
                eventsHandler.onCameraError(error);
              }
            } else {
              Logging.w(TAG, "Opening camera failed, retry: " + error);

              createSessionInternal(OPEN_CAMERA_DELAY_MS);
            }
          }
        }
      };

  private final CameraSession.Events cameraSessionEventsHandler = new CameraSession.Events() {
    @Override public void onCameraOpening() {
      checkIsOnCameraThread();
      synchronized (stateLock) {
        if (currentSession != null) {
          Logging.w(TAG, "onCameraOpening while session was open.");
          return;
        }
        eventsHandler.onCameraOpening(cameraName);
      }
    }

    @Override public void onCameraError(CameraSession session, String error) {
      checkIsOnCameraThread();
      synchronized (stateLock) {
        if (session != currentSession) {
          Logging.w(TAG, "onCameraError from another session: " + error);
          return;
        }
        eventsHandler.onCameraError(error);
        stopCapture();
      }
    }

    @Override public void onCameraDisconnected(CameraSession session) {
      checkIsOnCameraThread();
      synchronized (stateLock) {
        if (session != currentSession) {
          Logging.w(TAG, "onCameraDisconnected from another session.");
          return;
        }
        eventsHandler.onCameraDisconnected();
        stopCapture();
      }
    }

    @Override public void onCameraClosed(CameraSession session) {
      checkIsOnCameraThread();
      synchronized (stateLock) {
        if (session != currentSession && currentSession != null) {
          Logging.d(TAG, "onCameraClosed from another session.");
          return;
        }
        eventsHandler.onCameraClosed();
      }
    }

    @Override
    public void onByteBufferFrameCaptured(CameraSession session, byte[] data, int width, int height,
        int rotation, long timestamp) {
      checkIsOnCameraThread();
      synchronized (stateLock) {
        if (session != currentSession) {
          Logging.w(TAG, "onByteBufferFrameCaptured from another session.");
          return;
        }

        if (!firstFrameObserved) {
          eventsHandler.onFirstFrameAvailable();
          firstFrameObserved = true;
        }

        cameraStatistics.addFrame();

        if (rotation != lastFrameRotation && currentSession != null) {
          onNewCameraInfo.onNext(currentSession.getCameraInfo());
        }

        lastFrameRotation = rotation;

        //Timber.d("onByteBufferFrameCaptured FrameRotation : " + rotation);
        onPreviewFrame.onNext(new Frame(data, width, height, rotation, timestamp, frontFacing));
      }
    }

    @Override public void onTextureFrameCaptured(CameraSession session, int width, int height,
        int oesTextureId, float[] transformMatrix, int rotation, long timestamp) {
      checkIsOnCameraThread();
      synchronized (stateLock) {
        if (session != currentSession) {
          Logging.w(TAG, "onTextureFrameCaptured from another session.");
          surfaceHelper.returnTextureFrame();
          return;
        }
        if (!firstFrameObserved) {
          eventsHandler.onFirstFrameAvailable();
          firstFrameObserved = true;
        }
        cameraStatistics.addFrame();
        capturerObserver.onTextureFrameCaptured(width, height, oesTextureId, transformMatrix,
            rotation, timestamp);
      }
    }

    @Override public void onDetectedFaces(Camera.Face[] faces) {
      //if (currentSession == null || currentSession.getCameraInfo() == null) return;
      //
      //rotations = new int[faces.length];
      //rectFs = new RectF[faces.length];
      //for (int i = 0; i < faces.length; i++) {
      //  Camera.Face face = faces[i];
      //
      //  int width = currentSession.getCameraInfo().getCaptureFormat().width;
      //  int height = currentSession.getCameraInfo().getCaptureFormat().height;
      //
      //  RectF bounds = new RectF(face.rect.left, face.rect.top, face.rect.right, face.rect.bottom);
      //  Matrix matrix = new Matrix();
      //
      //                              /*START - convert driver coordinates to View coordinates in pixels*/
      //  matrix.setScale(1, 1); // for front facing camera (matrix.setScale(1, 1); otherwise)
      //  matrix.postRotate(currentSession.getCameraInfo().getFrameOrientation());
      //  // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
      //  // UI coordinates range from (0, 0) to (width, height).
      //  matrix.postScale(width / 2000f, height / 2000f);
      //  matrix.postTranslate(width / 2f, height / 2f);
      //  matrix.mapRect(bounds);
      //
      //  rectFs[i] = bounds;
      //  //Timber.d("Bounds : " + bounds);
      //  rotations[i] = -currentSession.getCameraInfo().getFrameOrientation();
      //}
      //
      //pairFaceRotations = new Pair<>(rectFs, rotations);
      //onFaces.onNext(pairFaceRotations);
    }
  };

  private final Runnable openCameraTimeoutRunnable = new Runnable() {
    @Override public void run() {
      eventsHandler.onCameraError("Camera failed to start within timeout.");
    }
  };

  // Initialized on initialize
  // -------------------------
  private Handler cameraThreadHandler;
  private Context applicationContext;
  private CapturerObserver capturerObserver;
  private SurfaceTextureHelper surfaceHelper;
  private FrameManager frameManager;

  private final Object stateLock = new Object();
  private boolean sessionOpening; /* guarded by stateLock */
  private CameraSession currentSession; /* guarded by stateLock */
  private String cameraName; /* guarded by stateLock */
  private int width; /* guarded by stateLock */
  private int height; /* guarded by stateLock */
  private int framerate; /* guarded by stateLock */
  private int openAttemptsRemaining; /* guarded by stateLock */
  private SwitchState switchState = SwitchState.IDLE; /* guarded by stateLock */
  private CameraSwitchHandler switchEventsHandler; /* guarded by stateLock */
  // Valid from onDone call until stopCapture, otherwise null.
  private CameraStatistics cameraStatistics; /* guarded by stateLock */
  private boolean firstFrameObserved; /* guarded by stateLock */
  private int lastFrameRotation; /* guarded by stateLock */

  public CameraCapturer(String cameraName, CameraEventsHandler eventsHandler,
      CameraEnumerator cameraEnumerator) {
    if (eventsHandler == null) {
      eventsHandler = new CameraEventsHandler() {
        @Override public void onCameraError(String errorDescription) {
        }

        @Override public void onCameraDisconnected() {
        }

        @Override public void onCameraFreezed(String errorDescription) {
        }

        @Override public void onCameraOpening(String cameraName) {
        }

        @Override public void onFirstFrameAvailable() {
        }

        @Override public void onCameraClosed() {
        }
      };
    }

    this.eventsHandler = eventsHandler;
    this.cameraEnumerator = cameraEnumerator;
    this.cameraName = cameraName;
    uiThreadHandler = new Handler(Looper.getMainLooper());

    final String[] deviceNames = cameraEnumerator.getDeviceNames();

    if (deviceNames.length == 0) {
      throw new RuntimeException("No cameras attached.");
    }
    if (!Arrays.asList(deviceNames).contains(this.cameraName)) {
      throw new IllegalArgumentException(
          "Camera name " + this.cameraName + " does not match any known camera device.");
    }
  }

  @Override
  public void initialize(SurfaceTextureHelper surfaceTextureHelper, Context applicationContext,
      CapturerObserver capturerObserver) {
    this.applicationContext = applicationContext;
    this.capturerObserver = capturerObserver;
    this.surfaceHelper = surfaceTextureHelper;
    cameraThreadHandler = surfaceTextureHelper == null ? null : surfaceTextureHelper.getHandler();

    frameManager = new FrameManager(applicationContext);
    subscriptions.add(frameManager.onRemoteFrame()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.from(cameraThreadHandler.getLooper()))
        .subscribe(frame -> capturerObserver.onByteBufferFrameCaptured(frame.getDataOut(),
            frame.getWidth(), frame.getHeight(), frame.getRotation(), frame.getTimestamp())));
  }

  @Override public void startCapture(int width, int height, int framerate) {
    Logging.d(TAG, "startCapture: " + width + "x" + height + "@" + framerate);
    if (applicationContext == null) {
      throw new RuntimeException("CameraCapturer must be initialized before calling startCapture.");
    }

    synchronized (stateLock) {
      if (sessionOpening || currentSession != null) {
        Logging.w(TAG, "Session already open");
        return;
      }

      this.width = width;
      this.height = height;
      this.framerate = framerate;

      sessionOpening = true;
      openAttemptsRemaining = MAX_OPEN_CAMERA_ATTEMPTS;
      createSessionInternal(0);

      frameManager.startCapture();
      frameManager.initFrameSubscription(onFrame);
      frameManager.initPreviewFrameSubscription(onPreviewFrame);
      //frameManager.initNewFacesSubscriptions(onFaces);
    }
  }

  private void createSessionInternal(int delayMs) {
    uiThreadHandler.postDelayed(openCameraTimeoutRunnable, delayMs + OPEN_CAMERA_TIMEOUT);
    cameraThreadHandler.postDelayed(
        () -> createCameraSession(createSessionCallback, cameraSessionEventsHandler,
            applicationContext, surfaceHelper, cameraName, width, height, framerate), delayMs);
  }

  @Override public void stopCapture() {
    Logging.d(TAG, "Stop capture");

    synchronized (stateLock) {
      while (sessionOpening) {
        Logging.d(TAG, "Stop capture: Waiting for session to open");
        ThreadUtils.waitUninterruptibly(stateLock);
      }

      if (currentSession != null) {
        Logging.d(TAG, "Stop capture: Nulling session");
        cameraStatistics.release();
        cameraStatistics = null;
        final CameraSession oldSession = currentSession;
        cameraThreadHandler.post(() -> oldSession.stop());
        currentSession = null;
        capturerObserver.onCapturerStopped();
        frameManager.stopCapture();
        if (subscriptionCaptureByteBuffer != null) subscriptionCaptureByteBuffer.unsubscribe();
      } else {
        Logging.d(TAG, "Stop capture: No session open");
      }
    }

    Logging.d(TAG, "Stop capture done");
  }

  @Override public void changeCaptureFormat(int width, int height, int framerate) {
    Logging.d(TAG, "changeCaptureFormat: " + width + "x" + height + "@" + framerate);
    synchronized (stateLock) {
      stopCapture();
      startCapture(width, height, framerate);
    }
  }

  @Override public void dispose() {
    Logging.d(TAG, "dispose");
    surfaceTexture = null;
    subscriptions.clear();
    stopCapture();
    frameManager.dispose();
  }

  @Override public void switchCamera(final CameraSwitchHandler switchEventsHandler) {
    Logging.d(TAG, "switchCamera");
    cameraThreadHandler.post(() -> switchCameraInternal(switchEventsHandler));
  }

  @Override public void switchFilter() {
    frameManager.switchFilter();
  }

  @Override public boolean isScreencast() {
    return false;
  }

  @Override public void setPreviewTexture(SurfaceTexture surfaceTexture) {
    Logging.d(TAG, "Set preview texture");

    synchronized (stateLock) {
      while (sessionOpening) {
        Logging.d(TAG, "Set preview texture : Waiting for session to open");
        ThreadUtils.waitUninterruptibly(stateLock);
      }

      if (currentSession != null) {
        this.surfaceTexture = surfaceTexture;
        currentSession.setPreviewTexture(createSessionCallback, this.surfaceTexture);
      } else {
        Logging.d(TAG, "Set preview texture : No session open");
      }
    }

    Logging.d(TAG, "Set preview texture : done");
  }

  @Override public void initFrameAvailableObs(Observable<Frame> obs) {
    subscriptionCaptureByteBuffer = obs.onBackpressureDrop()
        .observeOn(AndroidSchedulers.from(cameraThreadHandler.getLooper()))
        .subscribe(frame -> {
          checkIsOnCameraThread();
          synchronized (stateLock) {
            if (!firstFrameObserved) {
              eventsHandler.onFirstFrameAvailable();
              firstFrameObserved = true;
            }

            if (cameraStatistics != null) cameraStatistics.addFrame();

            onFrame.onNext(frame);
          }
        });
  }

  public void printStackTrace() {
    Thread cameraThread = null;
    if (cameraThreadHandler != null) {
      cameraThread = cameraThreadHandler.getLooper().getThread();
    }
    if (cameraThread != null) {
      StackTraceElement[] cameraStackTrace = cameraThread.getStackTrace();
      if (cameraStackTrace.length > 0) {
        Logging.d(TAG, "CameraCapturer stack trace:");
        for (StackTraceElement traceElem : cameraStackTrace) {
          Logging.d(TAG, traceElem.toString());
        }
      }
    }
  }

  private void switchCameraInternal(final CameraSwitchHandler switchEventsHandler) {
    Logging.d(TAG, "switchCamera internal");

    frameManager.switchCamera();

    final String[] deviceNames = cameraEnumerator.getDeviceNames();

    if (deviceNames.length < 2) {
      if (switchEventsHandler != null) {
        switchEventsHandler.onCameraSwitchError("No camera to switch to.");
      }
      return;
    }

    synchronized (stateLock) {
      if (switchState != SwitchState.IDLE) {
        Logging.d(TAG, "switchCamera switchInProgress");
        if (switchEventsHandler != null) {
          switchEventsHandler.onCameraSwitchError("Camera switch already in progress.");
        }
        return;
      }

      if (!sessionOpening && currentSession == null) {
        Logging.d(TAG, "switchCamera: No session open");
        if (switchEventsHandler != null) {
          switchEventsHandler.onCameraSwitchError("Camera is not running.");
        }
        return;
      }

      this.switchEventsHandler = switchEventsHandler;
      if (sessionOpening) {
        switchState = SwitchState.PENDING;
        return;
      } else {
        switchState = SwitchState.IN_PROGRESS;
      }

      Logging.d(TAG, "switchCamera: Stopping session");
      cameraStatistics.release();
      cameraStatistics = null;
      final CameraSession oldSession = currentSession;
      cameraThreadHandler.post(() -> oldSession.stop());
      currentSession = null;

      int cameraNameIndex = Arrays.asList(deviceNames).indexOf(cameraName);
      cameraName = deviceNames[(cameraNameIndex + 1) % deviceNames.length];

      sessionOpening = true;
      openAttemptsRemaining = 1;
      createSessionInternal(0);
    }
    Logging.d(TAG, "switchCamera done");
  }

  private void checkIsOnCameraThread() {
    if (Thread.currentThread() != cameraThreadHandler.getLooper().getThread()) {
      Logging.e(TAG, "Check is on camera thread failed.");
      throw new RuntimeException("Not on camera thread.");
    }
  }

  protected String getCameraName() {
    synchronized (stateLock) {
      return cameraName;
    }
  }

  abstract protected void createCameraSession(
      CameraSession.CreateSessionCallback createSessionCallback, CameraSession.Events events,
      Context applicationContext, SurfaceTextureHelper surfaceTextureHelper, String cameraName,
      int width, int height, int framerate);

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<CameraInfo> onNewCameraInfo() {
    return onNewCameraInfo;
  }
}
