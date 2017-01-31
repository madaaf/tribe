package com.tribe.app.presentation.view.camera.shader;

import com.tribe.app.presentation.view.camera.recorder.MediaVideoEncoder;

import static android.opengl.GLES20.glUniform1f;

public class GlRecordPixellateShader extends GlRecordShader {

  private static final String FRAGMENT_SHADER = "precision mediump float;\n"
      +
      "varying vec2 vTextureCoord;\n"
      +
      "uniform lowp sampler2D sTexture;\n"
      +

      "uniform highp float fractionalWidthOfPixel;\n"
      +
      "uniform highp float aspectRatio;\n"
      +

      "void main() {\n"
      +
      "highp vec2 sampleDivisor = vec2(fractionalWidthOfPixel, fractionalWidthOfPixel / aspectRatio);\n"
      +
      "highp vec2 samplePos = vTextureCoord - mod(vTextureCoord, sampleDivisor) + 0.5 * sampleDivisor;\n"
      +
      "gl_FragColor = texture2D(sTexture, samplePos);\n"
      +
      "}\n";

  private float fractionalWidthOfPixel = 1f / 20f;
  private float aspectRatio = 1.0f;

  public GlRecordPixellateShader() {
    super(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER);
  }

  protected String shaderName = "pixelate";

  public String getName() {
    return shaderName;
  }

  public float getFractionalWidthOfPixel() {
    return fractionalWidthOfPixel;
  }

  public void setFractionalWidthOfPixel(final float fractionalWidthOfPixel) {
    this.fractionalWidthOfPixel = fractionalWidthOfPixel;
  }

  public float getAspectRatio() {
    return aspectRatio;
  }

  public void setAspectRatio(final float aspectRatio) {
    this.aspectRatio = aspectRatio;
  }

  //////////////////////////////////////////////////////////////////////////

  @Override protected void onDraw() {
    glUniform1f(getHandle("fractionalWidthOfPixel"), fractionalWidthOfPixel);
    glUniform1f(getHandle("aspectRatio"), aspectRatio);
  }

  public void setMediaVideoEncoder(MediaVideoEncoder mediaVideoEncoder) {
    this.mediaVideoEncoder = mediaVideoEncoder;
  }
}