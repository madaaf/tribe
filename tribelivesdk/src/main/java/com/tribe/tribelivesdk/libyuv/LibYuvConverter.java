package com.tribe.tribelivesdk.libyuv;

import java.nio.ByteBuffer;

public class LibYuvConverter {

  static {
    System.loadLibrary("tribelibyuv");
  }

  public native int YUVToARGB(byte[] yuv, int width, int height, byte[] argb);

  public native void ARGBToYUV(byte[] argb, int width, int height, byte[] yuvOut);

  public native void ARGBToI420(byte[] argb, int width, int height, byte[] yuvOut);

  public static native void nativeCopyPlane(ByteBuffer src, int width, int height, int srcStride,
      ByteBuffer dst, int dstStride);
}
