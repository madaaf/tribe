package com.tribe.app.presentation.view.camera.gles;

import com.tribe.app.presentation.view.camera.interfaces.PreviewTexture;

public class GlPreviewTextureFactory {

  public static PreviewTexture newPreviewTexture(final int texName) {
    return new GlSurfaceTexture(texName);
  }

  private GlPreviewTextureFactory() {
  }
}