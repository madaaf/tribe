package com.tribe.app.presentation.view.widget.video;

import android.graphics.Matrix;

public class ScaleManager {

    private Size viewSize;
    private Size videoSize;

    public ScaleManager(Size viewSize, Size videoSize) {
        this.viewSize = viewSize;
        this.videoSize = videoSize;
    }

    public Matrix getScaleMatrix(ScalableType scalableType) {
        switch (scalableType) {
            case CENTER_CROP:
                return getCropScale(PivotPoint.CENTER);
            
            default:
                return null;
        }
    }

    private Matrix getMatrix(float sx, float sy, float px, float py) {
        Matrix matrix = new Matrix();
        matrix.setScale(sx, sy, px, py);
        return matrix;
    }

    private Matrix getMatrix(float sx, float sy, PivotPoint pivotPoint) {
        switch (pivotPoint) {
            case CENTER:
                return getMatrix(sx, sy, viewSize.getWidth() / 2f, viewSize.getHeight() / 2f);
            default:
                throw new IllegalArgumentException("Illegal PivotPoint");
        }
    }

    private Matrix getCropScale(PivotPoint pivotPoint) {
        float sx = (float) viewSize.getWidth() / videoSize.getWidth();
        float sy = (float) viewSize.getHeight() / videoSize.getHeight();
        float maxScale = Math.max(sx, sy);
        sx = maxScale / sx;
        sy = maxScale / sy;
        return getMatrix(sx, sy, pivotPoint);
    }
}
