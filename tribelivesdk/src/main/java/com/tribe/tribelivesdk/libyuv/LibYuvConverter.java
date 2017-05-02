package com.tribe.tribelivesdk.libyuv;

public class LibYuvConverter {

  static {
    System.loadLibrary("hello-jni");
  }

  public native String stringFromJNI();

  public native String unimplementedStringFromJNI();
}
