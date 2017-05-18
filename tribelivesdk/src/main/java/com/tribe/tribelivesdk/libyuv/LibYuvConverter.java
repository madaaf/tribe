package com.tribe.tribelivesdk.libyuv;

public class LibYuvConverter {

  static {
    System.loadLibrary("tribelibyuv");
  }

  public native int YUVToARGB(byte[] yuv, int width, int height, byte[] argb);

  public native void ARGBToYUV(byte[] argb, int width, int height, byte[] yuvOut);
}
