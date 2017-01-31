package com.tribe.app.presentation.view.camera.interfaces;

/**
 * Created by tiago on 03/08/2016.
 */
public interface AudioVisualizerCallback {

  void receive(final double[]... toTransform);

  void activate();

  void deactivate();

  void startRecording();

  void stopRecording();
}
