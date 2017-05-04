package com.tribe.tribelivesdk.libyuv;

public class LibYuvConverter {

  static {
    System.loadLibrary("tribelibyuv");
  }

  public native int yuvToRgb(byte[] yuv, int width, int height, byte[] rgb);

  public native void YUVtoRBG(int[] rgba, byte[] yuv, int width, int height);
}
