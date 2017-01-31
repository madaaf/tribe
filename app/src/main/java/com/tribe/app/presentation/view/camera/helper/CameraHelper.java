package com.tribe.app.presentation.view.camera.helper;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.tribe.app.presentation.view.camera.utils.Size;

import java.io.IOException;
import java.util.List;

public interface CameraHelper {

  int DEFAULT_CAMERA_ID = 1;

  int getNumberOfCameras();

  class CameraInfoCompat {
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

  void setRotation(int degrees);

  int getPreviewOrientation();

  void setPreviewCallback(Camera.PreviewCallback cb);

  void setPreviewTexture(SurfaceTexture surfaceTexture, int width, int height) throws IOException;

  void setPreviewTexture(SurfaceTexture surfaceTexture) throws IOException;

  void startPreview();

  void onPreviewFrame(Camera.PreviewCallback cb);

  void stopPreview();

  void takePicture(Camera.PictureCallback callback);

  void takePicture(Camera.PictureCallback callback, boolean autoFocus);

  void cancelAutoFocus();

  void setPictureFormat(int format);

  Size getPreviewSize();

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
