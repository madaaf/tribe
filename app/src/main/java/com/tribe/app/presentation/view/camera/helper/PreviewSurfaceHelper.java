package com.tribe.app.presentation.view.camera.helper;

import android.graphics.SurfaceTexture;

import java.io.IOException;

public interface PreviewSurfaceHelper {
    void setPreviewTexture(SurfaceTexture surfaceTexture, int width, int height) throws IOException;
}