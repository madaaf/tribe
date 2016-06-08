package com.tribe.app.presentation.view.widget;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.gles.Drawable2d;
import com.tribe.app.presentation.view.gles.EglCore;
import com.tribe.app.presentation.view.gles.GlUtil;
import com.tribe.app.presentation.view.gles.ScaledDrawable2d;
import com.tribe.app.presentation.view.gles.Sprite2d;
import com.tribe.app.presentation.view.gles.Texture2dProgram;
import com.tribe.app.presentation.view.gles.WindowSurface;
import com.tribe.app.presentation.view.utils.CameraUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class CameraManager extends FrameLayout {

    private static final String TAG = "TRIBE";

    private static final int DEFAULT_ZOOM_PERCENT = 0;      // 0-100
    private static final int DEFAULT_SIZE_PERCENT = 100;     // 0-100
    private static final int DEFAULT_ROTATE_PERCENT = 0;    // 0-100

    // Requested values; actual may differ.
    private static int REQ_CAMERA_WIDTH = 1280;
    private static int REQ_CAMERA_HEIGHT = 720;
    private static final int REQ_CAMERA_FPS = 20;

    private CameraManager.CameraViewSurface cameraViewSurface;

    // The holder for our SurfaceView.  The Surface can outlive the Activity (e.g. when
    // the screen is turned off and back on with the power button).
    //
    // This becomes non-null after the surfaceCreated() callback is called, and gets set
    // to null when surfaceDestroyed() is called.
    private static SurfaceHolder sSurfaceHolder;

    // Thread that handles rendering and controls the camera.  Started in onResume(),
    // stopped in onPause().
    private RenderThread mRenderThread;

    // Receives messages from renderer thread.
    private MainHandler mHandler;

    public CameraManager(Context context) {
        this(context, null);
    }

    public CameraManager(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHandler = new MainHandler((HomeActivity) context);

        try {
            cameraViewSurface = new CameraViewSurface(getContext());
            addView(cameraViewSurface);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void resume() {
        Log.d(TAG, "onResume BEGIN");
        mRenderThread = new RenderThread(getContext(), mHandler);
        mRenderThread.setName("TexFromCam Render");
        mRenderThread.start();
        mRenderThread.waitUntilReady();

        RenderHandler rh = mRenderThread.getHandler();

        if (sSurfaceHolder != null) {
            Log.d(TAG, "Sending previous surface");
            rh.sendSurfaceAvailable(sSurfaceHolder, false);
        } else {
            Log.d(TAG, "No previous surface");
        }
        Log.d(TAG, "onResume END");
    }

    public void pause() {
        Log.d(TAG, "onPause BEGIN");
        if (mRenderThread != null) {
            RenderHandler rh = mRenderThread.getHandler();
            rh.sendShutdown();
            try {
                mRenderThread.join();
            } catch (InterruptedException ie) {
                // not expected
                throw new RuntimeException("join was interrupted", ie);
            }
            mRenderThread = null;
            Log.d(TAG, "onPause END");
        }
    }

    public void capture(CameraViewListener cameraViewListener) {
        if (mRenderThread == null) {
            Log.w(TAG, "Ignoring action of capture");
            return;
        }

        RenderHandler rh = mRenderThread.getHandler();
        rh.sendCapture(cameraViewListener);

        // If we're getting preview frames quickly enough we don't really need this, but
        // we don't want to have chunky-looking resize movement if the camera is slow.
        // OTOH, if we get the updates too quickly (60fps camera?), this could jam us
        // up and cause us to run behind.  So use with caution.
        rh.sendRedraw();
    }

    public void startRecording(File videoFile) {
        if (mRenderThread == null) {
            Log.w(TAG, "Ignoring action of recording");
            return;
        }

        RenderHandler rh = mRenderThread.getHandler();
        rh.sendStartRecording(videoFile);
    }

    public void stopRecording() {
        if (mRenderThread == null) {
            Log.w(TAG, "Ignoring action of stopping");
            return;
        }

        RenderHandler rh = mRenderThread.getHandler();
        rh.sendStopRecording();
    }

    public void toggleCamera() {
        if (mRenderThread == null) {
            Log.w(TAG, "Ignoring action of stopping");
            return;
        }

        RenderHandler rh = mRenderThread.getHandler();
        rh.sendToggleCamera();
    }

    public class CameraViewSurface extends SurfaceView implements SurfaceHolder.Callback {
        int displayOrientation;

        public CameraViewSurface(Context context) {
            super(context);
            setBackgroundColor(Color.TRANSPARENT);
            getHolder().addCallback(this);
        }

        public int getDisplayOrientation() {
            return displayOrientation;
        }


        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "surfaceCreated holder=" + holder + " (static=" + sSurfaceHolder + ")");
            if (sSurfaceHolder != null) {
                throw new RuntimeException("sSurfaceHolder is already set");
            }

            sSurfaceHolder = holder;

            if (mRenderThread != null) {
                // Normal case -- render thread is running, tell it about the new surface.
                RenderHandler rh = mRenderThread.getHandler();
                rh.sendSurfaceAvailable(holder, true);
            } else {
                // Sometimes see this on 4.4.x N5: power off, power on, unlock, with device in
                // landscape and a lock screen that requires portrait.  The surface-created
                // message is showing up after onPause().
                //
                // Chances are good that the surface will be destroyed before the activity is
                // unpaused, but we track it anyway.  If the activity is un-paused and we start
                // the RenderThread, the SurfaceHolder will be passed in right after the thread
                // is created.
                Log.d(TAG, "render thread not running");
            }
        }

        @Override   // SurfaceHolder.Callback
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "surfaceChanged fmt=" + format + " size=" + width + "x" + height +
                    " holder=" + holder);

            if (mRenderThread != null) {
                RenderHandler rh = mRenderThread.getHandler();
                rh.sendSurfaceChanged(format, width, height);
            } else {
                Log.d(TAG, "Ignoring surfaceChanged");
                return;
            }
        }

        @Override   // SurfaceHolder.Callback
        public void surfaceDestroyed(SurfaceHolder holder) {
            // In theory we should tell the RenderThread that the surface has been destroyed.
            if (mRenderThread != null) {
                RenderHandler rh = mRenderThread.getHandler();
                rh.sendSurfaceDestroyed();
            }
            Log.d(TAG, "surfaceDestroyed holder=" + holder);
            sSurfaceHolder = null;
        }
    }

    /**
     * Custom message handler for main UI thread.
     * <p>
     * Receives messages from the renderer thread with UI-related updates, like the camera
     * parameters (which we show in a text message on screen).
     */
    private static class MainHandler extends Handler {
        private static final int MSG_SEND_CAMERA_PARAMS0 = 0;
        private static final int MSG_SEND_CAMERA_PARAMS1 = 1;
        private static final int MSG_SEND_ROTATION = 2;
        private static final int MSG_SEND_FACE_CAMERA = 3;

        private WeakReference<HomeActivity> mWeakActivity;

        public MainHandler(HomeActivity activity) {
            mWeakActivity = new WeakReference<HomeActivity>(activity);
        }

        /**
         * Sends the updated camera parameters to the main thread.
         * <p>
         * Call from render thread.
         */
        public void sendCameraParams(int width, int height, float fps) {
            // The right way to do this is to bundle them up into an object.  The lazy
            // way is to send two messages.
            sendMessage(obtainMessage(MSG_SEND_CAMERA_PARAMS0, width, height));
            sendMessage(obtainMessage(MSG_SEND_CAMERA_PARAMS1, (int) (fps * 1000), 0));
        }

        /**
         * Sends the updated camera parameters to the main thread.
         * <p>
         * Call from render thread.
         */
        public void sendRotation(int rotation) {
            sendMessage(obtainMessage(MSG_SEND_ROTATION, rotation));
        }

        /**
         * Sends if camera is front to the main thread.
         * <p>
         * Call from render thread.
         */
        public void sendIsFaceCamera(boolean isFaceCamera) {
            sendMessage(obtainMessage(MSG_SEND_FACE_CAMERA, isFaceCamera));
        }

        @Override
        public void handleMessage(Message msg) {
            HomeActivity activity = mWeakActivity.get();

            if (activity == null) {
                Log.d(TAG, "Got message for dead activity");
                return;
            }

            switch (msg.what) {
                case MSG_SEND_CAMERA_PARAMS0: {
                    //activity.mCameraPreviewWidth = msg.arg1;
                    //activity.mCameraPreviewHeight = msg.arg2;
                    break;
                }
                case MSG_SEND_CAMERA_PARAMS1: {
                    //activity.mCameraPreviewFps = msg.arg1 / 1000.0f;
                    break;
                }
                case MSG_SEND_ROTATION: {
                    //activity.mRotateDeg = (Integer) msg.obj;
                    break;
                }
                case MSG_SEND_FACE_CAMERA: {
                    //activity.mFaceCamera = (Boolean) msg.obj;
                    //activity.sendToggleCameraTrackEvent();
                    break;
                }
                default:
                    throw new RuntimeException("Unknown message " + msg.what);
            }
        }
    }

    /**
     * Thread that handles all rendering and camera operations.
     */
    private static class RenderThread extends Thread implements
            SurfaceTexture.OnFrameAvailableListener {
        // Object must be created on render thread to get correct Looper, but is used from
        // UI thread, so we need to declare it volatile to ensure the UI thread sees a fully
        // constructed object.
        private volatile RenderHandler mHandler;

        // Used to wait for the thread to start.
        private Object mStartLock = new Object();
        private boolean mReady = false;

        private MainHandler mMainHandler;

        private Camera mCamera;
        private int mCameraPreviewWidth, mCameraPreviewHeight;

        private EglCore mEglCore;
        private WindowSurface mWindowSurface;
        private int mWindowSurfaceWidth;
        private int mWindowSurfaceHeight;

        // Receives the output from the camera preview.
        private SurfaceTexture mCameraTexture;

        // Orthographic projection matrix.
        private float[] mDisplayProjectionMatrix = new float[16];

        private Texture2dProgram mTexProgram;
        private final ScaledDrawable2d mRectDrawable =
                new ScaledDrawable2d(Drawable2d.Prefab.RECTANGLE);
        private final Sprite2d mRect = new Sprite2d(mRectDrawable);

        private int mZoomPercent = DEFAULT_ZOOM_PERCENT;
        private int mSizePercent = DEFAULT_SIZE_PERCENT;
        private int mRotatePercent = DEFAULT_ROTATE_PERCENT;
        private float mPosX, mPosY;

        private int rotation;
        private int displayOrientation;
        private boolean takingPhoto;
        private boolean faceCamera;

        private MediaRecorder mediaRecorder;
        private boolean isRecording = false;

        private SharedPreferences pref;

        /**
         * Constructor.  Pass in the MainHandler, which allows us to send stuff back to the
         * Activity.
         */
        public RenderThread(Context ctx, MainHandler handler) {
            mMainHandler = handler;
            WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            Configuration config = ctx.getResources().getConfiguration();
            rotation = windowManager.getDefaultDisplay().getRotation();

            pref = ctx.getSharedPreferences("PREF_TRIBE", Activity.MODE_PRIVATE);
            faceCamera = pref.getBoolean("selfie_camera", true);

            if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
                rotation = (rotation+1) % 4;
            }

            if (rotation == Surface.ROTATION_0) {
                displayOrientation = 90;
            } else if (rotation == Surface.ROTATION_180) {
                displayOrientation = 270;
            } else if (rotation == Surface.ROTATION_270) {
                displayOrientation = 180;
            }
        }

        /**
         * Thread entry point.
         */
        @Override
        public void run() {
            Looper.prepare();

            // We need to create the Handler before reporting ready.
            mHandler = new RenderHandler(this);
            synchronized (mStartLock) {
                mReady = true;
                mStartLock.notify();    // signal waitUntilReady()
            }

            // Prepare EGL and open the camera before we start handling messages.
            mEglCore = new EglCore(null, 0);
            openCamera(REQ_CAMERA_WIDTH, REQ_CAMERA_HEIGHT, REQ_CAMERA_FPS);

            Looper.loop();

            Log.d(TAG, "looper quit");
            releaseCamera();
            releaseGl();
            mEglCore.release();

            synchronized (mStartLock) {
                mReady = false;
            }
        }

        /**
         * Waits until the render thread is ready to receive messages.
         * <p>
         * Call from the UI thread.
         */
        public void waitUntilReady() {
            synchronized (mStartLock) {
                while (!mReady) {
                    try {
                        mStartLock.wait();
                    } catch (InterruptedException ie) { /* not expected */ }
                }
            }
        }

        /**
         * Shuts everything down.
         */
        private void shutdown() {
            Log.d(TAG, "shutdown");
            Looper.myLooper().quit();
        }

        /**
         * Returns the render thread's Handler.  This may be called from any thread.
         */
        public RenderHandler getHandler() {
            return mHandler;
        }

        /**
         * Handles the surface-created callback from SurfaceView.  Prepares GLES and the Surface.
         */
        private void surfaceAvailable(SurfaceHolder holder, boolean newSurface) {
            Surface surface = holder.getSurface();
            mWindowSurface = new WindowSurface(mEglCore, surface, false);
            mWindowSurface.makeCurrent();

            // Create and configure the SurfaceTexture, which will receive frames from the
            // camera.  We set the textured rect's program to render from it.
            mTexProgram = new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT_BW);
            int textureId = mTexProgram.createTextureObject();
            mCameraTexture = new SurfaceTexture(textureId);
            mRect.setTexture(textureId);

            if (!newSurface) {
                // This Surface was established on a previous run, so no surfaceChanged()
                // message is forthcoming.  Finish the surface setup now.
                //
                // We could also just call this unconditionally, and perhaps do an unnecessary
                // bit of reallocating if a surface-changed message arrives.
                mWindowSurfaceWidth = mWindowSurface.getWidth();
                mWindowSurfaceHeight = mWindowSurface.getHeight();
                finishSurfaceSetup();
            }

            mCameraTexture.setOnFrameAvailableListener(this);
        }

        /**
         * Releases most of the GL resources we currently hold (anything allocated by
         * surfaceAvailable()).
         * <p>
         * Does not release EglCore.
         */
        private void releaseGl() {
            GlUtil.checkGlError("releaseGl start");

            if (mWindowSurface != null) {
                mWindowSurface.release();
                mWindowSurface = null;
            }
            if (mTexProgram != null) {
                mTexProgram.release();
                mTexProgram = null;
            }

            GlUtil.checkGlError("releaseGl done");

            mEglCore.makeNothingCurrent();
        }

        /**
         * Handles the surfaceChanged message.
         * <p>
         * We always receive surfaceChanged() after surfaceCreated(), but surfaceAvailable()
         * could also be called with a Surface created on a previous run.  So this may not
         * be called.
         */
        private void surfaceChanged(int width, int height) {
            Log.d(TAG, "RenderThread surfaceChanged " + width + "x" + height);

            mWindowSurfaceWidth = width;
            mWindowSurfaceHeight = height;
            finishSurfaceSetup();
        }

        /**
         * Handles the surfaceDestroyed message.
         */
        private void surfaceDestroyed() {
            // In practice this never appears to be called -- the activity is always paused
            // before the surface is destroyed.  In theory it could be called though.
            Log.d(TAG, "RenderThread surfaceDestroyed");
            releaseGl();
        }

        /**
         * Sets up anything that depends on the window size.
         * <p>
         * Open the camera (to set mCameraAspectRatio) before calling here.
         */
        private void finishSurfaceSetup() {
            int width = mWindowSurfaceWidth;
            int height = mWindowSurfaceHeight;
            Log.d(TAG, "finishSurfaceSetup size=" + width + "x" + height +
                    " camera=" + mCameraPreviewWidth + "x" + mCameraPreviewHeight);

            // Use full window.
            GLES20.glViewport(0, 0, width, height);

            // Simple orthographic projection, with (0,0) in lower-left corner.
            android.opengl.Matrix.orthoM(mDisplayProjectionMatrix, 0, 0, width, 0, height, -1, 0);

            // Default position is center of screen.
            mPosX = width / 2.0f;
            mPosY = height / 2.0f;

            updateGeometry();

            // Ready to go, start the camera.
            Log.d(TAG, "starting camera preview");
            try {
                mCamera.setPreviewTexture(mCameraTexture);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            mCamera.startPreview();
        }

        /**
         * Updates the geometry of mRect, based on the size of the window and the current
         * values set by the UI.
         */
        private void updateGeometry() {
            int width = mWindowSurfaceWidth;
            int height = mWindowSurfaceHeight;

            int smallDim = Math.min(width, height);
            // Max scale is a bit larger than the screen, so we can show over-size.
            float scaled = smallDim * (mSizePercent / 100.0f) * 1.25f;
            float cameraAspect = (float) mCameraPreviewWidth / mCameraPreviewHeight;
            int newWidth = Math.round(scaled * cameraAspect);
            int newHeight = Math.round(scaled);

            float zoomFactor = 1.0f - (mZoomPercent / 100.0f);

            mRect.setScale(!faceCamera ? -newWidth : newWidth, faceCamera ? -newHeight : -newHeight);
            mRect.setPosition(mPosX, mPosY);
            mRect.setRotation(displayOrientation);
            mRectDrawable.setScale(zoomFactor);

            mMainHandler.sendRotation(faceCamera ? rotation : displayOrientation);
        }

        @Override   // SurfaceTexture.OnFrameAvailableListener; runs on arbitrary thread
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            mHandler.sendFrameAvailable();
        }

        /**
         * Handles incoming frame of data from the camera.
         */
        private void frameAvailable() {
            try {
                mCameraTexture.updateTexImage();
                draw();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /**
         * Draws the scene and submits the buffer.
         */
        private void draw() {
            GlUtil.checkGlError("draw start");

            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            mRect.draw(mTexProgram, mDisplayProjectionMatrix);
            mWindowSurface.swapBuffers();

            GlUtil.checkGlError("draw done");
        }

        private void setZoom(int percent) {
            mZoomPercent = percent;
            updateGeometry();
        }

        private void setSize(int percent) {
            mSizePercent = percent;
            updateGeometry();
        }

        private void setRotate(int percent) {
            mRotatePercent = percent;
            updateGeometry();
        }

        private void setPosition(int x, int y) {
            mPosX = x;
            mPosY = mWindowSurfaceHeight - y;   // GLES is upside-down
            updateGeometry();
        }

        public void captureCameraAsync(final CameraViewListener listener) {
            if (takingPhoto && listener != null) {
                listener.repCaptureFailed();
                takingPhoto = false;
                return;
            }

            takingPhoto = true;

            try {
                if (mCamera != null) {
                    mCamera.startPreview();

                    mCamera.takePicture(null, null, new Camera.PictureCallback(){
                        @Override
                        public void onPictureTaken(byte[] data, final Camera camera) {
                            //if (listener != null)
                            //    listener.repCaptureBitmap(ImageUtils.convertPicture(data, rotation, mCameraPreviewWidth, mCameraPreviewHeight, isFaceCamera()));

                            takingPhoto = false;
                            mCamera.startPreview();
                        }
                    });
                } else {
                    if (listener != null) listener.repCaptureBitmap(null);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /**
         * Opens a camera, and attempts to establish preview mode at the specified width
         * and height with a fixed frame rate.
         * <p>
         * Sets mCameraPreviewWidth / mCameraPreviewHeight.
         */
        private void openCamera(int desiredWidth, int desiredHeight, int desiredFps) {
            if (mCamera != null) {
                throw new RuntimeException("camera already initialized");
            }

            Camera.CameraInfo info = new Camera.CameraInfo();

            try {
                if (faceCamera || !hasBackCamera()) {
                    mCamera = Camera.open(getFaceIndexCamera());
                } else {
                    Log.d(TAG, "No front-facing camera found; opening default");
                    mCamera = Camera.open();
                }
            } catch (Exception e) {
                throw new RuntimeException("Unable to open camera");
            }

            Camera.Parameters params = mCamera.getParameters();

            CameraUtils.choosePreviewSize(params, desiredWidth, desiredHeight);
            List<Camera.Size> sizesPicture = params.getSupportedPictureSizes();
            Camera.Size imgSize = getOptimalPreviewSize(sizesPicture, REQ_CAMERA_WIDTH, REQ_CAMERA_WIDTH);
            if(imgSize == null) return;
            params.setPictureSize(imgSize.width, imgSize.height);

            // Try to set the frame rate to a constant value.
            int thousandFps = CameraUtils.chooseFixedPreviewFps(params, desiredFps * 1000);

            // Give the camera a hint that we're recording video.  This can have a big
            // impact on frame rate.
            //parms.setRecordingHint(true);

            mCamera.setParameters(params);

            int[] fpsRange = new int[2];
            Camera.Size mCameraPreviewSize = params.getPreviewSize();
            params.getPreviewFpsRange(fpsRange);
            String previewFacts = mCameraPreviewSize.width + "x" + mCameraPreviewSize.height;
            if (fpsRange[0] == fpsRange[1]) {
                previewFacts += " @" + (fpsRange[0] / 1000.0) + "fps";
            } else {
                previewFacts += " @[" + (fpsRange[0] / 1000.0) +
                        " - " + (fpsRange[1] / 1000.0) + "] fps";
            }
            Log.i(TAG, "Camera config: " + previewFacts);

            mCameraPreviewWidth = mCameraPreviewSize.width;
            mCameraPreviewHeight = mCameraPreviewSize.height;
            mMainHandler.sendCameraParams(mCameraPreviewWidth, mCameraPreviewHeight,
                    thousandFps / 1000.0f);
            mMainHandler.sendIsFaceCamera(faceCamera);
        }

        /**
         * Stops camera preview, and releases the camera to the system.
         */
        private void releaseCamera() {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
                Log.d(TAG, "releaseCamera -- done");
            }
        }

        private void startRecording(File videoFile) {
            if (prepareMediaRecorder(videoFile)) {
                isRecording = true;
                mediaRecorder.start();
            } else {
                // TODO SHOW MESSAGE
                Log.d(TAG, "Recording not started");
            }
        }

        private void stopRecording() {
            if (isRecording) {
                try {
                    mediaRecorder.stop();
                    isRecording = false;
                    releaseMediaRecorder();
                } catch (Exception e) {
                    e.printStackTrace();
                    releaseMediaRecorder();
                }
            }
        }

        private boolean prepareMediaRecorder(File videoFile) {
            if (mediaRecorder == null) {
                mediaRecorder = new MediaRecorder();
            }

            Camera.Size size = getCameraSizes();
            if (size != null) {
                mCamera.lock();
                mCamera.unlock();
                mediaRecorder.setCamera(mCamera);

                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setVideoSize(size.width, size.height);
                mediaRecorder.setVideoEncodingBitRate(2000000);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                mediaRecorder.setAudioEncodingBitRate(96000);
                mediaRecorder.setAudioChannels(2);
                mediaRecorder.setAudioSamplingRate(44100);

                if (displayOrientation == 360 || displayOrientation == 0) {
                    mediaRecorder.setOrientationHint(isFaceCamera() ? 0 : displayOrientation);
                } else {
                    mediaRecorder.setOrientationHint(isFaceCamera() ? 360 - displayOrientation : displayOrientation);
                }

                mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
                mediaRecorder.setMaxDuration(15000); // 15 secs.

                try {
                    mediaRecorder.prepare();
                } catch (IllegalStateException e) {
                    releaseMediaRecorder();
                    e.printStackTrace();
                    return false;
                } catch (IOException e) {
                    releaseMediaRecorder();
                    e.printStackTrace();
                    return false;
                }

                return true;
            } else {
                Log.d(TAG, "ERROR WITH SIZES");
                return false;
            }
        }

        private Camera.Size getCameraSizes() {
            Camera.Size optimalSize = null;

            if (mCamera.getParameters() != null) {
                Camera.Parameters p = mCamera.getParameters();
                List<Camera.Size> videoSizes = p.getSupportedVideoSizes();

                if (videoSizes == null) {
                    optimalSize = getOptimalPreviewSize(p.getSupportedPreviewSizes(), mCameraPreviewWidth, mCameraPreviewHeight);
                } else {
                    p.set("cam_mode", 1);
                    p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

                    final double ASPECT_TOLERANCE = 0.23;
                    double targetRatio = (double) p.getPreviewSize().width / p.getPreviewSize().height;

                    if (videoSizes == null)
                        return null;

                    double minDiff = Double.MAX_VALUE;
                    int targetWidth = 700;

                    // Try to find an size match aspect ratio and size
                    for (Camera.Size size : videoSizes) {
                        double ratio = (double) size.width / size.height;
                        if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                            continue;
                        if (Math.abs(size.width - targetWidth) < minDiff) {
                            optimalSize = size;
                            minDiff = Math.abs(size.width - targetWidth);
                        }
                    }

                    // Cannot find the one match the aspect ratio, ignore the
                    // requirement
                    if (optimalSize == null) {
                        minDiff = Double.MAX_VALUE;
                        for (Camera.Size size : videoSizes) {
                            if (Math.abs(size.width - targetWidth) < minDiff) {
                                optimalSize = size;
                                minDiff = Math.abs(size.width - targetWidth);
                            }
                        }
                    }

                    Log.d("Camera", "Checking size " + optimalSize.width + "w " + optimalSize.height
                            + "h");
                }

                return optimalSize;
            } else {
                Log.d(TAG, "No parameters, camera not available");
                // TODO SEND MESSAGE TO MAIN ACTIVITY
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        if (camera != null)
//                            camera.resume();
//                        Toast.makeText(HomeActivity.this, R.string.overload_video, Toast.LENGTH_LONG).show();
//                    }
//                });
                return null;
            }

        }

        public Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
            final double ASPECT_TOLERANCE = 0.23;
            double targetRatio = (double) h / w;

            if (sizes == null) return null;

            Camera.Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            int targetHeight = h;

            for (Camera.Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }

            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - targetHeight);
                    }
                }
            }
            return optimalSize;
        }

        // VIDEO
        private void releaseMediaRecorder() {
            if (mediaRecorder != null) {
                mediaRecorder.reset();
                //mediaRecorder.release();
                //mediaRecorder = null;
            }
        }

        public boolean isFaceCamera() {
            int cameraId = 0;
            if (faceCamera) cameraId = getFaceIndexCamera();

            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            return cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        public boolean hasFaceCamera() {
            return  getFaceIndexCamera() != 0;
        }

        private int getFaceIndexCamera() {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int cameraIndex = 0; cameraIndex < Camera.getNumberOfCameras(); cameraIndex++) {
                Camera.getCameraInfo(cameraIndex, cameraInfo);

                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    return cameraIndex;
                }
            }
            return 0;
        }

        public boolean hasBackCamera() {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int cameraIndex = 0; cameraIndex < Camera.getNumberOfCameras(); cameraIndex++) {
                Camera.getCameraInfo(cameraIndex, cameraInfo);

                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    return true;
                }
            }
            return false;
        }

        private void toggleCamera() {
            faceCamera = !faceCamera;
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("selfie_camera", faceCamera);
            editor.commit();
        }
    }


    /**
     * Handler for RenderThread.  Used for messages sent from the UI thread to the render thread.
     * <p>
     * The object is created on the render thread, and the various "send" methods are called
     * from the UI thread.
     */
    private static class RenderHandler extends Handler {
        private static final int MSG_SURFACE_AVAILABLE = 0;
        private static final int MSG_SURFACE_CHANGED = 1;
        private static final int MSG_SURFACE_DESTROYED = 2;
        private static final int MSG_SHUTDOWN = 3;
        private static final int MSG_FRAME_AVAILABLE = 4;
        private static final int MSG_POSITION = 5;
        private static final int MSG_REDRAW = 6;
        private static final int MSG_CAPTURE = 7;
        private static final int MSG_START_RECORDING = 8;
        private static final int MSG_STOP_RECORDING = 9;
        private static final int MSG_TOGGLE_CAMERA = 10;


        // This shouldn't need to be a weak ref, since we'll go away when the Looper quits,
        // but no real harm in it.
        private WeakReference<RenderThread> mWeakRenderThread;

        /**
         * Call from render thread.
         */
        public RenderHandler(RenderThread rt) {
            mWeakRenderThread = new WeakReference<RenderThread>(rt);
        }

        /**
         * Sends the "surface available" message.  If the surface was newly created (i.e.
         * this is called from surfaceCreated()), set newSurface to true.  If this is
         * being called during Activity startup for a previously-existing surface, set
         * newSurface to false.
         * <p>
         * The flag tells the caller whether or not it can expect a surfaceChanged() to
         * arrive very soon.
         * <p>
         * Call from UI thread.
         */
        public void sendSurfaceAvailable(SurfaceHolder holder, boolean newSurface) {
            sendMessage(obtainMessage(MSG_SURFACE_AVAILABLE,
                    newSurface ? 1 : 0, 0, holder));
        }

        /**
         * Sends the "surface changed" message, forwarding what we got from the SurfaceHolder.
         * <p>
         * Call from UI thread.
         */
        public void sendSurfaceChanged(@SuppressWarnings("unused") int format, int width,
                                       int height) {
            // ignore format
            sendMessage(obtainMessage(MSG_SURFACE_CHANGED, width, height));
        }

        /**
         * Sends the "shutdown" message, which tells the render thread to halt.
         * <p>
         * Call from UI thread.
         */
        public void sendSurfaceDestroyed() {
            sendMessage(obtainMessage(MSG_SURFACE_DESTROYED));
        }

        /**
         * Sends the "shutdown" message, which tells the render thread to halt.
         * <p>
         * Call from UI thread.
         */
        public void sendShutdown() {
            sendMessage(obtainMessage(MSG_SHUTDOWN));
        }

        /**
         * Sends the "frame available" message.
         * <p>
         * Call from UI thread.
         */
        public void sendFrameAvailable() {
            sendMessage(obtainMessage(MSG_FRAME_AVAILABLE));
        }
        /**
         * Sends the "position" message.  Sets the position of the rect.
         * <p>
         * Call from UI thread.
         */
        public void sendPosition(int x, int y) {
            sendMessage(obtainMessage(MSG_POSITION, x, y));
        }

        /**
         * Sends the "redraw" message.  Forces an immediate redraw.
         * <p>
         * Call from UI thread.
         */
        public void sendRedraw() {
            sendMessage(obtainMessage(MSG_REDRAW));
        }

        public void sendCapture(CameraViewListener listener) {
            sendMessage(obtainMessage(MSG_CAPTURE, listener));
        }

        public void sendStartRecording(File videoFile) {
            sendMessage(obtainMessage(MSG_START_RECORDING, videoFile));
        }

        public void sendStopRecording() {
            sendMessage(obtainMessage(MSG_STOP_RECORDING));
        }

        public void sendToggleCamera() {
            sendMessage(obtainMessage(MSG_TOGGLE_CAMERA));
        }

        @Override  // runs on RenderThread
        public void handleMessage(Message msg) {
            int what = msg.what;
            //Log.d(TAG, "RenderHandler [" + this + "]: what=" + what);

            RenderThread renderThread = mWeakRenderThread.get();
            if (renderThread == null) {
                Log.w(TAG, "RenderHandler.handleMessage: weak ref is null");
                return;
            }

            switch (what) {
                case MSG_SURFACE_AVAILABLE:
                    renderThread.surfaceAvailable((SurfaceHolder) msg.obj, msg.arg1 != 0);
                    break;
                case MSG_SURFACE_CHANGED:
                    renderThread.surfaceChanged(msg.arg1, msg.arg2);
                    break;
                case MSG_SURFACE_DESTROYED:
                    renderThread.surfaceDestroyed();
                    break;
                case MSG_SHUTDOWN:
                    renderThread.shutdown();
                    break;
                case MSG_FRAME_AVAILABLE:
                    renderThread.frameAvailable();
                    break;
                case MSG_CAPTURE:
                    renderThread.captureCameraAsync((CameraViewListener) msg.obj);
                    break;
                case MSG_POSITION:
                    renderThread.setPosition(msg.arg1, msg.arg2);
                    break;
                case MSG_REDRAW:
                    renderThread.draw();
                    break;
                case MSG_START_RECORDING:
                    renderThread.startRecording((File) msg.obj);
                    break;
                case MSG_STOP_RECORDING:
                    renderThread.stopRecording();
                    break;
                case MSG_TOGGLE_CAMERA:
                    renderThread.toggleCamera();
                    break;
                default:
                    throw new RuntimeException("unknown message " + what);
            }
        }
    }

    public interface CameraViewListener {
        public void repCaptureBitmap(Bitmap bitmap);
        public void repCaptureFailed();
    }
}