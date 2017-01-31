package com.tribe.app.presentation.view.camera.gles;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tribe.app.presentation.view.camera.utils.BitmapFactoryUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.opengl.GLES20.GL_MAX_TEXTURE_SIZE;
import static android.opengl.GLES20.glGetIntegerv;


public class GlImageResourceTexture extends GlImageTexture {

    private InputStream imageStream;

    private final boolean autoClose;

    public GlImageResourceTexture(final Resources res, final int resId) {
        this(res.openRawResource(resId), true);
    }

    public GlImageResourceTexture(final String filename) throws FileNotFoundException {
        this(new FileInputStream(new File(filename)), true);
    }

    public GlImageResourceTexture(final File file) throws FileNotFoundException {
        this(new FileInputStream(file), true);
    }

    public GlImageResourceTexture(final InputStream is) {
        this(is, true);
    }

    public GlImageResourceTexture(final InputStream is, final boolean autoClose) {
        if (is == null) {
            throw new IllegalArgumentException("InputStream must not be null");
        }

        this.imageStream = is;
        this.autoClose = autoClose;
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    @Override
    public void setup() {
        final int[] args = new int[1];
        glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0);
        final int maxTextureSize = args[0];

        final BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(imageStream, null, opts);

        final int size = Math.max(opts.outWidth, opts.outHeight);
        if (size > maxTextureSize) {
            opts.inSampleSize = size / maxTextureSize;
        }

        opts.inJustDecodeBounds = false;
        opts.inDither = true;
        final Bitmap bitmap = BitmapFactoryUtils.decodeStream(imageStream, opts.inSampleSize, 0, 2);
        try {
            attachToTexture(bitmap);
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (autoClose) {
                dispose();
            }
        } finally {
            super.finalize();
        }
    }

    public void dispose() {
        if (imageStream != null) {
            try {
                imageStream.close();
            } catch (final IOException e) {
            }
        }

        imageStream = null;
    }

}
