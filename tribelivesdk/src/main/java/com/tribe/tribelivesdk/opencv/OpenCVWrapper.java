package com.tribe.tribelivesdk.opencv;

public class OpenCVWrapper {

  static {
    System.loadLibrary("tribelibopencv");
  }

  public native String stringFromJNI();

  public native boolean addPostIt(byte[] rgbaIn, int frameWidth, int frameHeight, int[] postIt,
      int postItWidth, int postItHeight, byte[] rgbaOut);
}
