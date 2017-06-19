package com.tribe.tribelivesdk.view.opengl.filter;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import com.tribe.tribelivesdk.view.opengl.utils.Common;
import com.tribe.tribelivesdk.view.opengl.utils.ProgramObject;
import java.nio.FloatBuffer;

public abstract class FrameRenderer {

  public static final String LOG_TAG = Common.LOG_TAG;

  public abstract boolean init(boolean isExternalOES);

  public abstract void release();

  public abstract void renderTexture(int texID, Viewport viewport);

  public abstract void setTextureSize(int width, int height);

  public abstract String getVertexShaderString();

  public abstract String getFragmentShaderString();

  public static class Viewport {
    public int x, y;
    public int width, height;

    public Viewport() {
    }

    public Viewport(int _x, int _y, int _width, int _height) {
      x = _x;
      y = _y;
      width = _width;
      height = _height;
    }
  }

  ////////////////////////////////////////////////////////////////

  protected static final String REQUIRE_STRING_EXTERNAL_OES =
      "#extension GL_OES_EGL_image_external : require\n";
  protected static final String SAMPLER2D_VAR_EXTERNAL_OES = "samplerExternalOES";
  protected static final String SAMPLER2D_VAR = "sampler2D";

  protected static final String vectorShaderDrawDefault = "" +
      "attribute vec2 vPosition;\n" +
      "varying vec2 texCoord;\n" +
      "uniform mat2 rotation;" +
      "void main()\n" +
      "{\n" +
      "   gl_Position = vec4(vPosition, 0.0, 1.0);\n" +
      "   texCoord = (vec2(vPosition.x, -vPosition.y) / 2.0) * rotation + 0.5;\n" +
      "}";

  protected static final String POSITION_NAME = "vPosition";
  protected static final String ROTATION_NAME = "rotation";

  public static final float[] vertices = { -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f };
  public static final int DRAW_FUNCTION = GLES20.GL_TRIANGLE_FAN;

  protected int TEXTURE_2D_BINDABLE;

  protected int vertexBuffer;
  protected ProgramObject program;

  protected float[] rotation;

  protected int textureWidth, textureHeight;

  public void setRotation(float rad) {
    final float cosRad = (float) Math.cos(rad);
    final float sinRad = (float) Math.sin(rad);

    rotation = new float[] {
        cosRad, sinRad, -sinRad, cosRad
    };

    assert program != null : "setRotation must not be called before init!";

    program.bind();
    program.sendUniformMat2(ROTATION_NAME, 1, false, rotation);
  }

  protected boolean setProgramDefualt(String vsh, String fsh, boolean isExternalOES) {
    TEXTURE_2D_BINDABLE = isExternalOES ? GLES11Ext.GL_TEXTURE_EXTERNAL_OES : GLES20.GL_TEXTURE_2D;
    program = new ProgramObject();
    program.bindAttribLocation(POSITION_NAME, 0);
    String fshResult = (isExternalOES ? REQUIRE_STRING_EXTERNAL_OES : "") +
        String.format(fsh, isExternalOES ? SAMPLER2D_VAR_EXTERNAL_OES : SAMPLER2D_VAR);
    if (program.init(vsh, fshResult)) {
      setRotation(0.0f);
      return true;
    }
    return false;
  }

  protected void defaultInitialize() {
    int[] vertexBuffer = new int[1];
    GLES20.glGenBuffers(1, vertexBuffer, 0);
    this.vertexBuffer = vertexBuffer[0];

    assert this.vertexBuffer != 0 : "Invalid VertexBuffer!";

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.vertexBuffer);
    FloatBuffer buffer = FloatBuffer.allocate(vertices.length);
    buffer.put(vertices).position(0);
    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 32, buffer, GLES20.GL_STATIC_DRAW);
  }
}
