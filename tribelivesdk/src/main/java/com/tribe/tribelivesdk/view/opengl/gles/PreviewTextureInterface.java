package com.tribe.tribelivesdk.view.opengl.gles;

import android.graphics.SurfaceTexture;
import rx.Observable;

/**
 * Created by laputan on 16/11/1.
 */
public interface PreviewTextureInterface extends Texture {
  interface OnFrameAvailableListener {
    void onFrameAvailable(PreviewTextureInterface previewTexture);
  }

  void setOnFrameAvailableListener(final OnFrameAvailableListener l);

  void setup();

  void updateTexImage();

  void getTransformMatrix(float[] mtx);

  Observable<SurfaceTexture> onSurfaceTextureReady();
}
