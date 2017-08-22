package com.tribe.tribelivesdk.libyuv;

import java.nio.ByteBuffer;

public class LibYuvConverter {

  private static LibYuvConverter instance;

  public static LibYuvConverter getInstance() {
    if (instance == null) {
      instance = new LibYuvConverter();
    }

    return instance;
  }

  static {
    System.loadLibrary("tribelibyuv");
  }

  public native int YUVToARGB(byte[] yuv, int width, int height, byte[] argb);

  public native void ABGRToYUV(byte[] argb, int width, int height, byte[] yuvOut);

  public native void ARGBToYUV(byte[] argb, int width, int height, byte[] yuvOut);

  public native void ARGBToI420(byte[] argb, int width, int height, byte[] yuvOut);

  public static native void nativeCopyPlane(ByteBuffer src, int width, int height, int srcStride,
      ByteBuffer dst, int dstStride);

  public native void initPBO(int width, int height);

  public native void releasePBO();

  public native void readFromPBO(ByteBuffer buffer, int width, int height);

  public native void readFromFBO(ByteBuffer buffer, int width, int height);
}
