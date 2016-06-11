package com.tribe.app.presentation.view.camera.renderer;

import android.graphics.Bitmap;
import com.tribe.app.presentation.view.camera.utils.OpenGlUtils;

import java.nio.IntBuffer;

import static android.opengl.GLES20.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES20.GL_DEPTH_ATTACHMENT;
import static android.opengl.GLES20.GL_DEPTH_COMPONENT16;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_FRAMEBUFFER_BINDING;
import static android.opengl.GLES20.GL_FRAMEBUFFER_COMPLETE;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_MAX_RENDERBUFFER_SIZE;
import static android.opengl.GLES20.GL_MAX_TEXTURE_SIZE;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_RENDERBUFFER;
import static android.opengl.GLES20.GL_RENDERBUFFER_BINDING;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_BINDING_2D;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindRenderbuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glCheckFramebufferStatus;
import static android.opengl.GLES20.glDeleteFramebuffers;
import static android.opengl.GLES20.glDeleteRenderbuffers;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glFramebufferRenderbuffer;
import static android.opengl.GLES20.glFramebufferTexture2D;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glGenRenderbuffers;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGetIntegerv;
import static android.opengl.GLES20.glReadPixels;
import static android.opengl.GLES20.glRenderbufferStorage;
import static android.opengl.GLES20.glTexImage2D;

public class GLES20FramebufferObject {

    private int mWidth;
    private int mHeight;
    private int mFramebufferName;
    private int mRenderbufferName;
    private int mTexName;

    public int getWidth() {
        return mWidth;
    }
    public int getHeight() {
        return mHeight;
    }
    public int getTexName() {
        return mTexName;
    }

    public void setup(final int width, final int height) {
        final int[] args = new int[1];
        glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0);
        if (width > args[0] || height > args[0]) {
            throw new IllegalArgumentException("GL_MAX_TEXTURE_SIZE " + args[0]);
        }

        glGetIntegerv(GL_MAX_RENDERBUFFER_SIZE, args, 0);
        if (width > args[0] || height > args[0]) {
            throw new IllegalArgumentException("GL_MAX_RENDERBUFFER_SIZE " + args[0]);
        }

        glGetIntegerv(GL_FRAMEBUFFER_BINDING, args, 0);
        final int saveFramebuffer = args[0];
        glGetIntegerv(GL_RENDERBUFFER_BINDING, args, 0);
        final int saveRenderbuffer = args[0];
        glGetIntegerv(GL_TEXTURE_BINDING_2D, args, 0);
        final int saveTexName = args[0];

        release();

        try {
            mWidth = width;
            mHeight = height;

            glGenFramebuffers(args.length, args, 0);
            mFramebufferName = args[0];
            glBindFramebuffer(GL_FRAMEBUFFER, mFramebufferName);

            glGenRenderbuffers(args.length, args, 0);
            mRenderbufferName = args[0];
            glBindRenderbuffer(GL_RENDERBUFFER, mRenderbufferName);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width, height);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, mRenderbufferName);

            glGenTextures(args.length, args, 0);
            mTexName = args[0];
            glBindTexture(GL_TEXTURE_2D, mTexName);

            OpenGlUtils.setupSampler(GL_TEXTURE_2D, GL_LINEAR, GL_NEAREST);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mTexName, 0);

            final int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
            if (status != GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException("Failed to initialize framebuffer object " + status);
            }
        } catch (final RuntimeException e) {
            release();
            throw e;
        }

        glBindFramebuffer(GL_FRAMEBUFFER, saveFramebuffer);
        glBindRenderbuffer(GL_RENDERBUFFER, saveRenderbuffer);
        glBindTexture(GL_TEXTURE_2D, saveTexName);
    }

    public void release() {
        final int[] args = new int[1];
        args[0] = mTexName;
        glDeleteTextures(args.length, args, 0);
        mTexName = 0;
        args[0] = mRenderbufferName;
        glDeleteRenderbuffers(args.length, args, 0);
        mRenderbufferName = 0;
        args[0] = mFramebufferName;
        glDeleteFramebuffers(args.length, args, 0);
        mFramebufferName = 0;
    }

    public void enable() {
        glBindFramebuffer(GL_FRAMEBUFFER, mFramebufferName);
    }

    public Bitmap getBitmap() {
        return getBitmap(0, false);
    }

    public Bitmap getBitmap(final int orientation) {
        return getBitmap(orientation, false);
    }

    public Bitmap getBitmap(final int orientation, final boolean mirror) {
        final int[] pixels = new int[mWidth * mHeight];
        final IntBuffer buffer = IntBuffer.wrap(pixels);
        buffer.position(0);

        enable();
        glReadPixels(0, 0, mWidth, mHeight, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return OpenGlUtils.createBitmap(pixels, mWidth, mHeight, Bitmap.Config.ARGB_8888, orientation, mirror);
    }

}
