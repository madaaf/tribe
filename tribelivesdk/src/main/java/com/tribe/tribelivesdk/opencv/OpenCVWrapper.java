package com.tribe.tribelivesdk.opencv;

public class OpenCVWrapper {

  private static OpenCVWrapper instance;

  public static OpenCVWrapper getInstance() {
    if (instance == null) {
      instance = new OpenCVWrapper();
    }

    return instance;
  }

  static {
    System.loadLibrary("tribelibopencv");
  }

  public native boolean addPostIt(byte[] rgbaIn, int frameWidth, int frameHeight, int[] postIt,
      int postItWidth, int postItHeight, float postItScale, float x, float y, byte[] rgbaOut);

  public native boolean flipBeforeSending(byte[] rgbaIn, byte[] rgbaOut, int frameWidth,
      int frameHeight, float scale);
}
