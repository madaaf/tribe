package com.tribe.app.presentation.view.camera.gles;

public interface Texture {

  int getTexName();

  int getWidth();

  int getHeight();

  void setup();

  void release();
}