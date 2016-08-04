package com.tribe.app.presentation.view.camera.interfaces;

import com.tribe.app.presentation.view.camera.helper.CameraHelper;

public interface Preview {

    void setCameraHelper(CameraHelper helper);

    void openCamera();

    void releaseCamera();

    void startPreview(int measurePreviewWidth, int measurePreviewHeight, CameraStateListener l);

    void stopPreview();

    void takePicture(CaptureCallback callback);

    void takePicture(CaptureCallback callback, boolean autoFocus);

    void startRecording(String friendId, AudioVisualizerCallback visualizerCallback);

    void stopRecording();
}