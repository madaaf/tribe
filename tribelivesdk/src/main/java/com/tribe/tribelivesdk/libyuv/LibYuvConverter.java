package com.tribe.tribelivesdk.libyuv;

@SuppressWarnings("JniMissingFunction") public class LibYuvConverter {

  static {
    System.loadLibrary("libyuv");
  }

  private byte[] mBgr;

  public void rgbToYuv(byte[] rgb, int width, int height, byte[] yuv) {
    if (mBgr == null || mBgr.length < rgb.length) {
      mBgr = new byte[rgb.length];
    }

    rgbToBgrInternal(rgb, width, height, mBgr);
    bgrToYuvInternal(mBgr, width, height, yuv);
  }

  private native void rgbToBgrInternal(byte[] rgb, int width, int height, byte[] bgr);

  private native void bgrToYuvInternal(byte[] bgr, int width, int height, byte[] yuv);
}
