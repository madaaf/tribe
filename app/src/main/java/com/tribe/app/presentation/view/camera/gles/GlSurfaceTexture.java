package com.tribe.app.presentation.view.camera.gles;

import android.graphics.SurfaceTexture;

import com.tribe.app.presentation.view.camera.helper.CameraHelper;
import com.tribe.app.presentation.view.camera.interfaces.PreviewTexture;
import com.tribe.app.presentation.view.camera.utils.OpenGlUtils;

import java.io.IOException;

final class GlSurfaceTexture implements PreviewTexture, SurfaceTexture.OnFrameAvailableListener {

  private SurfaceTexture surfaceTexture;
  private OnFrameAvailableListener onFrameAvailableListener;

  public GlSurfaceTexture(final int texName) {
    surfaceTexture = new SurfaceTexture(texName);
    surfaceTexture.setOnFrameAvailableListener(this);
  }

  @Override public void setOnFrameAvailableListener(final OnFrameAvailableListener l) {
    onFrameAvailableListener = l;
  }

  @Override public int getTextureTarget() {
    return OpenGlUtils.GL_TEXTURE_EXTERNAL_OES;
  }

  @Override public void setup(final CameraHelper camera) throws IOException {
    camera.setPreviewTexture(surfaceTexture);
  }

  @Override public void updateTexImage() {
    surfaceTexture.updateTexImage();
  }

  @Override public void getTransformMatrix(final float[] mtx) {
    surfaceTexture.getTransformMatrix(mtx);
  }

  @Override public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
    if (onFrameAvailableListener != null) {
      onFrameAvailableListener.onFrameAvailable(this);
    }
  }
}