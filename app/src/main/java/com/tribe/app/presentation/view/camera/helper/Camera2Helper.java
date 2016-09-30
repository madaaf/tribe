package com.tribe.app.presentation.view.camera.helper;


import android.annotation.TargetApi;
import android.os.Build;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Helper {//implements CameraHelper {
//
//    private static final SparseIntArray INTERNAL_FACINGS = new SparseIntArray();
//
//    private final Context context;
//    private final CameraManager cameraManager;
//    private String cameraId;
//    private CameraCharacteristics cameraCharacteristics;
//    private CameraDevice camera;
//    private CameraCaptureSession captureSession;
//    private CaptureRequest.Builder previewRequestBuilder;
//    private ImageReader imageReader;
//    private final SurfaceInfo surfaceInfo = new SurfaceInfo();
//    private final SizeMap previewSizes = new SizeMap();
//    private final SizeMap pictureSizes = new SizeMap();
//    private int facing;
//    private AspectRatio aspectRatio = Constants.DEFAULT_ASPECT_RATIO;
//    private boolean autoFocus;
//    private int flash;
//    private int displayOrientation;
//
//    public Camera2Helper(final Context context) {
//        this.context = context;
//    }
//
//    protected final Context getContext() {
//        return context;
//    }
//
//    static {
//        INTERNAL_FACINGS.put(CameraInfoCompat.CAMERA_FACING_BACK, CameraCharacteristics.LENS_FACING_BACK);
//        INTERNAL_FACINGS.put(CameraInfoCompat.CAMERA_FACING_FRONT, CameraCharacteristics.LENS_FACING_FRONT);
//    }
//
//    protected final CameraDevice getCamera() {
//        return camera;
//    }
//
//    protected final void setCamera(final CameraDevice camera) {
//        this.camera = camera;
//    }
//
//    protected final void setCameraId(final String cameraId) {
//        this.cameraId = cameraId;
//    }
//
//    @Override
//    public final boolean isOpened() {
//        return this.camera != null;
//    }
//
//    @Override
//    public void nextCamera() {
//        openCamera((cameraId + 1) % getNumberOfCameras());
//    }
//
//    @Override
//    public int getNextCamera() {
//        return (cameraId + 1) % getNumberOfCameras();
//    }
//
//    @Override
//    public final void releaseCamera() {
//        if (camera != null) {
//            stopPreview();
//            camera.release();
//            camera = null;
//        }
//    }
//
//    @Override
//    public void setErrorCallback(final Camera.ErrorCallback cb) {
//        camera.setErrorCallback(cb);
//    }
//
//    @Override
//    public void setupOptimalSizes(int measureWidth, int measureHeight, int maxSize) {
//        final List<Size> supportedPreviewSizes = getSupportedPreviewSizes();
//        final List<Size> supportedPictureSizes = getSupportedPictureSizes();
//
//        if (supportedPreviewSizes != null && supportedPictureSizes != null) {
//            int width;
//            int height;
//            if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//                width = measureHeight;
//                height = measureWidth;
//            } else {
//                width = measureWidth;
//                height = measureHeight;
//            }
//
//            final Size pictureSize = supportedPictureSizes.get(0); //getOptimalSize(supportedPictureSizes, width, height, maxSize);
//            if (pictureSize != null) {
//                width = pictureSize.width;
//                height = pictureSize.height;
//            }
//            final Size previewSize = supportedPreviewSizes.get(0); //getOptimalSize(supportedPreviewSizes, measureWidth, measureHeight, maxSize);
//
//            if (previewSize != null && pictureSize != null) {
//                final Camera.Parameters parameters = getCamera().getParameters();
//                parameters.setPreviewSize(previewSize.width, previewSize.height);
//                parameters.setPictureSize(pictureSize.width, pictureSize.height);
//                try {
//                    getCamera().setParameters(parameters);
//                } catch (final RuntimeException e) {
//                    Log.d("Camera", " errror!", e);
//                }
//            }
//        }
//    }
//
//    private static final double ASPECT_TOLERANCE = 0.1D;
//
//    @Override
//    public void setPreviewDisplay(final SurfaceHolder holder) throws IOException {
//        camera.setPreviewDisplay(holder);
//    }
//
//    @Override
//    public void setPreviewTexture(final Object surfaceTexture) throws IOException {
//        getCamera().setPreviewTexture((SurfaceTexture) surfaceTexture);
//    }
//
//    @Override
//    public void startPreview() {
//        camera.startPreview();
//    }
//
//    @Override
//    public final void stopPreview() {
//        synchronized (this) {
//            if (camera != null) {
//                camera.setPreviewCallback(null);
//                try {
//                    camera.stopPreview();
//                } catch (final Exception e) {}	// ignore: tried to stop a non-existent preview
//            }
//        }
//    }
//
//    @Override
//    public void takePicture(Camera.PictureCallback callback) {
//        //takePicture(callback);
//    }
//
//
//    @Override
//    public final void onPictureTaken(final byte[] data, final Camera camera) {
//        pictureCallback.onPictureTaken(data, camera);
//        pictureCallback = null;
//    }
//
//    //////////////////////////////////////////////////////////////////////////
//
//     @Override
//    public void onAutoFocus(boolean success, Camera camera) {
//
//    }
//
//    public static final class CameraSizeComparator implements Comparator<Size> {
//
//        private static final int LOW = 1;
//        private static final int HIGH = -1;
//        private static final int EQUAL = 0;
//
//        @Override
//        public int compare(final Size lhs, final Size rhs) {
//            if (lhs == null && rhs == null) {
//                return EQUAL;
//            }
//            if (lhs == null) {
//                return LOW;
//            }
//            if (rhs == null) {
//                return HIGH;
//            }
//
//            final int lhsSize = lhs.width * lhs.height;
//            final int rhsSize = rhs.width * rhs.height;
//            if (lhsSize < rhsSize) {
//                return LOW;
//            } else if (lhsSize > rhsSize) {
//                return HIGH;
//            }
//            return EQUAL;
//        }
//
//    }
//
//    @Override
//    public LinkedHashMap<Size, Size> getSupportedPreviewSizeAndSupportedPictureSizeMap() {
//        final List<Size> previewSizes = getSupportedPreviewSizes();
//        final List<Size> pictureSizes = getSupportedPictureSizes();
//        if (previewSizes == null || pictureSizes == null) {
//            return null;
//        }
//
//        final LinkedHashMap<Size, Size> results = new LinkedHashMap<Size, Size>();
//
//        for (final Size previewSize : previewSizes) {
//            final double previewRatio = (double) previewSize.width / (double) previewSize.height;
//            for (final Size pictureSize : pictureSizes) {
//                final double pictureRatio = (double) pictureSize.width / (double) pictureSize.height;
//                if (Math.abs(previewRatio - pictureRatio) == 0D) {
//                    results.put(previewSize, pictureSize);
//                    break;
//                }
//
//            }
//        }
//
//        if (results.isEmpty()) {
//            return null;
//        }
//        return results;
//    }
//
//    @Override
//    public List<Size> getSupportedVideoSizes() {
//        return null;
//    }
//
//    @Override
//    public final Size getPreviewSize() {
//        return camera.getParameters().getPreviewSize();
//    }
//
//    @Override
//    public final Size getPictureSize() {
//        return camera.getParameters().getPictureSize();
//    }
//
//    @Override
//    public Size getVideoSize() {
//        return camera.getParameters().getSupportedVideoSizes().get(0);
//    }
//
//    @Override
//    public final void setPictureFormat(final int format) {
//        final Camera.Parameters params = camera.getParameters();
//        params.setPictureFormat(format);
//        try {
//            camera.setParameters(params);
//        } catch (final RuntimeException e) {
//
//        }
//    }
//
//    @Override
//    public void takePicture(final Camera.PictureCallback callback, final boolean autoFocus) {
//
//    }
//
//    @Override
//    public final void cancelAutoFocus() {
//        getCamera().cancelAutoFocus();
//    }
//
//    protected void takePicture(final Camera camera) {
//        camera.setPreviewCallback(null);
//        System.gc();
//
//        camera.takePicture(null, null, null, this);
//    }
//
//
//    @Override
//    public List<Size> getSupportedPreviewSizes() {
//        final List<Size> results = getCamera().getParameters().getSupportedPreviewSizes();
//        Collections.sort(results, new CameraSizeComparator());
//        return results;
//    }
//
//    @Override
//    public List<Size> getSupportedPictureSizes() {
//        final List<Size> results = getCamera().getParameters().getSupportedPictureSizes();
//        Collections.sort(results, new CameraSizeComparator());
//        return results;
//    }
//
//    @Override
//    public String getFlashMode() {
//        return getCamera().getParameters().getFlashMode();
//    }
//
//    @Override
//    public String getFocusMode() {
//        return getCamera().getParameters().getFocusMode();
//    }
//
//    @Override
//    public List<String> getSupportedFlashModes() {
//        return getCamera().getParameters().getSupportedFlashModes();
//    }
//
//    @Override
//    public List<String> getSupportedFocusModes() {
//        return getCamera().getParameters().getSupportedFocusModes();
//    }
//
//    @Override
//    public List<String> getSupportedFlashModes(final String... values) {
//        return getContainsList(getCamera().getParameters().getSupportedFlashModes(), values);
//    }
//
//    @Override
//    public List<String> getSupportedFocusModes(final String... values) {
//        return getContainsList(getCamera().getParameters().getSupportedFocusModes(), values);
//    }
//
//    private static List<String> getContainsList(final List<String> list, final String... values) {
//        if (list == null) {
//            return null;
//        }
//
//        final ArrayList<String> results = new ArrayList<String>();
//        for (final String value : values) {
//            if (list.contains(value)) {
//                results.add(value);
//            }
//        }
//        if (results.isEmpty()) {
//            return null;
//        }
//        return results;
//    }
//
//    @Override
//    public void setFlashMode(final String value) {
//        final Camera.Parameters params = getCamera().getParameters();
//        params.setFlashMode(value);
//
//        try {
//            getCamera().setParameters(params);
//        } catch (final RuntimeException  e) {}
//    }
//
//    @Override
//    public void setFocusMode(final String value) {
//        final Camera.Parameters params = getCamera().getParameters();
//        params.setFocusMode(value);
//
//        try {
//            getCamera().setParameters(params);
//        } catch (final RuntimeException  e) {}
//    }
//
//    @Override
//    public String switchFlashMode() {
//        return switchFlashMode(getSupportedFlashModes());
//    }
//
//    @Override
//    public String switchFlashMode(final String... values) {
//        return switchFlashMode(getSupportedFlashModes(values));
//    }
//    private String switchFlashMode(final List<String> list) {
//        final String value = getNextValue(list, getFlashMode());
//        if (value != null) {
//            setFlashMode(value);
//        }
//        return value;
//    }
//
//    @Override
//    public String switchFocusMode() {
//        return switchFocusMode(getSupportedFocusModes());
//    }
//
//    @Override
//    public String switchFocusMode(final String... values) {
//        return switchFocusMode(getSupportedFocusModes(values));
//    }
//
//    private String switchFocusMode(final List<String> list) {
//        final String value = getNextValue(list, getFocusMode());
//        if (value != null) {
//            setFocusMode(value);
//        }
//        return value;
//    }
//
//    private static String getNextValue(final List<String> list, final String value) {
//        if (list != null && list.size() > 1) {
//            final int index = list.indexOf(value);
//            final String result;
//            if (index != -1) {
//                result = list.get((index + 1) % list.size());
//            } else {
//                result = list.get(0);
//            }
//            return result;
//        }
//        return null;
//    }
//
//    @Override
//    public void setDisplayOrientation(final int degrees) {
//        System.out.println("Display Orientation set : " + degrees);
//        getCamera().setDisplayOrientation(degrees);
//    }
//
//    @Override
//    public void setRotation(int degrees) {
//        getCamera().getParameters().setRotation(degrees);
//    }
//
//    @Override
//    public int getOrientation() {
//        final WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//        final Display display = windowManager.getDefaultDisplay();
//
//        final int degrees;
//        switch (display.getRotation()) {
//            case Surface.ROTATION_270:
//                degrees = 270;
//                break;
//            case Surface.ROTATION_180:
//                degrees = 180;
//                break;
//            case Surface.ROTATION_90:
//                degrees = 90;
//                break;
//            case Surface.ROTATION_0:
//            default:
//                degrees = 0;
//                break;
//        }
//
//        int result;
//        final CameraInfoCompat info = getCameraInfo();
//        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            result = (info.orientation + degrees) % 360;
////			result = (360 - result) % 360;  // compensate the mirror
//        } else {  // back-facing
//            result = (info.orientation - degrees + 360) % 360;
//        }
//        return result;
//    }
//
//    @Override
//    public int getOptimalOrientation() {
//        final WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//        final Display display = windowManager.getDefaultDisplay();
//
//        final int degrees;
//        switch (display.getRotation()) {
//            case Surface.ROTATION_270:
//                degrees = 270;
//                break;
//            case Surface.ROTATION_180:
//                degrees = 180;
//                break;
//            case Surface.ROTATION_90:
//                degrees = 90;
//                break;
//            case Surface.ROTATION_0:
//            default:
//                degrees = 0;
//                break;
//        }
//
//        int result;
//        final CameraInfoCompat info = getCameraInfo();
//        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            result = (info.orientation + degrees) % 360;
//            result = (360 - result) % 360;  // compensate the mirror
//        } else {  // back-facing
//            result = (info.orientation - degrees + 360) % 360;
//        }
//
//        System.out.println("OPTIMAL ORIENTATION : " + result);
//
//        return result;
//    }
//
//    @Override
//    public int getPreviewOrientation() {
//        final CameraInfoCompat info = getCameraInfo();
//        final int deviceOrientation = Degrees.getDisplayRotation(context);
//        int displayOrientationVideo = Degrees.getDisplayOrientation(
//                info.orientation, deviceOrientation, info.facing == CameraInfoCompat.CAMERA_FACING_FRONT);
//        Log.d("CameraFragment", String.format("Orientations: Sensor = %d˚, Device = %d˚, Display = %d˚",
//                info.orientation, deviceOrientation, displayOrientationVideo));
//
//        int previewOrientation = 0;
//
//        if (CameraUtils.isArcWelder()) {
//            previewOrientation = 0;
//        } else {
//            previewOrientation = displayOrientationVideo;
//            if (Degrees.isPortrait(deviceOrientation) && info.facing == CameraInfoCompat.CAMERA_FACING_FRONT)
//                previewOrientation = Degrees.mirror(displayOrientationVideo);
//        }
//
//        return previewOrientation;
//    }
//
//    @Override
//    public void setPreviewCallback(final Camera.PreviewCallback cb) {
//        final Camera camera = getCamera();
//        if (cb != null) {
//            try {
//                final Size previewSize = getPreviewSize();
//                final Size pictureSize = getPictureSize();
//                final Camera.Parameters parameters = camera.getParameters();
//                buffer = new byte[Math.max(
//                        previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8,
//                        pictureSize.width * pictureSize.height * ImageFormat.getBitsPerPixel(ImageFormat.RGB_565) / 8
//                )];
//                camera.setPreviewCallbackWithBuffer(cb);
//                camera.addCallbackBuffer(buffer);
//            } catch (final OutOfMemoryError e) {
//                buffer = null;
//                camera.setPreviewCallbackWithBuffer(null);
//                camera.setOneShotPreviewCallback(cb);
//            }
//        } else {
//            buffer = null;
//            camera.setPreviewCallbackWithBuffer(null);
//            camera.setPreviewCallback(null);
//        }
//    }
//
//    @Override
//    public void onPreviewFrame(final Camera.PreviewCallback cb) {
//        if (buffer != null) {
//            getCamera().addCallbackBuffer(buffer);
//        } else {
//            getCamera().setOneShotPreviewCallback(cb);
//        }
//    }
//
//    @Override
//    public int getNumberOfCameras() {
//        return Camera.getNumberOfCameras();
//    }
//
//    @Override
//    public CameraInfoCompat getCameraInfo() {
//        final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//        Camera.getCameraInfo(getCameraId(), cameraInfo);
//
//        final CameraInfoCompat result = new CameraInfoCompat();
//        result.facing = cameraInfo.facing;
//        result.orientation = cameraInfo.orientation;
//        return result;
//    }
//
//    @Override
//    public final boolean isFaceCamera() {
//        return getCameraInfo().facing == CameraInfoCompat.CAMERA_FACING_FRONT;
//    }
//
//    @Override
//    public void openCamera(final int cameraId) {
//        releaseCamera();
//
//        if (getNumberOfCameras() > 1) {
//            setCamera(Camera.open(cameraId));
//        } else if (cameraId != DEFAULT_CAMERA_ID) {
//            throw new RuntimeException();
//        } else {
//            setCamera(Camera.open());
//        }
//
//        setCameraId(cameraId);
//        initializeFocusMode();
//    }
//
//    @Override
//    public void initializeFocusMode() {
//        final List<String> supportedFocusModes = getSupportedFocusModes();
//
//        if (supportedFocusModes != null) {
//            if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
//                final Camera.Parameters parameters = getCamera().getParameters();
//                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//                try {
//                    getCamera().setParameters(parameters);
//                } catch (final RuntimeException e) {}
//            } else {
//                if (supportedFocusModes != null) {
//                    final Camera.Parameters parameters = getCamera().getParameters();
//                    if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
//                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//                        try {
//                            getCamera().setParameters(parameters);
//                        } catch (final RuntimeException e) {}
//                    } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
//                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
//                        try {
//                            getCamera().setParameters(parameters);
//                        } catch (final RuntimeException e) {}
//                    }
//                }
//            }
//        }
//    }
}
