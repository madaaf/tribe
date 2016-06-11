package com.tribe.app.presentation.view.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.tribe.app.presentation.view.camera.helper.CameraHelper;
import com.tribe.app.presentation.view.camera.interfaces.CameraStateListener;
import com.tribe.app.presentation.view.camera.interfaces.CaptureCallback;
import com.tribe.app.presentation.view.camera.interfaces.Preview;

import java.io.IOException;

public class DefaultPreview extends SurfaceView implements Preview, Camera.PictureCallback, GestureDetector.OnGestureListener {

    private static final String TAG = "DefaultPreview";

    private CameraHelper cameraHelper;
    private GestureDetector gestureDetector;
    private CaptureCallback captureCallback;

    public DefaultPreview(final Context context) {
        super(context);
        initialize(context);
    }

    public DefaultPreview(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public DefaultPreview(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    @SuppressWarnings("deprecation")
    private void initialize(final Context context) {
        gestureDetector = new GestureDetector(context, this);

        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void setCameraHelper(final CameraHelper helper) {
        cameraHelper = helper;
    }

    @Override
    public void openCamera() {
    }

    @Override
    public void releaseCamera() {
    }

    @Override
    public void startPreview(final int measurePreviewWidth, final int measurePreviewHeight, final CameraStateListener listener) {
        if (measurePreviewWidth > 0 && measurePreviewHeight > 0) {
            cameraHelper.setupOptimalSizes(measurePreviewWidth, measurePreviewHeight, 0);
        }
        requestLayout();

        cameraHelper.setDisplayOrientation(cameraHelper.getOptimalOrientation());

        try {
            cameraHelper.setPreviewDisplay(getHolder());
        } catch (IOException e) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", e);
            throw new IllegalStateException(e.getMessage(), e);
        }

        cameraHelper.startPreview();

        if (listener != null) {
            listener.startPreview();
        }
    }

    @Override
    public void stopPreview() {
    }

    @Override
    public void takePicture(final CaptureCallback callback) {
        takePicture(callback, true);
    }

    @Override
    public void takePicture(final CaptureCallback callback, final boolean autoFocus) {
        captureCallback = callback;
        cameraHelper.takePicture(this, autoFocus);
    }

    @Override
    public void onPictureTaken(final byte[] data, final Camera camera) {
        cameraHelper.stopPreview();

        final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        if (!captureCallback.onImageCapture(bitmap) && bitmap != null) {
            bitmap.recycle();
        }

        captureCallback = null;
    }


    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }


    @Override
    public boolean onDown(final MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
        return false;
    }

    @Override
    public void onLongPress(final MotionEvent e) {
        //onTap(e);
    }

    @Override
    public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(final MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(final MotionEvent e) {
        return true;
        //return onTap(e);
    }
}