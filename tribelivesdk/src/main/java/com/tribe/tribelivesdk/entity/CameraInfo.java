package com.tribe.tribelivesdk.entity;

import android.hardware.Camera;
import org.webrtc.CameraEnumerationAndroid;

/**
 * Created by tiago on 26/06/2017.
 */

public class CameraInfo {

  private CameraEnumerationAndroid.CaptureFormat captureFormat;
  private int frameOrientation;
  private int frameOrientationUlsee;
  private Camera.CameraInfo cameraInfo;

  public CameraInfo(CameraEnumerationAndroid.CaptureFormat captureFormat, int frameOrientation,
      int frameOrientationUlsee, Camera.CameraInfo cameraInfo) {
    this.captureFormat = captureFormat;
    this.frameOrientation = frameOrientation;
    this.frameOrientationUlsee = frameOrientationUlsee;
    this.cameraInfo = cameraInfo;
  }

  public CameraEnumerationAndroid.CaptureFormat getCaptureFormat() {
    return captureFormat;
  }

  public int getFrameOrientation() {
    return frameOrientation;
  }

  public void setFrameOrientation(int frameOrientation) {
    this.frameOrientation = frameOrientation;
  }

  public int getFrameOrientationUlsee() {
    return frameOrientationUlsee;
  }

  public void setFrameOrientationUlsee(int frameOrientationUlsee) {
    this.frameOrientationUlsee = frameOrientationUlsee;
  }

  public Camera.CameraInfo getCameraInfo() {
    return cameraInfo;
  }

  public boolean isFrontFacing() {
    return cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
  }

  public int rotatedWidth() {
    return this.frameOrientation % 180 == 0 ? this.captureFormat.width : this.captureFormat.height;
  }

  public int rotatedHeight() {
    return this.frameOrientation % 180 == 0 ? this.captureFormat.height : this.captureFormat.width;
  }

  public float rotatedRatio() {
    return (float) rotatedWidth() / (float) rotatedHeight();
  }
}
