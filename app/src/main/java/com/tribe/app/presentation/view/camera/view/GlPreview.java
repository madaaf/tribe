package com.tribe.app.presentation.view.camera.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.opengl.Matrix;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;

import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.preferences.Filter;
import com.tribe.app.presentation.view.camera.gles.GLES20ConfigChooser;
import com.tribe.app.presentation.view.camera.gles.GLES20ContextFactory;
import com.tribe.app.presentation.view.camera.gles.GlImageBitmapTexture;
import com.tribe.app.presentation.view.camera.gles.GlPreviewTextureFactory;
import com.tribe.app.presentation.view.camera.gles.GlTextureView;
import com.tribe.app.presentation.view.camera.gles.PixelBuffer;
import com.tribe.app.presentation.view.camera.gles.Texture;
import com.tribe.app.presentation.view.camera.helper.CameraHelper;
import com.tribe.app.presentation.view.camera.interfaces.AudioVisualizerCallback;
import com.tribe.app.presentation.view.camera.interfaces.CameraStateListener;
import com.tribe.app.presentation.view.camera.interfaces.CaptureCallback;
import com.tribe.app.presentation.view.camera.interfaces.Preview;
import com.tribe.app.presentation.view.camera.interfaces.PreviewTexture;
import com.tribe.app.presentation.view.camera.recorder.MediaAudioEncoder;
import com.tribe.app.presentation.view.camera.recorder.MediaVideoEncoder;
import com.tribe.app.presentation.view.camera.recorder.TribeMuxerWrapper;
import com.tribe.app.presentation.view.camera.renderer.GLES20FramebufferObject;
import com.tribe.app.presentation.view.camera.renderer.GlFrameBufferObjectRenderer;
import com.tribe.app.presentation.view.camera.shader.GlPreviewShader;
import com.tribe.app.presentation.view.camera.shader.GlRecordPixellateShader;
import com.tribe.app.presentation.view.camera.shader.GlRecordShader;
import com.tribe.app.presentation.view.camera.shader.GlShader;
import com.tribe.app.presentation.view.camera.shader.fx.GlRecordLutShader;
import com.tribe.app.presentation.view.camera.utils.Fps;
import com.tribe.app.presentation.view.camera.utils.OpenGlUtils;
import com.tribe.app.presentation.view.camera.utils.Size;

import java.io.IOException;

import javax.inject.Inject;
import javax.microedition.khronos.egl.EGLConfig;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_MAX_TEXTURE_SIZE;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGetIntegerv;
import static android.opengl.GLES20.glViewport;

public class GlPreview extends GlTextureView implements Preview, Camera.PictureCallback {

    @Inject
    @Filter
    Preference<Integer> filter;

    private CameraHelper cameraHelper;
    private Renderer renderer;
    private TribeMuxerWrapper muxerWrapper;
    private MediaVideoEncoder mediaVideoEncoder;

    boolean faceMirror = true;

    public GlPreview(final Context context) {
        super(context);
        initDependencyInjector();
        initialize(context);
    }

    public GlPreview(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initDependencyInjector();
        initialize(context);
    }

    private void initialize(final Context context) {
        setEGLConfigChooser(new GLES20ConfigChooser(false));
        setEGLContextFactory(new GLES20ContextFactory());

        renderer = new Renderer();
        setRenderer(renderer);

        setRenderMode(RENDERMODE_WHEN_DIRTY);
        setPreserveEGLContextOnPause(true);
    }

