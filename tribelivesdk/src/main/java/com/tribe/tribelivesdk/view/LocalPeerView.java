package com.tribe.tribelivesdk.view;

import android.content.Context;
import android.util.AttributeSet;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

public class LocalPeerView extends PeerView {

  public LocalPeerView(Context context) {
    super(context);
    initVideoRenderer();
  }

  public LocalPeerView(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    initVideoRenderer();
  }

  private void initVideoRenderer() {
    SurfaceViewRenderer surfaceViewRenderer = getSurfaceViewRenderer();
    surfaceViewRenderer.init(null, rendererEvents);
    videoRenderer = new VideoRenderer(surfaceViewRenderer);
    setMirror(true);
  }
}
