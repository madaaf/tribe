package com.tribe.app.presentation.view.camera.shader.fx;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;

import com.tribe.app.presentation.view.camera.recorder.TribeRecorder;
import com.tribe.app.presentation.view.camera.renderer.GLES20FramebufferObject;
import com.tribe.app.presentation.view.camera.shader.GlShader;
import com.tribe.app.presentation.view.camera.utils.OpenGlUtils;

public class GlRecordLutShader extends GlShader {
    private final static String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "uniform mediump sampler2D lutTexture; \n" +
                    "uniform lowp sampler2D sTexture; \n" +
                    "varying highp vec2 vTextureCoord; \n" +
                    "vec4 sampleAs3DTexture(vec3 uv, float width) {\n" +
                    "    float sliceSize = 1.0 / width;              // space of 1 slice\n" +
                    "    float slicePixelSize = sliceSize / width;           // space of 1 pixel\n" +
                    "    float sliceInnerSize = slicePixelSize * (width - 1.0);  // space of width pixels\n" +
                    "    float zSlice0 = min(floor(uv.z * width), width - 1.0);\n" +
                    "    float zSlice1 = min(zSlice0 + 1.0, width - 1.0);\n" +
                    "    float xOffset = slicePixelSize * 0.5 + uv.x * sliceInnerSize;\n" +
                    "    float s0 = xOffset + (zSlice0 * sliceSize);\n" +
                    "    float s1 = xOffset + (zSlice1 * sliceSize);\n" +
                    "    vec4 slice0Color = texture2D(lutTexture, vec2(s0, uv.y));\n" +
                    "    vec4 slice1Color = texture2D(lutTexture, vec2(s1, uv.y));\n" +
                    "    float zOffset = mod(uv.z * width, 1.0);\n" +
                    "    vec4 result = mix(slice0Color, slice1Color, zOffset);\n" +
                    "    return result;\n" +
                    "}\n" +
                    "void main() {\n" +
                    "   vec4 pixel = texture2D(sTexture, vTextureCoord);\n" +
                    "   vec4 gradedPixel = sampleAs3DTexture(pixel.rgb, 16.);\n" +
                    "   gradedPixel.a = pixel.a;\n" +
                    "   pixel = gradedPixel;\n" +
                    "   gl_FragColor = pixel;\n " +
                    "}";

    public GlRecordLutShader(Resources resources, int fxID) {
        super(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER);
        this.lutTexture = BitmapFactory.decodeResource(resources, fxID);
        hTex = -1;
    }

    private int hTex;

    private Bitmap lutTexture;

    private TribeRecorder tribeRecorder;

    private EGLDisplay mSavedEglDisplay     = null;
    private EGLSurface mSavedEglDrawSurface = null;
    private EGLSurface mSavedEglReadSurface = null;
    private EGLContext mSavedEglContext     = null;

    @Override
    public void draw(int texName, GLES20FramebufferObject fbo) {
        saveRenderState();

        tribeRecorder.firstTimeSetup();
        tribeRecorder.makeCurrent();

        super.draw(texName, fbo);

        tribeRecorder.swapBuffers();

        restoreRenderState();
    }

    @Override
    public void onDraw() {
        int offsetDepthMapTextureUniform = getHandle("lutTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, hTex);
        GLES20.glUniform1i(offsetDepthMapTextureUniform, 3);
    }

    @Override
    public void setup() {
        if (hTex == -1) {
            super.setup();
            hTex = OpenGlUtils.loadTexture(lutTexture, OpenGlUtils.NO_TEXTURE, false);
        }
    }

    public void reset() {
        hTex = -1;
        hTex = OpenGlUtils.loadTexture(lutTexture, OpenGlUtils.NO_TEXTURE, false);
    }

    public void setTribeRecorder(TribeRecorder tribeRecorder) {
        this.tribeRecorder = tribeRecorder;
    }

    private void saveRenderState() {
        mSavedEglDisplay     = EGL14.eglGetCurrentDisplay();
        mSavedEglDrawSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
        mSavedEglReadSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ);
        mSavedEglContext     = EGL14.eglGetCurrentContext();
    }

    private void restoreRenderState() {
        if (!EGL14.eglMakeCurrent(
                mSavedEglDisplay,
                mSavedEglDrawSurface,
                mSavedEglReadSurface,
                mSavedEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }
}
