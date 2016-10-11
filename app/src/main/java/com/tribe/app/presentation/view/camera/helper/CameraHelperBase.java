package com.tribe.app.presentation.view.camera.helper;


import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

public class CameraHelperBase implements CameraHelper, Camera.PictureCallback, Camera.AutoFocusCallback {

    private final Context context;
    private int cameraId;
    private Camera camera;
    private Camera.PictureCallback pictureCallback;
    private byte[] buffer;
    private SurfaceInfo surfaceInfo = new SurfaceInfo();
    private Camera.Parameters cameraParameters;
    private SizeMap previewSizes = new SizeMap();
    private SizeMap pictureSizes = new SizeMap();
    private AspectRatio aspectRatio;
    private Size previewSize;
    private Size pictureSize;

    public CameraHelperBase(final Context context) {
        this.context = context;
    }

    protected final Context getContext() {
        return context;
    }

    protected final Camera getCamera() {
        return camera;
    }

    protected final void setCamera(final Camera camera) {
        this.camera = camera;
    }

    protected final void setCameraId(final int cameraId) {
        this.cameraId = cameraId;
    }

    @Override
    public final boolean isOpened() {
        return this.camera != null;
    }

    @Override
    public void nextCamera() {
        openCamera((cameraId + 1) % getNumberOfCameras());
    }

    @Override
    public int getNextCamera() {
        return (cameraId + 1) % getNumberOfCameras();
    }

