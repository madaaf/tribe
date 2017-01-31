package com.tribe.app.presentation.view.camera.gles;

import android.graphics.Bitmap;

public class GlImageBitmapTexture extends GlImageTexture {

  private Bitmap bitmap;
  private final boolean autoRecycle;

  public GlImageBitmapTexture(final Bitmap bitmap) {
    this(bitmap, true);
  }

  public GlImageBitmapTexture(final Bitmap bitmap, final boolean autoRecycle) {
    this.bitmap = bitmap;
    this.autoRecycle = autoRecycle;
  }

  public boolean isAutoRecycle() {
    return autoRecycle;
  }

  @Override public void setup() {
    attachToTexture(bitmap);
  }

  @Override protected void finalize() throws Throwable {
    try {
      if (autoRecycle) {
        dispose();
      }
    } finally {
      super.finalize();
    }
  }

  public void dispose() {
    if (bitmap != null) {
      if (!bitmap.isRecycled()) {
        bitmap.recycle();
      }

      bitmap = null;
    }
  }
}
