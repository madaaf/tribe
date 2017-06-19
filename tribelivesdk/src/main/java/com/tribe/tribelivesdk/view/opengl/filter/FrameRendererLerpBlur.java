package com.tribe.tribelivesdk.view.opengl.filter;

import android.opengl.GLES20;
import android.util.Log;
import com.tribe.tribelivesdk.view.opengl.utils.FrameBufferObject;
import com.tribe.tribelivesdk.view.opengl.utils.ProgramObject;

public class FrameRendererLerpBlur extends FrameRendererDrawOrigin {

  private static final String vectorShaderUpScale = "" +
      "attribute vec2 vPosition;\n" +
      "varying vec2 texCoord;\n" +
      "void main()\n" +
      "{\n" +
      "   gl_Position = vec4(vPosition, 0.0, 1.0);\n" +
      "   texCoord = vPosition / 2.0 + 0.5;\n" +
      "}";

  private static final String fragmentShaderUpScale = "" +
      "precision mediump float;\n" +
      "varying vec2 texCoord;\n" +
      "uniform sampler2D inputImageTexture;\n" +

      "void main()\n" +
      "{\n" +
      "   gl_FragColor = texture2D(inputImageTexture, texCoord);\n" +
      "}";

  private static final String vectorShaderBlurUpScale = "" +
      "attribute vec2 vPosition;\n" +
      "varying vec2 texCoords[5];\n" +
      "uniform vec2 samplerSteps;\n" +
      "\n" +
      "void main()\n" +
      "{\n" +
      "  gl_Position = vec4(vPosition, 0.0, 1.0);\n" +
      "  vec2 texCoord = vPosition / 2.0 + 0.5;\n" +
      "  texCoords[0] = texCoord - 2.0 * samplerSteps;\n" +
      "  texCoords[1] = texCoord - 1.0 * samplerSteps;\n" +
      "  texCoords[2] = texCoord;\n" +
      "  texCoords[3] = texCoord + 1.0 * samplerSteps;\n" +
      "  texCoords[4] = texCoord + 2.0 * samplerSteps;\n" +
      "}";

  private static final String fragmentShaderBlurUpScale = "" +
      "precision mediump float;\n" +
      "varying vec2 texCoords[5];\n" +
      "uniform sampler2D inputImageTexture;\n" +
      "\n" +
      "void main()\n" +
      "{\n" +
      "  vec3 color = texture2D(inputImageTexture, texCoords[0]).rgb * 0.1;\n" +
      "  color += texture2D(inputImageTexture, texCoords[1]).rgb * 0.2;\n" +
      "  color += texture2D(inputImageTexture, texCoords[2]).rgb * 0.4;\n" +
      "  color += texture2D(inputImageTexture, texCoords[3]).rgb * 0.2;\n" +
      "  color += texture2D(inputImageTexture, texCoords[4]).rgb * 0.1;\n" +
      "\n" +
      "  gl_FragColor = vec4(color, 1.0);\n" +
      "}";

  private static final String vectorShaderBlurCache = "" +
      "attribute vec2 vPosition;\n" +
      "varying vec2 texCoord;\n" +
      "void main()\n" +
      "{\n" +
      "   gl_Position = vec4(vPosition, 0.0, 1.0);\n" +
      "   texCoord = vPosition / 2.0 + 0.5;\n" +
      "}";

  private static final String fragmentShaderBlur = "" +
      "precision highp float;\n" +
      "varying vec2 texCoord;\n" +
      "uniform sampler2D inputImageTexture;\n" +
      "uniform vec2 samplerSteps;\n" +

      "const int samplerRadius = 5;\n" +
      "const float samplerRadiusFloat = 5.0;\n" +

      "float random(vec2 seed)\n" +
      "{\n" +
      "  return fract(sin(dot(seed ,vec2(12.9898,78.233))) * 43758.5453);\n" +
      "}\n" +

