package com.tribe.tribelivesdk.view.opengl.gles;

public interface Texture {

  int getTextureId();

  void release();

  int getTextureTarget();

  long getTimestamp();
}
