package com.tribe.app.presentation.view.camera.renderer;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.tribe.app.presentation.view.camera.shader.GlShader;
import com.tribe.app.presentation.view.camera.utils.Fps;
import com.tribe.app.presentation.view.camera.utils.OpenGlUtils;

import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glViewport;


public abstract class GlFrameBufferObjectRenderer implements GLSurfaceView.Renderer {

    private GLES20FramebufferObject framebufferObject;
    private GlShader shader;
    private final Queue<Runnable> runOnDraw;
    private Fps fps;
    private int gLTextureId = OpenGlUtils.NO_TEXTURE;

    protected GlFrameBufferObjectRenderer() {
        runOnDraw = new LinkedList<>();
    }

    public void setFps(final Fps fps) {
        if (this.fps != null) {
            this.fps.stop();
            this.fps = null;
        }

        this.fps = fps;
    }

    public Bitmap getBitmap() {
        return framebufferObject.getBitmap();
    }

    public Bitmap getBitmap(final int orientation) {
        return framebufferObject.getBitmap(orientation);
    }

    public Bitmap getBitmap(final int orientation, final boolean mirror) {
        return framebufferObject.getBitmap(orientation, mirror);
    }

    @Override
    public final void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
        framebufferObject = new GLES20FramebufferObject();
        shader = new GlShader();
        shader.setup();
        onSurfaceCreated(config);

        if (fps != null) {
            fps.start();
        }
    }

    @Override
    public final void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        framebufferObject.setup(width, height);
        shader.setFrameSize(width, height);
        onSurfaceChanged(width, height);
    }

    @Override
    public final void onDrawFrame(final GL10 gl) {
        synchronized(runOnDraw) {
            while(!runOnDraw.isEmpty()) {
                runOnDraw.poll().run();
            }
        }

        framebufferObject.enable();
        glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());

        onDrawFrame(framebufferObject);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        shader.draw(framebufferObject.getTexName(), null);

        if (fps != null) {
            fps.countUp();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (fps != null) {
                fps.stop();
                fps = null;
            }
        } finally {
            super.finalize();
        }
    }

    public abstract void onSurfaceCreated(EGLConfig config);

    public abstract void onSurfaceChanged(int width, int height);

    public abstract void onDrawFrame(GLES20FramebufferObject fbo);

    protected void runOnDraw(final Runnable runnable) {
        synchronized (runOnDraw) {
            runOnDraw.add(runnable);
        }
    }

    public void setImageBitmap(final Bitmap bitmap, boolean b) {
        runOnDraw(() -> {
            gLTextureId = OpenGlUtils.loadTexture(bitmap, gLTextureId, false);

            if (bitmap != null) {
                bitmap.recycle();
            }
        });
    }

    public void deleteImage() {
        runOnDraw(() -> {
            try {
                GLES20.glDeleteTextures(1, new int[] { gLTextureId }, 0);
                gLTextureId = -1;
            } catch (Exception e){
                Log.d("DEBUG", "", e);
            }
        });
    }
}

