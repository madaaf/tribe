package com.tribe.app.presentation.view.camera.interfaces;

import com.tribe.app.presentation.view.camera.view.CameraView;

public interface OnErrorListener {
    static final int ERROR_UNKNOWN = -1;
    static final int ERROR_CAMERA_INITIAL_OPEN =  0;
    void onError(int error, Exception e, CameraView view);
}