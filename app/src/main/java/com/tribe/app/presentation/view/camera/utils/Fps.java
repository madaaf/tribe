package com.tribe.app.presentation.view.camera.utils;

import android.os.Handler;

public class Fps implements Runnable {

  public interface Callback {
    void onFps(final int fps);
  }

  volatile int frameCount;
  private Thread thread;
  public Callback callback;
  private final Handler handler = new Handler();

  private final Runnable callbackRunner = new Runnable() {
    @Override public void run() {
      callback.onFps(frameCount);
      frameCount = 0;
    }
  };

  public Fps(final Callback callback) {
    if (callback == null) {
      throw new NullPointerException("Callback must not be null");
    }

    this.callback = callback;
  }

  public void start() {
    synchronized (this) {
      stop();
      frameCount = 0;
      thread = new Thread(this);
      thread.start();
    }
  }

  public void stop() {
    synchronized (this) {
      thread = null;
    }
  }

  public void countUp() {
    frameCount++;
  }

  @Override public void run() {
    while (true) {
      try {
        Thread.sleep(1000L);

        synchronized (this) {
          if (thread == null || thread != Thread.currentThread()) {
            break;
          }
        }

        handler.post(callbackRunner);
      } catch (final InterruptedException e) {
        break;
      }
    }
  }
}