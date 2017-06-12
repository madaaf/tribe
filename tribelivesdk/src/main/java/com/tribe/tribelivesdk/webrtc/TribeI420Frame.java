package com.tribe.tribelivesdk.webrtc;

import java.nio.ByteBuffer;
import org.webrtc.VideoRenderer;

public class TribeI420Frame {

  public int width;
  public int height;
  public int[] yuvStrides;
  public ByteBuffer[] yuvPlanes;
  public int rotationDegree;
  public final float[] samplingMatrix;
  public VideoRenderer.I420Frame webRtcI420Frame;

  public TribeI420Frame(int width, int height, int rotationDegree, int[] yuvStrides,
      ByteBuffer[] yuvPlanes) {
    this.width = width;
    this.height = height;
    this.yuvStrides = yuvStrides;
    this.yuvPlanes = yuvPlanes;
    this.rotationDegree = rotationDegree;
    if (rotationDegree % 90 != 0) {
      throw new IllegalArgumentException("Rotation degree not multiple of 90: " + rotationDegree);
    } else {
      this.samplingMatrix = new float[] {
          1.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F,
          1.0F
      };
    }
  }

  public VideoRenderer.I420Frame getWebRtcI420Frame() {
    if (webRtcI420Frame == null) {
      webRtcI420Frame =
          new VideoRenderer.I420Frame(width, height, rotationDegree, yuvStrides, yuvPlanes, 0L);
    }

    return webRtcI420Frame;
  }

  public int rotatedWidth() {
    return this.rotationDegree % 180 == 0 ? this.width : this.height;
  }

  public int rotatedHeight() {
    return this.rotationDegree % 180 == 0 ? this.height : this.width;
  }

  public String toString() {
    return this.width
        + "x"
        + this.height
        + ":"
        + this.yuvStrides[0]
        + ":"
        + this.yuvStrides[1]
        + ":"
        + this.yuvStrides[2];
  }
}
