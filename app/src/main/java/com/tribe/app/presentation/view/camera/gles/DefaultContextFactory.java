package com.tribe.app.presentation.view.camera.gles;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import timber.log.Timber;

import static javax.microedition.khronos.egl.EGL10.EGL_NONE;
import static javax.microedition.khronos.egl.EGL10.EGL_NO_CONTEXT;

public class DefaultContextFactory implements GLSurfaceView.EGLContextFactory {

    private static final String TAG = "DefaultContextFactory";

    private int eGLContextClientVersion;

    public DefaultContextFactory() {
    }

    public DefaultContextFactory(final int version) {
        eGLContextClientVersion = version;
    }

    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    @Override
    public EGLContext createContext(final EGL10 egl, final EGLDisplay display, final EGLConfig config) {
        final int[] attrib_list;
        if (eGLContextClientVersion != 0) {
            attrib_list = new int[]{EGL_CONTEXT_CLIENT_VERSION, eGLContextClientVersion, EGL_NONE};
        } else {
            attrib_list = null;
        }
        return egl.eglCreateContext(display, config, EGL_NO_CONTEXT, attrib_list);
    }

    @Override
    public void destroyContext(final EGL10 egl, final EGLDisplay display, final EGLContext context) {
        if (!egl.eglDestroyContext(display, context)) {
            Timber.e("display:" + display + " context: " + context);
            throw new RuntimeException("eglDestroyContex" + egl.eglGetError());
        }
    }
}