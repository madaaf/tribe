package com.tribe.tribelivesdk.view.opengl.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;

/**
 * Created by laputan on 16/10/31.
 */
public class ImgSdk {
  private static Context instance;

  /**
   * Must be called in your application onCreate()!
   *
   * @param application your application
   */
  public static void init(Context application) {
    //Workaround Bugfix!!!, it prevents from crashing on Android 4.0.x
    try {
      Class.forName("android.os.AsyncTask");
    } catch (Throwable ignored) {
    }

    instance = application;
  }

  /**
   * Get the Application Context
   *
   * @return The application context
   */
  @NonNull public static Context getAppContext() {
    if (instance == null) {
      throw new RuntimeException("Please Call ImgSdk init() in Application onCreate");
    }
    return instance;
  }

  /**
   * Get the resources object.
   *
   * @return Application resource reference
   */
  @NonNull public static Resources getAppResource() {
    if (instance != null) {
      return instance.getResources();
    } else {
      Log.e("ImgSdk", "Please Call ImgSdk init() in Application onCreate");
      //throw new RuntimeException("Please Call ImgSdk init() in Application onCreate");
      return Resources.getSystem();
    }
  }
}
