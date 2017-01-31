package com.tribe.app.presentation.view.camera.interfaces;

import android.graphics.Bitmap;

public interface CaptureCallback {
  boolean onImageCapture(Bitmap bitmap);
}