    @Override
    public final void releaseCamera() {
        if (camera != null) {
            stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void setErrorCallback(final Camera.ErrorCallback cb) {
        camera.setErrorCallback(cb);
    }

    @Override
    public void setupOptimalSizes(int measureWidth, int measureHeight, int maxSize) {

    }

    @Override
    public void setPreviewTexture(final SurfaceTexture surfaceTexture, int width, int height) throws IOException {
        surfaceInfo.configure(surfaceTexture, width, height);

        if (camera != null) {
            setUpPreview();
            adjustCameraParameters();
        }
    }

    public void setPreviewTexture(final SurfaceTexture surfaceTexture) {
        surfaceInfo.setSurface(surfaceTexture);

        if (camera != null) {
            setUpPreview();
            adjustCameraParameters();
        }
    }

    private void setUpPreview() {
        try {
            camera.setPreviewTexture(surfaceInfo.getSurface());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void adjustCameraParameters() {
        SortedSet<Size> sizes = previewSizes.sizes(aspectRatio);
        if (sizes == null) { // Not supported
            aspectRatio = chooseAspectRatio();
            sizes = previewSizes.sizes(aspectRatio);
        }
        previewSize = chooseOptimalSize(sizes);
        final Camera.Size currentSize = cameraParameters.getPictureSize();
        if (currentSize.width != previewSize.getWidth() || currentSize.height != previewSize.getHeight()) {
            cameraParameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());

            // Largest picture size in this ratio
            SortedSet<Size> sizesPicture = pictureSizes.sizes(aspectRatio);
            if (sizes == null) { // Not supported
                aspectRatio = chooseAspectRatio();
                sizesPicture = pictureSizes.sizes(aspectRatio);
            }

            if (sizesPicture != null && sizesPicture.size() > 0) {
                pictureSize = sizesPicture.last();
                cameraParameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
            }
            camera.setParameters(cameraParameters);
        }
    }

    private AspectRatio chooseAspectRatio() {
        AspectRatio r = null;
        for (AspectRatio ratio : previewSizes.ratios()) {
            r = ratio;
            if (ratio.equals(Constants.DEFAULT_ASPECT_RATIO)) {
                return ratio;
            }
        }
        return r;
    }

    private Size chooseOptimalSize(SortedSet<Size> sizes) {
        if (surfaceInfo.getWidth() == 0 || surfaceInfo.getHeight() == 0) { // Not yet laid out
            return sizes.first(); // Return the smallest size
        }

        int desiredWidth;
        int desiredHeight;

        if (getOptimalOrientation() == 90 || getOptimalOrientation() == 270) {
            desiredWidth = surfaceInfo.getHeight();
            desiredHeight = surfaceInfo.getWidth();
        } else {
            desiredWidth = surfaceInfo.getWidth();
            desiredHeight = surfaceInfo.getHeight();
        }
        Size result = null;
        for (Size size : sizes) { // Iterate from small to large
            if (desiredWidth <= size.getWidth() && desiredHeight <= size.getHeight()) {
                return size;

            }
            result = size;
        }
        return result;
    }

    @Override
    public void startPreview() {
        camera.startPreview();
    }

    @Override
    public final void stopPreview() {
        synchronized (this) {
            if (camera != null) {
                camera.setPreviewCallback(null);
                try {
                    camera.stopPreview();
                } catch (final Exception e) {}	// ignore: tried to stop a non-existent preview
            }
        }
    }

    @Override
    public void takePicture(Camera.PictureCallback callback) {
        //takePicture(callback);
    }


    protected final void setPictureCallback(final Camera.PictureCallback callback) {
        pictureCallback = callback;
    }

    @Override
    public final void onPictureTaken(final byte[] data, final Camera camera) {
        pictureCallback.onPictureTaken(data, camera);
        pictureCallback = null;
    }

    //////////////////////////////////////////////////////////////////////////

    @Override
    public final void setPictureFormat(final int format) {
        final Camera.Parameters params = camera.getParameters();
        params.setPictureFormat(format);
        try {
            camera.setParameters(params);
        } catch (final RuntimeException e) {

        }
    }

    @Override
    public Size getPreviewSize() {
        return previewSize;
    }

    @Override
    public void takePicture(final Camera.PictureCallback callback, final boolean autoFocus) {
        setPictureCallback(callback);

        if (autoFocus) {
            getCamera().autoFocus(this);
        } else {
            takePicture(getCamera());
        }
    }

    @Override
    public final void cancelAutoFocus() {
        getCamera().cancelAutoFocus();
    }

    protected void takePicture(final Camera camera) {
        camera.setPreviewCallback(null);
        System.gc();

        camera.takePicture(null, null, null, this);
    }

    @Override
    public String getFlashMode() {
        return getCamera().getParameters().getFlashMode();
    }

    @Override
    public String getFocusMode() {
        return getCamera().getParameters().getFocusMode();
    }

    @Override
    public List<String> getSupportedFlashModes() {
        return getCamera().getParameters().getSupportedFlashModes();
    }

    @Override
    public List<String> getSupportedFocusModes() {
        return getCamera().getParameters().getSupportedFocusModes();
    }

    @Override
    public List<String> getSupportedFlashModes(final String... values) {
        return getContainsList(getCamera().getParameters().getSupportedFlashModes(), values);
    }

    @Override
    public List<String> getSupportedFocusModes(final String... values) {
        return getContainsList(getCamera().getParameters().getSupportedFocusModes(), values);
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
        final Camera.Parameters params = getCamera().getParameters();
        params.setFlashMode(value);

        try {
            getCamera().setParameters(params);
        } catch (final RuntimeException  e) {}
    }

    @Override
    public void setFocusMode(final String value) {
        final Camera.Parameters params = getCamera().getParameters();
        params.setFocusMode(value);

        try {
            getCamera().setParameters(params);
        } catch (final RuntimeException  e) {}
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
        getCamera().setDisplayOrientation(degrees);
    }

    @Override
    public void setRotation(int degrees) {
        getCamera().getParameters().setRotation(degrees);
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
        final Camera camera = getCamera();
        if (cb != null) {
            try {
                final Size previewSize = this.previewSize;
                final Size pictureSize = this.pictureSize;
                final Camera.Parameters parameters = camera.getParameters();
                buffer = new byte[Math.max(
                        previewSize.getWidth() * previewSize.getHeight() * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8,
                        pictureSize.getWidth() * pictureSize.getHeight() * ImageFormat.getBitsPerPixel(ImageFormat.RGB_565) / 8
                )];
                camera.setPreviewCallbackWithBuffer(cb);
                camera.addCallbackBuffer(buffer);
            } catch (final OutOfMemoryError e) {
                buffer = null;
                camera.setPreviewCallbackWithBuffer(null);
                camera.setOneShotPreviewCallback(cb);
            }
        } else {
            buffer = null;
            camera.setPreviewCallbackWithBuffer(null);
            camera.setPreviewCallback(null);
        }
    }

    @Override
    public void onPreviewFrame(final Camera.PreviewCallback cb) {
        if (buffer != null) {
            getCamera().addCallbackBuffer(buffer);
        } else {
            getCamera().setOneShotPreviewCallback(cb);
        }
    }

    @Override
    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    @Override
    public CameraHelper.CameraInfoCompat getCameraInfo() {
        final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        final CameraHelper.CameraInfoCompat result = new CameraHelper.CameraInfoCompat();
        result.facing = cameraInfo.facing;
        result.orientation = cameraInfo.orientation;
        return result;
    }

    @Override
    public final boolean isFaceCamera() {
        return getCameraInfo().facing == CameraInfoCompat.CAMERA_FACING_FRONT;
    }

    @Override
    public void openCamera(final int cameraId) {
        releaseCamera();

        if (getNumberOfCameras() > 1) {
            setCamera(Camera.open(cameraId));
        } else if (cameraId != DEFAULT_CAMERA_ID) {
            throw new RuntimeException();
        } else {
            setCamera(Camera.open());
        }

        setCameraId(cameraId);
        initializeFocusMode();

        cameraParameters = camera.getParameters();

        // Supported preview sizes
        previewSizes.clear();
        for (Camera.Size size : cameraParameters.getSupportedPreviewSizes()) {
            previewSizes.add(new Size(size.width, size.height));
        }

        // Supported picture sizes;
        pictureSizes.clear();
        for (Camera.Size size : cameraParameters.getSupportedPictureSizes()) {
            pictureSizes.add(new Size(size.width, size.height));
        }

        // AspectRatio
        if (aspectRatio == null) {
            aspectRatio = Constants.ASPECT_RATIO_16_9;
        }

        adjustCameraParameters();

        final WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        camera.setDisplayOrientation(degrees);
    }

    @Override
    public void initializeFocusMode() {
        final List<String> supportedFocusModes = getSupportedFocusModes();

        if (supportedFocusModes != null) {
            if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                final Camera.Parameters parameters = getCamera().getParameters();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                try {
                    getCamera().setParameters(parameters);
                } catch (final RuntimeException e) {}
            } else {
                if (supportedFocusModes != null) {
                    final Camera.Parameters parameters = getCamera().getParameters();
                    if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        try {
                            getCamera().setParameters(parameters);
                        } catch (final RuntimeException e) {}
                    } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                        try {
                            getCamera().setParameters(parameters);
                        } catch (final RuntimeException e) {}
                    }
                }
            }
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }
}
