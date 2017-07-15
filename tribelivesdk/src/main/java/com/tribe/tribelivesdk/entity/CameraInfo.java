package com.tribe.tribelivesdk.entity;

import android.hardware.Camera;
import org.webrtc.CameraEnumerationAndroid;

/**
 * Created by tiago on 26/06/2017.
 */

public class CameraInfo {

  private CameraEnumerationAndroid.CaptureFormat captureFormat;
  private int frameOrientation;
  private Camera.CameraInfo cameraInfo;

  public CameraInfo(CameraEnumerationAndroid.CaptureFormat captureFormat, int frameOrientation,
      Camera.CameraInfo cameraInfo) {
    this.captureFormat = captureFormat;
    this.frameOrientation = frameOrientation;
    this.cameraInfo = cameraInfo;
  }

  public CameraEnumerationAndroid.CaptureFormat getCaptureFormat() {
    return captureFormat;
  }

  public int getFrameOrientation() {
    return frameOrientation;
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
}
