package com.tribe.tribelivesdk.view.opengl.filter;

import android.opengl.GLES20;

/**
 * Created by wangyang on 15/7/18.
 */
public class FrameRendererWave extends FrameRendererDrawOrigin {

  private static final String fragmentShaderWave = "" +
      "precision mediump float;\n" +
      "varying vec2 texCoord;\n" +
      "uniform %s inputImageTexture;\n" +
      "uniform float motion;\n" +
      "const float angle = 20.0;" +
      "void main()\n" +
      "{\n" +
      "   vec2 coord;\n" +
      "   coord.x = texCoord.x + 0.01 * sin(motion + texCoord.x * angle);\n" +
      "   coord.y = texCoord.y + 0.01 * sin(motion + texCoord.y * angle);\n" +
      "   gl_FragColor = texture2D(inputImageTexture, coord);\n" +
      "}";

  private int motionLoc = 0;

  private boolean autoMotion = false;
  private float motion = 0.0f;
  private float motionSpeed = 0.0f;

  public FrameRendererWave() {
  }

  public static FrameRendererWave create(boolean isExternalOES) {
    FrameRendererWave renderer = new FrameRendererWave();
    if (!renderer.init(isExternalOES)) {
      renderer.release();
      return null;
    }
    return renderer;
  }

  @Override public boolean init(boolean isExternalOES) {
    if (setProgramDefualt(vectorShaderDrawDefault, fragmentShaderWave, isExternalOES)) {
      program.bind();
      motionLoc = program.getUniformLoc("motion");
      return true;
    }
    return false;
  }

  public void setWaveMotion(float motion) {
    program.bind();
    GLES20.glUniform1f(motionLoc, motion);
  }

  public void setAutoMotion(float speed) {
    motionSpeed = speed;
    autoMotion = (speed != 0.0f);
  }

  @Override public void renderTexture(int texID, Viewport viewport) {

    if (viewport != null) {
      GLES20.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);
    }

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(TEXTURE_2D_BINDABLE, texID);

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer);
    GLES20.glEnableVertexAttribArray(0);
    GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 0, 0);

    program.bind();

    if (autoMotion) {
      motion += motionSpeed;
      GLES20.glUniform1f(motionLoc, motion);
      if (motion > Math.PI * 20.0f) {
        motion -= Math.PI * 20.0f;
      }
    }

    GLES20.glDrawArrays(DRAW_FUNCTION, 0, 4);
  }
}
