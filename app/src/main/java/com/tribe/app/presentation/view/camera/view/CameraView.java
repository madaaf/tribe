package com.tribe.app.presentation.view.camera.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.presentation.view.camera.helper.CameraHelper;
import com.tribe.app.presentation.view.camera.helper.CameraHelperBase;
import com.tribe.app.presentation.view.camera.helper.PreviewSurfaceHelper;
import com.tribe.app.presentation.view.camera.helper.PreviewSurfaceHelperBase;
import com.tribe.app.presentation.view.camera.interfaces.CameraStateListener;
import com.tribe.app.presentation.view.camera.interfaces.CaptureCallback;
import com.tribe.app.presentation.view.camera.interfaces.OnErrorListener;
import com.tribe.app.presentation.view.camera.interfaces.Preview;
import com.tribe.app.presentation.view.camera.utils.Size;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class CameraView extends CardView implements TextureView.SurfaceTextureListener {

    private static final String TAG = "CameraView";

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private CameraHelper cameraHelper;
    private PreviewSurfaceHelper previewSurfaceHelper;
    private Preview preview;
    private boolean usePreviewCallback;
    private boolean autoStart = true;
    private boolean previewAlignCenter;
    private CameraStateListener cameraStateListener;
    private OnErrorListener onErrorListener;
    private boolean isSwitching = false;

    public CameraView(final Context context) {
        super(context);
        initialize(context);
    }

    public CameraView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public CameraView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(final Context context) {
        cameraHelper = new CameraHelperBase(context);
        previewSurfaceHelper = new PreviewSurfaceHelperBase(cameraHelper);
        //setPreview(new DefaultPreview(context), false);
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        final int width = r - l;
        final int height = b - t;
        final int count = getChildCount();

        View previewSel = null;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child != null) {
                if (child.equals(preview)) {
                    int childWidth = width - getPaddingLeft() - getPaddingRight();
                    int childHeight = height - getPaddingTop() - getPaddingBottom();

                    if (cameraHelper.isOpened()) {
                        final Size previewSize = cameraHelper.getPreviewSize();
                        if (previewSize != null) {
                            final int previewWidth;
                            final int previewHeight;
                            switch (getResources().getConfiguration().orientation) {
                                case Configuration.ORIENTATION_PORTRAIT:
                                    previewWidth = previewSize.getHeight();
                                    previewHeight = previewSize.getWidth();
                                    break;
                                default:
                                    previewWidth = previewSize.getWidth();
                                    previewHeight = previewSize.getHeight();
                                    break;
                            }
                            final double scale = Math.min((double) childWidth / (double) previewWidth, (double) childHeight / (double) previewHeight);
                            //childWidth = (int) Math.floor(previewWidth * scale);
                            //childHeight = (int) Math.floor(previewHeight * scale);
                        }
                    }

                    final int childLeft;
                    final int childTop;

                    if (previewAlignCenter) {
                        childLeft = (width - childWidth) / 2;
                        childTop = (height - childHeight) / 2;
                    } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        childLeft = (width - childWidth) / 2;
                        childTop = 0;
                    } else {
                        childLeft = 0;
                        childTop = (height - childHeight) / 2;
                    }

                    child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);

                    previewSel = child;
                    break;
                }
            }
        }
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
    }

    @Override
    public void removeView(final View view) {
        super.removeView(view);
    }

    @Override
    public void removeViewAt(final int index) {
        super.removeViewAt(index);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            openCamera(CameraHelper.DEFAULT_CAMERA_ID);
        } catch (final RuntimeException e) {
            if (onErrorListener != null) {
                onErrorListener.onError(OnErrorListener.ERROR_CAMERA_INITIAL_OPEN, e, this);
            } else {
                throw e;
            }
        }
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (!cameraHelper.isOpened()) {
            return;
        }

        if (usePreviewCallback) {
            try {
                previewSurfaceHelper.setPreviewTexture(surface, width, height);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (autoStart) {
            startPreview();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        releaseCamera();
        return true;
    }

    public CameraHelper getCameraHelper() {
        return cameraHelper;
    }

    public Preview getPreview() {
        return preview;
    }

    public void setPreview(final Preview preview) {
        setPreview(preview, preview != null);
    }

    public void setPreview(final Preview preview, final boolean usePreviewCallback) {
        removePreview();

        this.usePreviewCallback = usePreviewCallback;

        if (preview != null) {
            if (preview instanceof TextureView) {
                final TextureView surface = (TextureView) preview;
                surface.setSurfaceTextureListener(this);
                addView(surface, 0);
            } else {
                //throw new IllegalArgumentException();
            }

            this.preview = preview;
            preview.setCameraHelper(cameraHelper);
        }
    }

    public void removePreview() {
        if (preview != null) {
            if (preview instanceof TextureView) {
                final TextureView surface = (TextureView) preview;
                surface.setSurfaceTextureListener(null);
            }

            if (preview instanceof View) {
                final View view = (View) preview;
                removeView(view);
            }

            preview.setCameraHelper(null);
            preview = null;
        }

        usePreviewCallback = false;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(final boolean autoStart) {
        this.autoStart = autoStart;
    }

    public boolean isPreviewAlignCenter() {
        return previewAlignCenter;
    }

    public void setPreviewAlignCenter(final boolean previewAlignCenter) {
        this.previewAlignCenter = previewAlignCenter;
        requestLayout();
    }

    public void setCameraStateListener(final CameraStateListener callback) {
        cameraStateListener = callback;
    }

    public void setOnErrorListener(final OnErrorListener l) {
        onErrorListener = l;
    }

    public void switchCamera() {
        if (!isSwitching) {
            isSwitching = true;
            openCamera(cameraHelper.getNextCamera());
            startPreview();
            subscriptions.add(Observable
                    .timer(1000L, TimeUnit.MILLISECONDS)
                    .onBackpressureBuffer(100)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        isSwitching = false;
                    }));
        }
    }

    private void openCamera(final int cameraId) {
        synchronized (this) {
            cameraHelper.openCamera(cameraId);
            preview.openCamera();

            if (cameraStateListener != null) {
                cameraStateListener.openCamera();
            }
        }
    }

    private void releaseCamera() {
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        stopPreview();

        synchronized (this) {
            if (cameraStateListener != null) {
                cameraStateListener.releaseCamera();
            }

            cameraHelper.releaseCamera();
            preview.releaseCamera();
        }
    }

    public void startPreview() {
        stopPreview();

        synchronized (this) {
            if (cameraHelper.isOpened()) {
                int width = getWidth();
                int height = getHeight();
                preview.startPreview(width, height, cameraStateListener);
            }
        }
    }

    public void stopPreview() {
        synchronized (this) {
            if (preview != null) {
                cameraHelper.stopPreview();
                preview.stopPreview();
            }
        }
    }

    public void capture(final CaptureCallback callback) {
        capture(callback, true);
    }

    public void capture(final CaptureCallback callback, final boolean autoFocus) {
        preview.takePicture(callback, autoFocus);
    }
}