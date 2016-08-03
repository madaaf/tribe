package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * A SurfaceView that resizes itself to match a specified aspect ratio.
 */
public class VideoTextureView extends ScalableTextureView {

    public VideoTextureView(Context context) {
        super(context);
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VideoTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}