      "void main()\n" +
      "{\n" +
      "  vec3 resultColor = vec3(0.0);\n" +
      "  float blurPixels = 0.0;\n" +
      "  float offset = random(texCoord) - 0.5;\n" +
      "  \n" +
      "  for(int i = -samplerRadius; i <= samplerRadius; ++i)\n" +
      "  {\n" +
      "    float percent = (float(i) + offset) / samplerRadiusFloat;\n" +
      "    float weight = 1.0 - abs(percent);\n" +
      "    vec2 coord = texCoord + samplerSteps * percent;\n" +
      "    resultColor += texture2D(inputImageTexture, coord).rgb * weight;\n" +
      "    blurPixels += weight;\n" +
      "  }\n" +

      "  gl_FragColor = vec4(resultColor / blurPixels, 1.0);\n" +
      "}";

  private static final String SAMPLER_STEPS = "samplerSteps";

  private ProgramObject scaleProgram;
  private int[] textureDownScale;

  private FrameBufferObject frameBuffer;
  private Viewport texViewport;
  private int samplerStepLoc = 0;

  private int intensity = 0;
  private boolean shouldUpdateTexture = true;

  private float sampleScaling = 1.0f;

  private final int level = 16;
  private final float base = 2.0f;

  public static FrameRendererLerpBlur create(boolean isExternalOES) {
    FrameRendererLerpBlur renderer = new FrameRendererLerpBlur();
    if (!renderer.init(isExternalOES)) {
      renderer.release();
      return null;
    }
    return renderer;
  }

  public void setIntensity(int intensity) {
    if (this.intensity == intensity) return;

    this.intensity = intensity;
    if (intensity > level) this.intensity = level;
    shouldUpdateTexture = true;
  }

  @Override public boolean init(boolean isExternalOES) {
    return super.init(isExternalOES) && initLocal();
  }

  @Override public void renderTexture(int texID, Viewport viewport) {
    if (intensity == 0) {
      GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
      super.renderTexture(texID, viewport);
      return;
    }

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

    frameBuffer.bindTexture(textureDownScale[0]);

    texViewport.width = calcMips(512, 1);
    texViewport.height = calcMips(512, 1);
    super.renderTexture(texID, texViewport);

    scaleProgram.bind();
    for (int i = 1; i < intensity; ++i) {
      frameBuffer.bindTexture(textureDownScale[i]);
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDownScale[i - 1]);
      GLES20.glViewport(0, 0, calcMips(512, i + 1), calcMips(512, i + 1));
      GLES20.glDrawArrays(DRAW_FUNCTION, 0, 4);
    }

    for (int i = intensity - 1; i > 0; --i) {
      frameBuffer.bindTexture(textureDownScale[i - 1]);
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDownScale[i]);
      GLES20.glViewport(0, 0, calcMips(512, i), calcMips(512, i));
      GLES20.glDrawArrays(DRAW_FUNCTION, 0, 4);
    }

    GLES20.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);

    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDownScale[0]);

    GLES20.glDrawArrays(DRAW_FUNCTION, 0, 4);
  }

  @Override public void release() {
    scaleProgram.release();
    frameBuffer.release();
    GLES20.glDeleteTextures(textureDownScale.length, textureDownScale, 0);
    scaleProgram = null;
    frameBuffer = null;
  }

  private boolean initLocal() {
    genMipmaps(level, 512, 512);
    frameBuffer = new FrameBufferObject();

    scaleProgram = new ProgramObject();
    scaleProgram.bindAttribLocation(POSITION_NAME, 0);

    if (!scaleProgram.init(vectorShaderUpScale, fragmentShaderUpScale)) {
      Log.e(LOG_TAG, "Lerp blur initLocal failed...");
      return false;
    }

    return true;
  }

  private void updateTexture() {

  }

  @Override public void setTextureSize(int w, int h) {
    super.setTextureSize(w, h);
  }

  private void genMipmaps(int level, int width, int height) {
    textureDownScale = new int[level];
    GLES20.glGenTextures(level, textureDownScale, 0);

    for (int i = 0; i < level; ++i) {
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDownScale[i]);
      GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, calcMips(width, i + 1),
          calcMips(height, i + 1), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
          GLES20.GL_CLAMP_TO_EDGE);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
          GLES20.GL_CLAMP_TO_EDGE);
    }

    texViewport = new Viewport(0, 0, 512, 512);
  }

  private int calcMips(int len, int level) {
    return len / (level + 1);
  }
}
