package com.tribe.app.presentation.view.camera.gles;

import com.tribe.app.presentation.view.camera.helper.CameraHelper;

public final class PreviewSurfaceHelperFactory {

    public static PreviewSurfaceHelper newPreviewSurfaceHelper(final CameraHelper camera) {
        return new PreviewSurfaceHelperBase(camera);
    }

    private PreviewSurfaceHelperFactory() {}

}