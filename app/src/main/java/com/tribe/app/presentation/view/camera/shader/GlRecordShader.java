package com.tribe.app.presentation.view.camera.shader;

import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

import com.tribe.app.presentation.view.camera.recorder.MediaVideoEncoder;
import com.tribe.app.presentation.view.camera.renderer.GLES20FramebufferObject;

public abstract class GlRecordShader extends GlShader {

    protected MediaVideoEncoder mediaVideoEncoder;
    protected EGLDisplay savedEglDisplay     = null;
    protected EGLSurface savedEglDrawSurface = null;
    protected EGLSurface savedEglReadSurface = null;
    protected EGLContext savedEglContext     = null;

    public GlRecordShader(final String vertexShaderSource, final String fragmentShaderSource) {
        super(vertexShaderSource, fragmentShaderSource);
    }

    @Override
    public void draw(int texName, GLES20FramebufferObject fbo) {
        saveRenderState();

        mediaVideoEncoder.firstTimeSetup();
        mediaVideoEncoder.makeCurrent();

        super.draw(texName, fbo);

        mediaVideoEncoder.swapBuffers();

        restoreRenderState();
    }

    public MediaVideoEncoder getMediaVideoEncoder() {
        return mediaVideoEncoder;
    }

    public void setMediaVideoEncoder(MediaVideoEncoder mediaVideoEncoder) {
        this.mediaVideoEncoder = mediaVideoEncoder;
    }

    protected void saveRenderState() {
        savedEglDisplay     = EGL14.eglGetCurrentDisplay();
        savedEglDrawSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
        savedEglReadSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ);
        savedEglContext     = EGL14.eglGetCurrentContext();
    }

    protected void restoreRenderState() {
        if (!EGL14.eglMakeCurrent(
                savedEglDisplay,
                savedEglDrawSurface,
                savedEglReadSurface,
                savedEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }
}
