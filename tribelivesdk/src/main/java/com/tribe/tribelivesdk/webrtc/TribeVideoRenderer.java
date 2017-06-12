package com.tribe.tribelivesdk.webrtc;

/**
 * Created by tiago on 19/05/2017.
 */

public class TribeVideoRenderer {

  private VideoRenderer videoRenderer;

  public TribeVideoRenderer(VideoRenderer videoRenderer) {
    this.videoRenderer = videoRenderer;
  }

  public void renderFrame(TribeI420Frame frame) {
    videoRenderer.renderFrame(frame);
  }

  public void dispose() {

  }
}
