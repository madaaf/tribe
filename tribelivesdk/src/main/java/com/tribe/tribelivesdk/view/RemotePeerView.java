package com.tribe.tribelivesdk.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by tiago on 15/01/2017.
 */

public class RemotePeerView extends PeerView {

    public RemotePeerView(Context context) {
        super(context);
    }

    public RemotePeerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onAttachedToWindow() {
        try {
            // Generally, OpenGL is only necessary while this View is attached
            // to a window so there is no point in having the whole rendering
            // infrastructure hooked up while this View is not attached to a
            // window. Additionally, a memory leak was solved in a similar way
            // on iOS.
            tryAddRendererToVideoTrack();
        } finally {
            super.onAttachedToWindow();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDetachedFromWindow() {
        try {
            // Generally, OpenGL is only necessary while this View is attached
            // to a window so there is no point in having the whole rendering
            // infrastructure hooked up while this View is not attached to a
            // window. Additionally, a memory leak was solved in a similar way
            // on iOS.
            removeRendererFromVideoTrack();
        } finally {
            super.onDetachedFromWindow();
        }
    }
}
