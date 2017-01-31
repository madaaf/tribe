package com.tribe.app.presentation.view.camera.helper;

import android.graphics.SurfaceTexture;

import java.io.IOException;

public class PreviewSurfaceHelperBase implements PreviewSurfaceHelper {

  private CameraHelper cameraHelper;

  public PreviewSurfaceHelperBase(final CameraHelper camera) {
    cameraHelper = camera;
  }

  @Override public void setPreviewTexture(SurfaceTexture surfaceTexture, int width, int height)
      throws IOException {
    cameraHelper.setPreviewTexture(surfaceTexture, width, height);
  }
}