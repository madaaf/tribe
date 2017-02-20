package com.tribe.tribelivesdk.stream;

import android.support.annotation.NonNull;
import com.tribe.tribelivesdk.core.MediaConstraints;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.webrtc.CameraEnumerationAndroid;

public class CaptureFormatConstraints {
  private final List<CameraEnumerationAndroid.CaptureFormat> captureFormats;

  public CaptureFormatConstraints(@NonNull List<CameraEnumerationAndroid.CaptureFormat> paramList) {
    if (paramList == null) {
      throw new NullPointerException("CaptureFormats can not be null");
    }
    captureFormats = paramList;
  }

  private MediaConstraints generateConstraintsFromCaptureFormats(
      CameraEnumerationAndroid.CaptureFormat captureFormat) {
    MediaConstraints localMediaConstraints = new MediaConstraints();

    if (captureFormat == null) {
      return localMediaConstraints;
    }

    localMediaConstraints = new com.tribe.tribelivesdk.core.MediaConstraints.MediaConstraintsBuilder().maxHeight(
        captureFormat.height)
        .minHeight(captureFormat.height)
        .maxWidth(captureFormat.width)
        .minWidth(captureFormat.width)
        .minFrameRate(captureFormat.framerate.min)
        .maxFrameRate(captureFormat.framerate.max)
        .build();

    return localMediaConstraints;
  }

  private CameraEnumerationAndroid.CaptureFormat getClosestCaptureFormat(final int width,
      final int height) {
    if (captureFormats.isEmpty()) {
      return null;
    }

    return Collections.min(captureFormats,
        new ClosestComparator<CameraEnumerationAndroid.CaptureFormat>(width) {
          int diff(CameraEnumerationAndroid.CaptureFormat captureFormat) {
            return Math.abs(width * height - captureFormat.width * captureFormat.height);
          }
        });
  }

  public CameraEnumerationAndroid.CaptureFormat getCaptureFormatClosestToDimensions(int width,
      int height) {
    return getClosestCaptureFormat(width, height);
  }

  public MediaConstraints getConstraintsClosestToDimensions(int width, int height) {
    return generateConstraintsFromCaptureFormats(getClosestCaptureFormat(width, height));
  }

  private static abstract class ClosestComparator<T> implements Comparator<T> {
    private ClosestComparator(int width) {
    }

    public int compare(T paramT1, T paramT2) {
      return diff(paramT1) - diff(paramT2);
    }

    abstract int diff(T paramT);
  }
}
