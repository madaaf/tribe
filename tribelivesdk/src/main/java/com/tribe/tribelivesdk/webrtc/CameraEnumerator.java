package com.tribe.tribelivesdk.webrtc;

import java.util.List;
import org.webrtc.CameraEnumerationAndroid;

public interface CameraEnumerator {

  public String[] getDeviceNames();

  public boolean isFrontFacing(String deviceName);

  public boolean isBackFacing(String deviceName);

  public List<CameraEnumerationAndroid.CaptureFormat> getSupportedFormats(String deviceName);

  public CameraCapturer createCapturer(String deviceName,
      CameraVideoCapturer.CameraEventsHandler eventsHandler);
}