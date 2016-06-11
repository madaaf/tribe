package com.tribe.app.presentation.view.camera.interfaces;

import com.tribe.app.presentation.view.camera.helper.CameraHelper;

import java.io.IOException;

public interface PreviewTexture {

    public interface OnFrameAvailableListener {
        void onFrameAvailable(PreviewTexture previewTexture);
    }

    void setOnFrameAvailableListener(final OnFrameAvailableListener l);

    int getTextureTarget();

    void setup(CameraHelper camera) throws IOException;

    void updateTexImage();

    void getTransformMatrix(float[] mtx);
}