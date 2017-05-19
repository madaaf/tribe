package com.tribe.tribelivesdk.webrtc;

public abstract interface VideoRenderer {
  public abstract void renderFrame(org.webrtc.VideoRenderer.I420Frame paramI420Frame);
}
