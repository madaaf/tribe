package com.tribe.tribelivesdk.view.opengl.filter;

import android.opengl.GLES20;

public class FrameRendererDrawOrigin extends FrameRenderer {

  private static final String fragmentShaderDrawOrigin = "" +
      "precision mediump float;\n" +
      "varying vec2 texCoord;\n" +
      "uniform %s inputImageTexture;\n" +
      "void main()\n" +
      "{\n" +
      "   gl_FragColor = texture2D(inputImageTexture, texCoord);\n" +
      "}";

  FrameRendererDrawOrigin() {
    defaultInitialize();
  }

  FrameRendererDrawOrigin(boolean noDefaultInitialize) {
    if (!noDefaultInitialize) defaultInitialize();
  }

  public static FrameRendererDrawOrigin create(boolean isExternalOES) {
    FrameRendererDrawOrigin renderer = new FrameRendererDrawOrigin();

    if (!renderer.init(isExternalOES)) {
      renderer.release();
      return null;
    }

    return renderer;
  }

  @Override public boolean init(boolean isExternalOES) {
    boolean ret =
        setProgramDefualt(getVertexShaderString(), getFragmentShaderString(), isExternalOES);
    onInitialized();
    return ret;
  }

  protected void onInitialized() {
  }

  @Override public void release() {
    GLES20.glDeleteBuffers(1, new int[] { vertexBuffer }, 0);
    vertexBuffer = 0;
    program.release();
    program = null;
    rotation = null;
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
    onDrawArraysPre();
    GLES20.glDrawArrays(DRAW_FUNCTION, 0, 4);
  }

  protected void onDrawArraysPre() {
  }

  @Override public void setTextureSize(int w, int h) {
    textureWidth = w;
    textureHeight = h;
  }

  @Override public String getVertexShaderString() {
    return vectorShaderDrawDefault;
  }

  @Override public String getFragmentShaderString() {
    return fragmentShaderDrawOrigin;
  }
}
