package com.tribe.app.presentation.view.camera.helper;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.tribe.app.presentation.view.camera.utils.AspectRatio;
import com.tribe.app.presentation.view.camera.utils.Constants;
import com.tribe.app.presentation.view.camera.utils.Size;
import com.tribe.app.presentation.view.camera.utils.SizeMap;
import com.tribe.app.presentation.view.camera.utils.SurfaceInfo;
import com.tribe.app.presentation.view.utils.CameraUtils;
import com.tribe.app.presentation.view.utils.Degrees;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Helper implements CameraHelper {

    private static final String TAG = "Camera2Helper";

    private static final SparseIntArray INTERNAL_FACINGS = new SparseIntArray();

    private Context context;
    private final CameraManager cameraManager;
    private String cameraId;
    private CameraCharacteristics cameraCharacteristics;
    private CameraDevice camera;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private ImageReader imageReader;
    private SurfaceInfo surfaceInfo = new SurfaceInfo();
    private SizeMap previewSizes = new SizeMap();
    private SizeMap pictureSizes = new SizeMap();
    private AspectRatio aspectRatio;
    private Size previewSize;
    private Size pictureSize;
    private int mFacing;

    public Camera2Helper(final Context context) {
        this.context = context;
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    protected final Context getContext() {
        return context;
    }

    protected final CameraDevice getCamera() {
        return camera;
    }

    protected final void setCamera(final CameraDevice camera) {
        this.camera = camera;
    }

    protected final void setCameraId(final String cameraId) {
        this.cameraId = cameraId;
    }

    private final CameraDevice.StateCallback mCameraDeviceCallback
            = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Camera2Helper.this.camera = camera;
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Camera2Helper.this.camera = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "onError: " + camera.getId() + " (" + error + ")");
            Camera2Helper.this.camera = null;
        }
    };

    private final CameraCaptureSession.StateCallback sessionCallback
            = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            if (camera == null) {
                return;
            }
            captureSession = session;
            //updateAutoFocus();
            //updateFlash();
            try {
                captureSession.setRepeatingRequest(previewRequestBuilder.build(),
                        captureCallback, null);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to start camera preview.", e);
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "Failed to configure capture session.");
        }

        @Override
        public void onClosed(@NonNull CameraCaptureSession session) {
            captureSession = null;
        }

    };

    private PictureCaptureCallback captureCallback = new PictureCaptureCallback() {

        @Override
        public void onPrecaptureRequired() {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            setState(STATE_PRECAPTURE);
            try {
                captureSession.capture(previewRequestBuilder.build(), this, null);
                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                        CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to run precapture sequence.", e);
            }
        }

        @Override
        public void onReady() {
            captureStillPicture();
        }

    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            try (Image image = reader.acquireNextImage()) {
                Image.Plane[] planes = image.getPlanes();
                if (planes.length > 0) {
                    ByteBuffer buffer = planes[0].getBuffer();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                }
            }
        }

    };

    @Override
    public final boolean isOpened() {
        return this.camera != null;
    }

    @Override
    public void nextCamera() {
        //openCamera((cameraId + 1) % getNumberOfCameras());
    }

    @Override
    public int getNextCamera() {
        //return (cameraId + 1) % getNumberOfCameras();
        return 0;
    }

    @Override
    public final void releaseCamera() {
        if (camera != null) {
            stopPreview();
            if (camera != null) {
                camera.close();
                camera = null;
            }
            if (imageReader != null) {
                imageReader.close();
                imageReader = null;
            }
        }
    }

    @Override
    public void setErrorCallback(final Camera.ErrorCallback cb) {

    }

    @Override
    public void setupOptimalSizes(int measureWidth, int measureHeight, int maxSize) {

    }

    @Override
    public void setPreviewTexture(final SurfaceTexture surfaceTexture, int width, int height) throws IOException {
        surfaceInfo.configure(surfaceTexture, width, height);
    }

    public void setPreviewTexture(final SurfaceTexture surfaceTexture) {
        surfaceInfo.setSurface(surfaceTexture);
    }

    private AspectRatio chooseAspectRatio() {
        AspectRatio r = null;
        for (AspectRatio ratio : previewSizes.ratios()) {
            r = ratio;
            if (ratio.equals(Constants.ASPECT_RATIO_16_9)) {
                return ratio;
            }
        }
        return r;
    }

    /**
     * Chooses the optimal preview size based on {@link #previewSizes} and {@link #surfaceInfo}.
     *
     * @return The picked size for camera preview.
     */
    private Size chooseOptimalSize() {
        int surfaceLonger, surfaceShorter;
        if (surfaceInfo.getWidth() < surfaceInfo.getHeight()) {
            surfaceLonger = surfaceInfo.getHeight();
            surfaceShorter = surfaceInfo.getWidth();
        } else {
            surfaceLonger = surfaceInfo.getWidth();
            surfaceShorter = surfaceInfo.getHeight();
        }
        SortedSet<Size> candidates = previewSizes.sizes(aspectRatio);
        // Pick the smallest of those big enough.
        for (Size size : candidates) {
            if (size.getWidth() >= surfaceLonger && size.getHeight() >= surfaceShorter) {
                return size;
            }
        }
        // If no size is big enough, pick the largest one.
        return candidates.last();
    }

    @Override
    public void startPreview() {
        startCaptureSession();
    }

    @Override
    public final void stopPreview() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
    }

    /**
     * Starts a capture session for camera preview.
     *
     * <p>This rewrites {@link #previewRequestBuilder}.</p>
     *
     * <p>The result will be continuously processed in {@link #sessionCallback}.</p>
     */
    private void startCaptureSession() {
        if (!isOpened() || surfaceInfo.getSurface() == null) {
            return;
        }

        previewSize = chooseOptimalSize();
        surfaceInfo.getSurface().setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        Surface surface = new Surface(surfaceInfo.getSurface());
        try {
            previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);
            camera.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
                    sessionCallback, null);
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to start camera session");
        }
    }

    /**
     * Captures a still picture.
     */
    private void captureStillPicture() {
        try {
            CaptureRequest.Builder captureRequestBuilder = camera.createCaptureRequest(
                    CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.addTarget(imageReader.getSurface());
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    previewRequestBuilder.get(CaptureRequest.CONTROL_AF_MODE));
//            switch (flash) {
//                case Constants.FLASH_OFF:
//                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
//                            CaptureRequest.CONTROL_AE_MODE_ON);
//                    captureRequestBuilder.set(CaptureRequest.FLASH_MODE,
//                            CaptureRequest.FLASH_MODE_OFF);
//                    break;
//                case Constants.FLASH_ON:
//                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
//                            CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
//                    break;
//                case Constants.FLASH_TORCH:
//                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
//                            CaptureRequest.CONTROL_AE_MODE_ON);
//                    captureRequestBuilder.set(CaptureRequest.FLASH_MODE,
//                            CaptureRequest.FLASH_MODE_TORCH);
//                    break;
//                case Constants.FLASH_AUTO:
//                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
//                            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//                    break;
//                case Constants.FLASH_RED_EYE:
//                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
//                            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//                    break;
//            }
//            // Calculate JPEG orientation.
//            @SuppressWarnings("ConstantConditions")
//            int sensorOrientation = cameraCharacteristics.get(
//                    CameraCharacteristics.SENSOR_ORIENTATION);
//            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION,
//                    (sensorOrientation +
//                            displayOrientation * (mFacing == Constants.FACING_FRONT ? 1 : -1) +
//                            360) % 360);
//            // Stop preview and capture a still picture.
//            mCaptureSession.stopRepeating();
//            mCaptureSession.capture(captureRequestBuilder.build(),
//                    new CameraCaptureSession.CaptureCallback() {
//                        @Override
//                        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
//                                                       @NonNull CaptureRequest request,
//                                                       @NonNull TotalCaptureResult result) {
//                            unlockFocus();
//                        }
//                    }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Cannot capture a still picture.", e);
        }
    }

    @Override
    public void takePicture(Camera.PictureCallback callback) {
        //takePicture(callback);
    }

    protected final void setPictureCallback(final Camera.PictureCallback callback) {
    }

    //////////////////////////////////////////////////////////////////////////

    @Override
    public final void setPictureFormat(final int format) {

    }

    @Override
    public Size getPreviewSize() {
        return previewSize;
    }

    @Override
    public void takePicture(final Camera.PictureCallback callback, final boolean autoFocus) {

    }

    @Override
    public final void cancelAutoFocus() {
        //getCamera().cancelAutoFocus();
    }

    protected void takePicture(final Camera camera) {

    }

    @Override
    public String getFlashMode() {
        return null;
    }

    @Override
    public String getFocusMode() {
        return null;
    }

    @Override
    public List<String> getSupportedFlashModes() {
        return null;
    }

    @Override
    public List<String> getSupportedFocusModes() {
        return null;
    }

    @Override
    public List<String> getSupportedFlashModes(final String... values) {
        return null;
    }

    @Override
    public List<String> getSupportedFocusModes(final String... values) {
        return null;
    }

    private static List<String> getContainsList(final List<String> list, final String... values) {
        if (list == null) {
            return null;
        }

        final ArrayList<String> results = new ArrayList<String>();
        for (final String value : values) {
            if (list.contains(value)) {
                results.add(value);
            }
        }
        if (results.isEmpty()) {
            return null;
        }
        return results;
    }

    @Override
    public void setFlashMode(final String value) {

    }

    @Override
    public void setFocusMode(final String value) {

    }

    @Override
    public String switchFlashMode() {
        return switchFlashMode(getSupportedFlashModes());
    }

    @Override
    public String switchFlashMode(final String... values) {
        return switchFlashMode(getSupportedFlashModes(values));
    }

    private String switchFlashMode(final List<String> list) {
        final String value = getNextValue(list, getFlashMode());
        if (value != null) {
            setFlashMode(value);
        }
        return value;
    }

    @Override
    public String switchFocusMode() {
        return switchFocusMode(getSupportedFocusModes());
    }

    @Override
    public String switchFocusMode(final String... values) {
        return switchFocusMode(getSupportedFocusModes(values));
    }

    private String switchFocusMode(final List<String> list) {
        final String value = getNextValue(list, getFocusMode());
        if (value != null) {
            setFocusMode(value);
        }
        return value;
    }

    private static String getNextValue(final List<String> list, final String value) {
        if (list != null && list.size() > 1) {
            final int index = list.indexOf(value);
            final String result;
            if (index != -1) {
                result = list.get((index + 1) % list.size());
            } else {
                result = list.get(0);
            }
            return result;
        }
        return null;
    }

    @Override
    public void setDisplayOrientation(final int degrees) {

    }

    @Override
    public void setRotation(int degrees) {

    }

    @Override
    public int getOrientation() {
        final WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();

        final int degrees;
        switch (display.getRotation()) {
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_0:
            default:
                degrees = 0;
                break;
        }

        int result;
        final CameraHelper.CameraInfoCompat info = getCameraInfo();
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
//			result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    @Override
    public int getOptimalOrientation() {
        final WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();

        final int degrees;
        switch (display.getRotation()) {
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_0:
            default:
                degrees = 0;
                break;
        }

        int result;
        final CameraHelper.CameraInfoCompat info = getCameraInfo();
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    @Override
    public int getPreviewOrientation() {
        final CameraHelper.CameraInfoCompat info = getCameraInfo();
        final int deviceOrientation = Degrees.getDisplayRotation(context);
        int displayOrientationVideo = Degrees.getDisplayOrientation(
                info.orientation, deviceOrientation, info.facing == CameraHelper.CameraInfoCompat.CAMERA_FACING_FRONT);
        Log.d("CameraFragment", String.format("Orientations: Sensor = %d˚, Device = %d˚, Display = %d˚",
                info.orientation, deviceOrientation, displayOrientationVideo));

        int previewOrientation = 0;

        if (CameraUtils.isArcWelder()) {
            previewOrientation = 0;
        } else {
            previewOrientation = displayOrientationVideo;
            if (Degrees.isPortrait(deviceOrientation) && info.facing == CameraHelper.CameraInfoCompat.CAMERA_FACING_FRONT)
                previewOrientation = Degrees.mirror(displayOrientationVideo);
        }

        return previewOrientation;
    }

    @Override
    public void setPreviewCallback(final Camera.PreviewCallback cb) {

    }

    @Override
    public void onPreviewFrame(final Camera.PreviewCallback cb) {

    }

    @Override
    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    @Override
    public CameraHelper.CameraInfoCompat getCameraInfo() {
        return null;
    }

    @Override
    public final boolean isFaceCamera() {
        return getCameraInfo().facing == CameraHelper.CameraInfoCompat.CAMERA_FACING_FRONT;
    }

    @Override
    public void openCamera(final int cameraId) {
        chooseCameraIdByFacing();
        collectCameraInfo();
        prepareImageReader();
        startOpeningCamera();
    }

    /**
     * Chooses a camera ID by the specified camera facing ({@link #mFacing}).
     *
     * <p>This rewrites {@link #cameraId}, {@link #cameraCharacteristics}, and optionally
     * {@link #mFacing}.</p>
     */
    private void chooseCameraIdByFacing() {
        try {
            int internalFacing = INTERNAL_FACINGS.get(mFacing);
            final String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer internal = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (internal == null) {
                    throw new NullPointerException("Unexpected state: LENS_FACING null");
                }
                if (internal == internalFacing) {
                    cameraId = id;
                    cameraCharacteristics = characteristics;
                    return;
                }
            }
            // Not found
            cameraId = ids[0];
            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            Integer internal = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
            if (internal == null) {
                throw new NullPointerException("Unexpected state: LENS_FACING null");
            }
            for (int i = 0, count = INTERNAL_FACINGS.size(); i < count; i++) {
                if (INTERNAL_FACINGS.valueAt(i) == internal) {
                    mFacing = INTERNAL_FACINGS.keyAt(i);
                    return;
                }
            }
            // The operation can reach here when the only camera device is an external one.
            // We treat it as facing back.
            mFacing = Constants.FACING_BACK;
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to get a list of camera devices", e);
        }
    }

    /**
     * Collects some information from {@link #cameraCharacteristics}.
     *
     * <p>This rewrites {@link #previewSizes}, {@link #pictureSizes}, and optionally,
     * {@link #aspectRatio}.</p>
     */
    private void collectCameraInfo() {
        StreamConfigurationMap map = cameraCharacteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
            throw new IllegalStateException("Failed to get configuration map: " + cameraId);
        }
        previewSizes.clear();
        for (android.util.Size size : map.getOutputSizes(SurfaceTexture.class)) {
            previewSizes.add(new Size(size.getWidth(), size.getHeight()));
        }
        pictureSizes.clear();
        // try to get hi-res output sizes for Marshmellow and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.util.Size[] outputSizes = map.getHighResolutionOutputSizes(ImageFormat.JPEG);
            if (outputSizes != null) {
                for (android.util.Size size : map.getHighResolutionOutputSizes(ImageFormat.JPEG)) {
                    pictureSizes.add(new Size(size.getWidth(), size.getHeight()));
                }
            }
        }
        // fallback camera sizes and lower than Marshmellow
        if (pictureSizes.ratios().size() == 0) {
            for (android.util.Size size : map.getOutputSizes(ImageFormat.JPEG)) {
                pictureSizes.add(new Size(size.getWidth(), size.getHeight()));
            }
        }

        if (!previewSizes.ratios().contains(aspectRatio)) {
            aspectRatio = previewSizes.ratios().iterator().next();
        }
    }

    private void prepareImageReader() {
        Size largest = pictureSizes.sizes(aspectRatio).last();
        imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                ImageFormat.JPEG, /* maxImages */ 2);
        imageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
    }

    /**
     * Starts opening a camera device.
     *
     * <p>The result will be processed in {@link #mCameraDeviceCallback}.</p>
     */
    private void startOpeningCamera() {
        try {
            cameraManager.openCamera(cameraId, mCameraDeviceCallback, null);
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to open camera: " + cameraId, e);
        }
    }


    @Override
    public void initializeFocusMode() {

    }

    /**
     * A {@link CameraCaptureSession.CaptureCallback} for capturing a still picture.
     */
    private static abstract class PictureCaptureCallback
            extends CameraCaptureSession.CaptureCallback {

        public static final int STATE_PREVIEW = 0;
        public static final int STATE_LOCKING = 1;
        public static final int STATE_LOCKED = 2;
        public static final int STATE_PRECAPTURE = 3;
        public static final int STATE_WAITING = 4;
        public static final int STATE_CAPTURING = 5;

        private int mState;

        public void setState(int state) {
            mState = state;
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

        private void process(@NonNull CaptureResult result) {
            switch (mState) {
                case STATE_LOCKING: {
                    Integer af = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (af == null) {
                        break;
                    }
                    if (af == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                            af == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                        Integer ae = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (ae == null || ae == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            setState(STATE_CAPTURING);
                            onReady();
                        } else {
                            setState(STATE_LOCKED);
                            onPrecaptureRequired();
                        }
                    }
                    break;
                }
                case STATE_PRECAPTURE: {
                    Integer ae = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (ae == null || ae == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            ae == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        setState(STATE_WAITING);
                    }
                    break;
                }
                case STATE_WAITING: {
                    Integer ae = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (ae == null || ae != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        setState(STATE_CAPTURING);
                        onReady();
                    }
                    break;
                }
            }
        }

        /**
         * Called when it is ready to take a still picture.
         */
        public abstract void onReady();

        /**
         * Called when it is necessary to run the precapture sequence.
         */
        public abstract void onPrecaptureRequired();

    }
}
