package com.tribe.tribelivesdk.opencv;

public class OpenCVWrapper {

  static {
    System.loadLibrary("tribelibopencv");
  }

  public native boolean addPostIt(byte[] rgbaIn, int frameWidth, int frameHeight, int[] postIt,
      int postItWidth, int postItHeight, float postItScale, float x, float y, byte[] rgbaOut);

  public native boolean flipBeforeSending(byte[] rgbaIn, byte[] rgbaOut, int frameWidth, int frameHeight);
}
