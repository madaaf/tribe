package com.tribe.app.presentation.view.camera.gles;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.tribe.app.presentation.view.camera.helper.CameraHelper;

import java.io.IOException;

class PreviewSurfaceHelperBase implements PreviewSurfaceHelper {
    private CameraHelper cameraHelper;

    public PreviewSurfaceHelperBase(final CameraHelper camera) {
        cameraHelper = camera;
    }
    @Override
    public SurfaceView createPushBufferSurfaceViewIfNeed(final Context context) {
        final SurfaceView surface = new SurfaceView(context);
        surface.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surface.setKeepScreenOn(true);
        surface.setWillNotDraw(true);
        return surface;
    }

    @Override
    public void setZOrderMediaOverlay(final SurfaceView surface) {
        surface.setZOrderMediaOverlay(false);
    }

    @Override
    public void setPreviewDisplay(final SurfaceHolder holder) throws IOException {
        cameraHelper.setPreviewDisplay(holder);
    }
}