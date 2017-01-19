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

    /**
     * Stops rendering {@link #videoTrack} and releases the associated acquired
     * resources (if rendering is in progress).
     */
    protected void removeRendererFromVideoTrack() {
        if (videoRenderer != null) {
            videoRenderer.dispose();
            videoRenderer = null;

            getSurfaceViewRenderer().release();

            // Since this PeerView is no longer rendering anything, make sure
            // surfaceViewRenderer displays nothing as well.
            synchronized (layoutSyncRoot) {
                frameHeight = 0;
                frameRotation = 0;
                frameWidth = 0;
            }
            requestSurfaceViewRendererLayout();
        }
    }
}