    private void initDependencyInjector() {
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);
    }

    public final boolean isFaceMirror() {
        return faceMirror;
    }

    public final void setFaceMirror(final boolean mirror) {
        faceMirror = mirror;
    }

    private GlShader mShader;

    public void setShader(final GlShader shader) {
        mShader = shader;
        queueEvent(() -> renderer.setShader(shader));
    }
    public void setInputTexture(final Texture texture) {
        queueEvent(() -> renderer.setTexture(texture));
    }

    public void setFps(final Fps fps) {
        queueEvent(() -> renderer.setFps(fps));
    }

    @Override
    public void setCameraHelper(final CameraHelper helper) {
        cameraHelper = helper;
    }

    @Override public void openCamera() {}

    @Override public void releaseCamera() {}

    private boolean previewing;

    private int measurePreviewWidth;
    private int measurePreviewHeight;
    private CameraStateListener cameraStateListener;
    private boolean waitingStartPreview;

    @Override
    public void startPreview(final int measurePreviewWidth, final int measurePreviewHeight, final CameraStateListener listener) {
        synchronized (this) {
            this.measurePreviewWidth = measurePreviewWidth;
            this.measurePreviewHeight = measurePreviewHeight;
            this.cameraStateListener = listener;

            if (renderer.mMaxTextureSize != 0) {
                startPreview();
            } else {
                waitingStartPreview = true;
            }
        }
    }

    void onRendererInitialized() {
        if (waitingStartPreview) {
            waitingStartPreview = false;
            startPreview();
        }
    }

    private void startPreview() {
        synchronized (this) {
            previewing = false;

            if (measurePreviewWidth > 0 && measurePreviewHeight > 0) {
                cameraHelper.setupOptimalSizes(measurePreviewWidth, measurePreviewHeight, renderer.mMaxTextureSize);
            }

            requestLayout();
            queueEvent(() -> renderer.onStartPreview());
        }
    }

    void onStartPreviewFinished() {
        synchronized (this) {
            if (!previewing && cameraHelper != null && cameraHelper.isOpened()) {
                cameraHelper.startPreview();
                previewing = true;

                if (cameraStateListener != null) {
                    cameraStateListener.startPreview();
                    cameraStateListener = null;
                }
            }
        }
    }

    @Override
    public void stopPreview() {
        synchronized (this) {
            waitingStartPreview = false;
            previewing = false;
        }
    }

    private CaptureCallback captureCallback;

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
    public void startRecording(String fileId, AudioVisualizerCallback visualizerCallback) {
        if (muxerWrapper == null) {
            try {
                muxerWrapper = new TribeMuxerWrapper(getContext(), fileId);
                if (visualizerCallback == null) {
                    mediaVideoEncoder = new MediaVideoEncoder(getContext(), muxerWrapper);
                    queueEvent(() -> renderer.setMediaVideoEncoder(mediaVideoEncoder));
                }
                new MediaAudioEncoder(getContext(), muxerWrapper, visualizerCallback);
                muxerWrapper.prepare(getContext(), getWidth(), getHeight());
                muxerWrapper.startRecording();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stopRecording() {
        if (muxerWrapper != null) {
            muxerWrapper.stopRecording();
            muxerWrapper = null;
            renderer.setMediaVideoEncoder(null);
        }
    }

    @Override
    public void onPictureTaken(final byte[] data, final Camera camera) {
        cameraHelper.stopPreview();
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Renderer finalRenderer = new Renderer();
        finalRenderer.setShader(mShader);
        PixelBuffer buffer = new PixelBuffer(bitmap.getWidth(), bitmap.getHeight());
        buffer.setRenderer(finalRenderer);
        finalRenderer.setTexture(new GlImageBitmapTexture(bitmap, false));
        bitmap = buffer.getBitmap();
        buffer.destroy();
        onImageCapture(bitmap);
    }


    public void capture(final CaptureCallback callback) {
        captureCallback = callback;
        queueEvent(() -> renderer.capture());
    }

    void onImageCapture(final Bitmap bitmap) {
        if (!captureCallback.onImageCapture(bitmap) && bitmap != null) {
            bitmap.recycle();
        }

        captureCallback = null;
    }

    private final class Renderer extends GlFrameBufferObjectRenderer implements PreviewTexture.OnFrameAvailableListener {

        private static final String TAG = "GLES20Preview.Renderer";

        private final Handler mHandler = new Handler();

        private PreviewTexture mPreviewTexture;
        private boolean mUpdateSurface = false;

        private Texture mImageTexture;
        private boolean mUploadTexture;

        private int mTexName;

        private float[] mMVPMatrix  = new float[16];
        private float[] mProjMatrix = new float[16];
        private float[] mMMatrix    = new float[16];
        private float[] mVMatrix    = new float[16];
        private float[] mSTMatrix   = new float[16];
        private float mCameraRatio  = 1.0f;

        private GLES20FramebufferObject mFramebufferObject;
        private GlPreviewShader mPreviewShader;
        private GlPreviewShader mImageShader;
        private GlRecordShader recordShader;

        private GlShader mShader;
        private boolean mIsNewShader;
        int mMaxTextureSize;

        public Renderer() {
            super();
            Matrix.setIdentityM(mSTMatrix, 0);
        }

        public void setShader(final GlShader shader) {
            if (mShader != null) {
                mShader.release();
            }

            if (shader != null) {
                mIsNewShader = true;
            }

            mShader = shader;
            mIsNewShader = true;
            requestRender();
        }

        public void onStartPreview() {
            Matrix.setIdentityM(mMMatrix, 0);
            Matrix.rotateM(mMMatrix, 0, -cameraHelper.getOptimalOrientation(), 0.0f, 0.0f, 1.0f);
            if (cameraHelper.isFaceCamera() && !faceMirror) {
                Matrix.scaleM(mMMatrix, 0, 1.0f, -1.0f, 1.0f);
            } else {
                Matrix.scaleM(mMMatrix, 0, 0.75f, 0.75f, 1.0f);
            }
            //Matrix.orthoM(mMMatrix, 0, -1.3f, 1.3f, -1, 1, -1, 1);

            final Size previewSize = cameraHelper.getPreviewSize();
            mCameraRatio = (float) previewSize.getWidth() / previewSize.getHeight();

            try {
                mPreviewTexture.setup(cameraHelper);
            } catch (final IOException e) {
                Log.e(TAG, "Cannot set preview texture target!");
            }

            mHandler.post(() -> onStartPreviewFinished());
        }

        public void setTexture(final Texture texture) {
            synchronized (this) {
                if (mImageTexture != null) {
                    mImageTexture.release();
                }
                Matrix.setIdentityM(mMMatrix, 0);
                mImageTexture = texture;
                mUploadTexture = true;
            }
            requestRender();
        }

        public void capture() {
            final Bitmap bitmap;

            if (cameraHelper != null) {
                bitmap = getBitmap(cameraHelper.getOrientation(), cameraHelper.isFaceCamera());
            } else {
                bitmap = getBitmap();
            }

            mHandler.post(() -> onImageCapture(bitmap));
        }

        @Override
        public void onSurfaceCreated(final EGLConfig config) {
            glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

            final int[] args = new int[1];

            glGenTextures(args.length, args, 0);
            mTexName = args[0];

            mPreviewTexture = GlPreviewTextureFactory.newPreviewTexture(mTexName);
            mPreviewTexture.setOnFrameAvailableListener(this);

            glBindTexture(mPreviewTexture.getTextureTarget(), mTexName);
            OpenGlUtils.setupSampler(mPreviewTexture.getTextureTarget(), GL_LINEAR, GL_NEAREST);
            glBindTexture(GL_TEXTURE_2D, 0);

            mFramebufferObject = new GLES20FramebufferObject();
            mPreviewShader = new GlPreviewShader(mPreviewTexture.getTextureTarget());
            mPreviewShader.setup();
            mImageShader = new GlPreviewShader(GL_TEXTURE_2D);
            mImageShader.setup();

            Matrix.setLookAtM(mVMatrix, 0,
                    0.0f, 0.0f, 5.0f,
                    0.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f
            );

            synchronized (this) {
                mUpdateSurface = false;
            }
            if (mImageTexture != null) {
                mUploadTexture = true;
            }
            if (mShader != null) {
                mIsNewShader = true;
            }

            glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0);
            mMaxTextureSize = args[0];

            mHandler.post(() -> onRendererInitialized());
        }

        @Override
        public void onSurfaceChanged(final int width, final int height) {
            mFramebufferObject.setup(width, height);
            mPreviewShader.setFrameSize(width, height);
            mImageShader.setFrameSize(width, height);
            if (mShader != null) {
                mShader.setFrameSize(width, height);
            }

            final float aspectRatio = (float) width / height;
            Matrix.frustumM(mProjMatrix, 0, -aspectRatio, aspectRatio, -1, 1, 5, 7);
        }

        @Override
        public void onDrawFrame(final GLES20FramebufferObject fbo) {
            synchronized (this) {
                if (mUpdateSurface) {
                    mPreviewTexture.updateTexImage();
                    mPreviewTexture.getTransformMatrix(mSTMatrix);
                    mUpdateSurface = false;
                }
            }

            if (mUploadTexture) {
                mImageTexture.setup();
                mCameraRatio = (float) mImageTexture.getWidth() / mImageTexture.getHeight();
                Matrix.setIdentityM(mSTMatrix, 0);
                mUploadTexture = false;
            }

            if (mIsNewShader) {
                if (mShader != null) {
                    mShader.setup();
                    mShader.setFrameSize(fbo.getWidth(), fbo.getHeight());
                }
                mIsNewShader = false;
            }

            if (mShader != null) {
                mFramebufferObject.enable();
                glViewport(0, 0, mFramebufferObject.getWidth(), mFramebufferObject.getHeight());
            }

            glClear(GL_COLOR_BUFFER_BIT);

            Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

            if (mImageTexture != null) {
                mImageShader.draw(mImageTexture.getTexName(), mMVPMatrix, mSTMatrix, mCameraRatio);
            } else {
                mPreviewShader.draw(mTexName, mMVPMatrix, mSTMatrix, mCameraRatio);

                try {
                    if (recordShader != null && mFramebufferObject != null) {
                        recordShader.setup();
                        recordShader.setFrameSize(mFramebufferObject.getHeight(), mFramebufferObject.getWidth());
                        recordShader.draw(mFramebufferObject.getTexName(), fbo);
                    }
                } catch (Exception ex) {

                }
            }

            if (mShader != null) {
                fbo.enable();
                glViewport(0, 0, fbo.getWidth(), fbo.getHeight());
                glClear(GL_COLOR_BUFFER_BIT);
                mShader.draw(mFramebufferObject.getTexName(), fbo);
            }
        }

        @Override
        public synchronized void onFrameAvailable(final PreviewTexture previewTexture) {
            mUpdateSurface = true;
            requestRender();
        }

        public void setMediaVideoEncoder(MediaVideoEncoder mediaVideoEncoder) {
            synchronized(this) {
                if (mediaVideoEncoder != null) {
                    if (filter.get() == 3) {
                        recordShader = new GlRecordPixellateShader();
                    } else {
                        int resourceFilter = -1;
                        if (filter.get().equals(0)) resourceFilter = R.drawable.video_filter_punch;
                        else if (filter.get().equals(1)) resourceFilter = R.drawable.video_filter_blue;
                        else if (filter.get().equals(2)) resourceFilter = R.drawable.video_filter_bw;
                        else resourceFilter = R.drawable.video_filter_punch;
                        recordShader = new GlRecordLutShader(getContext().getResources(), resourceFilter);
                    }

                    recordShader.setMediaVideoEncoder(mediaVideoEncoder);
                } else {
                    recordShader = null;
                }
            }
        }
    }
}