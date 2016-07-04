/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * A SurfaceView that resizes itself to match a specified aspect ratio.
 */
public class VideoTextureView extends TextureView {

    /**
     * The surface view will not resize itself if the fractional difference between its default
     * aspect ratio and the aspect ratio of the video falls below this threshold.
     * <p>
     * This tolerance is useful for fullscreen playbacks, since it ensures that the surface will
     * occupy the whole of the screen when playing content that has the same (or virtually the same)
     * aspect ratio as the device. This typically reduces the number of view layers that need to be
     * composited by the underlying system, which can help to reduce power consumption.
     */
    private static final float MAX_ASPECT_RATIO_DEFORMATION_PERCENT = 0;

    private float videoWidth;
    private float videoHeight;

    public VideoTextureView(Context context) {
        super(context);
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Set the video / height that this {@link VideoTextureView} should satisfy.
     *
     * @param videoWidth The width of the video.
     * @param videoHeight The height of the video
     */
    public void setVideoWidthHeight(float videoWidth, float videoHeight) {
        if (this.videoWidth != videoWidth
                && this.videoHeight != videoHeight) {
            this.videoHeight = videoHeight;
            this.videoWidth = videoWidth;

            int viewWidth = getWidth();
            int viewHeight = getHeight();
            double aspectRatio = (double) videoHeight / videoWidth;

            int newWidth, newHeight;
            if (viewHeight > (int) (viewWidth * aspectRatio)) {
                // limited by narrow width; restrict height
                newWidth = viewWidth;
                newHeight = (int) (viewWidth * aspectRatio);

                if (newHeight < viewHeight) {
                    newWidth = (int) (newWidth * ((float) viewHeight / newHeight));
                    newHeight = (int) (newHeight * ((float) viewHeight / newHeight));
                }
            } else {
                // limited by short height; restrict width
                newWidth = (int) (viewHeight / aspectRatio);
                newHeight = viewHeight;

                if (newWidth < viewWidth) {
                    newWidth = (int) (newWidth * ((float) viewWidth / newWidth));
                    newHeight = (int) (newHeight * ((float) viewWidth / newWidth));
                }
            }
            int xoff = (viewWidth - newWidth) / 2;
            int yoff = (viewHeight - newHeight) / 2;

            Matrix txform = new Matrix();
            getTransform(txform);

            float scaleWidth = (float) newWidth / viewWidth;
            float scaleHeight =  (float) newHeight / viewHeight;
            if (scaleWidth < 1 && newWidth < viewWidth) {
                float scaleDiff = 1 - scaleWidth;
                scaleWidth = 1 + scaleDiff;
                scaleHeight = scaleHeight + scaleDiff;
            }

            txform.setScale(scaleWidth, scaleHeight);
            txform.postTranslate(xoff, yoff);
            setTransform(txform);
        }
    }
}