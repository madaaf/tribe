package com.tribe.app.presentation.view.camera.helper;

import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

public interface CameraHelper {

    static final int DEFAULT_CAMERA_ID = 1;

    int getNumberOfCameras();

    int getCameraId();

    public static class CameraInfoCompat {
        public static final int CAMERA_FACING_BACK = 0;
        public static final int CAMERA_FACING_FRONT = 1;

        public int facing;
        public int orientation;
    }

    CameraInfoCompat getCameraInfo();

    boolean isFaceCamera();

    boolean isOpened();

    void openCamera(int cameraId);

    void nextCamera();

    int getNextCamera();

    void initializeFocusMode();

    void releaseCamera();

    void setErrorCallback(Camera.ErrorCallback cb);

    void setupOptimalSizes(int measureWidth, int measureHeight, int maxSize);

    int getOptimalOrientation();

    int getOrientation();

    void setDisplayOrientation(int degrees);

    void setPreviewCallback(Camera.PreviewCallback cb);

    void setPreviewDisplay(SurfaceHolder holder) throws IOException;

    void setPreviewTexture(Object surfaceTexture) throws IOException;

    void startPreview();

    void onPreviewFrame(Camera.PreviewCallback cb);

    void stopPreview();

    void takePicture(Camera.PictureCallback callback);

    void takePicture(Camera.PictureCallback callback, boolean autoFocus);

    void cancelAutoFocus();

    LinkedHashMap<Camera.Size, Camera.Size> getSupportedPreviewSizeAndSupportedPictureSizeMap();

    List<Camera.Size> getSupportedPreviewSizes();

    List<Camera.Size> getSupportedPictureSizes();

    List<Camera.Size> getSupportedVideoSizes();

    Camera.Size getPreviewSize();

    Camera.Size getPictureSize();

    Camera.Size getVideoSize();

    void setPictureFormat(int format);

    String getFlashMode();

    String getFocusMode();

    List<String> getSupportedFlashModes();

    List<String> getSupportedFocusModes();

    List<String> getSupportedFlashModes(String... values);

    List<String> getSupportedFocusModes(String... values);

    void setFlashMode(String value);

    void setFocusMode(String value);

    String switchFlashMode();

    String switchFocusMode();

    String switchFlashMode(String... values);

    String switchFocusMode(String... values);
}
