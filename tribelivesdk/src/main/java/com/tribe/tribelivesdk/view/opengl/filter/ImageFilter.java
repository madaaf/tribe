package com.tribe.tribelivesdk.view.opengl.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.annotation.StringRes;
import com.tribe.tribelivesdk.view.opengl.gles.OpenGLES;
import com.tribe.tribelivesdk.view.opengl.gles.Texture;
import com.tribe.tribelivesdk.view.opengl.utils.ImgSdk;
import java.util.HashMap;
import timber.log.Timber;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteBuffers;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

public class ImageFilter extends FilterMask {

  @StringDef({ IMAGE_FILTER_TAN, IMAGE_FILTER_BW, IMAGE_FILTER_HIPSTER, IMAGE_FILTER_NONE })
  public @interface ImageFilterType {
  }

  public static final String IMAGE_FILTER_TAN = "IMAGE_FILTER_TAN";
  public static final String IMAGE_FILTER_BW = "IMAGE_FILTER_BW";
  public static final String IMAGE_FILTER_HIPSTER = "IMAGE_FILTER_HIPSTER";
  public static final String IMAGE_FILTER_NONE = "IMAGE_FILTER_NONE";

  public static final String DEFAULT_ATTRIB_POSITION = "in_pos";
  public static final String DEFAULT_ATTRIB_TEXTURE_COORDINATE = "in_tc";
  public static final String DEFAULT_UNIFORM_SAMPLER = "sTexture";

  private final OpenGLES openGLES = OpenGLES.getInstance();

  private int textureTarget = -1;
  //protected FrameBufferObject fbo;
  protected int texCache = 0, cacheTexWidth, cacheTexHeight, textureWidth = 0, textureHeight = 0;

  protected static final String DEFAULT_VERTEX_SHADER = "varying vec2 interp_tc;\n" +
      "attribute vec4 " +
      DEFAULT_ATTRIB_POSITION +
      ";\n" +
      "attribute vec4 " +
      DEFAULT_ATTRIB_TEXTURE_COORDINATE +
      ";\n" +
      "\n" +
      "uniform mat4 texMatrix;\n" +
      "\n" +
      "void main() {\n" +
      "    gl_Position = " +
      DEFAULT_ATTRIB_POSITION +
      ";\n" +
      "    interp_tc = (texMatrix * " +
      DEFAULT_ATTRIB_TEXTURE_COORDINATE +
      ").xy;\n" +
      "}\n";

  protected static final String TARGET_PLACEHOLDER = "#*SAMPLER_TYPE*#";

  private static final String DEFAULT_FRAGMENT_SHADER_DUMMY = "precision mediump float;\n" +
      "varying lowp vec2 interp_tc;\n" +
      "uniform lowp " +
      TARGET_PLACEHOLDER +
      " " +
      DEFAULT_UNIFORM_SAMPLER +
      ";\n" +
      "void main() {\n" +
      "gl_FragColor = texture2D(" +
      DEFAULT_UNIFORM_SAMPLER +
      ", interp_tc);\n" +
      "}\n";

  private static final float[] VERTICES_DATA = new float[] {
      // X, Y, Z, U, V
      -1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
      1.0f, -1.0f, 0.0f, 1.0f, 0.0f
  };

  private static final int FLOAT_SIZE_BYTES = 4;
  protected static final int VERTICES_DATA_POS_SIZE = 3;
  protected static final int VERTICES_DATA_UV_SIZE = 2;
  protected static final int VERTICES_DATA_STRIDE_BYTES =
      (VERTICES_DATA_POS_SIZE + VERTICES_DATA_UV_SIZE) * FLOAT_SIZE_BYTES;
  protected static final int VERTICES_DATA_POS_OFFSET = 0; //0 * FLOAT_SIZE_BYTES;
  protected static final int VERTICES_DATA_UV_OFFSET =
      VERTICES_DATA_POS_OFFSET + VERTICES_DATA_POS_SIZE * FLOAT_SIZE_BYTES;

  private final String mVertexShaderSource;
  private final String mFragmentShaderSource;

  private int program;
  private int vertexShader;
  private int fragmentShader;
  private int vertexBufferName;
  private boolean setupCompleted = false;

  private final HashMap<String, Integer> mHandleMap = new HashMap<>();

  public ImageFilter(Context context, @ImageFilterType String id, String name,
      @DrawableRes int drawableId) {
    super(context, id, name, drawableId);
    mVertexShaderSource = DEFAULT_VERTEX_SHADER;
    mFragmentShaderSource = DEFAULT_FRAGMENT_SHADER_DUMMY;
  }

  public ImageFilter(Context context, @ImageFilterType String id, String name,
      @DrawableRes int drawableId, @StringRes final int vertexShaderSourceResId,
      @StringRes final int fragmentShaderSourceResId) {
    this(context, id, name, drawableId, ImgSdk.getAppResource().getString(vertexShaderSourceResId),
        ImgSdk.getAppResource().getString(fragmentShaderSourceResId));
  }

  public ImageFilter(Context context, @ImageFilterType String id, String name,
      @DrawableRes int drawableId, final String vertexShaderSource,
      final String fragmentShaderSource) {
    super(context, id, name, drawableId);
    mVertexShaderSource = vertexShaderSource;
    mFragmentShaderSource = fragmentShaderSource;
  }

  public void updateTextureSize(int textureWidth, int textureHeight) {
    if (this.textureHeight == textureHeight && this.textureWidth == textureWidth) return;
    this.textureWidth = textureWidth;
    this.textureHeight = textureHeight;
    release();
  }

  protected static String createTargetShader(@NonNull String shader, final int texTarget) {
    switch (texTarget) {
      case GLES11Ext.GL_TEXTURE_EXTERNAL_OES:
        final String gl_oes_require = "#extension GL_OES_EGL_image_external : require";

        return (shader.contains(gl_oes_require) ? "" : gl_oes_require + "\n") +
            shader.replace(TARGET_PLACEHOLDER, "samplerExternalOES");

      case GL_TEXTURE_2D:
      default:
        return shader.replace(TARGET_PLACEHOLDER, "sampler2D");
    }
  }

  protected synchronized void setup(final int textureTarget) {
    this.textureTarget = textureTarget;
    if (setupCompleted) {
      releaseProgram();
    }

    Timber.d("setup : " + getName());

    String targetFragmentShader = createTargetShader(mFragmentShaderSource, textureTarget);

    vertexShader = openGLES.generateShader(mVertexShaderSource, GL_VERTEX_SHADER);
    fragmentShader = openGLES.generateShader(targetFragmentShader, GL_FRAGMENT_SHADER);
    program = openGLES.loadProgram2(vertexShader, fragmentShader);
    vertexBufferName = openGLES.initBuffer(VERTICES_DATA);
    setupCompleted = true;
  }

  protected synchronized void releaseProgram() {
    if (!setupCompleted) {
      return;
    }

    Timber.d("glDeleteProgram : " + program);
    glDeleteProgram(program);
    checkGlError("glDeleteProgram");
    program = 0;
    Timber.d("glDeleteShader : " + vertexShader);
    glDeleteShader(vertexShader);
    checkGlError("glDeleteShader");
    vertexShader = 0;
    Timber.d("glDeleteShader : " + fragmentShader);
    glDeleteShader(fragmentShader);
    checkGlError("glDeleteShader");
    fragmentShader = 0;
    glDeleteBuffers(1, new int[] { vertexBufferName }, 0);
    Timber.d("glDeleteBuffers : " + vertexBufferName);
    checkGlError("glDeleteBuffers");
    vertexBufferName = 0;

    //if (fbo != null) fbo.release();

    mHandleMap.clear();
    setupCompleted = false;
  }

  /**
   * Release the shader program and texture
   */
  public synchronized void release() {
    if (!setupCompleted) {
      return;
    }

    Timber.d("Release : " + Thread.currentThread());

    textureTarget = -1;
    texCache = 0;
    cacheTexWidth = 0;
    cacheTexHeight = 0;
    releaseProgram();
  }

  public synchronized void draw(@NonNull Texture texture, final float[] texMatrix,
      final float[] texMatrixFBO, int viewportX, int viewportY, int viewportWidth,
      int viewportHeight) {
    Timber.d("onDraw ImageFilter");

    if (textureTarget != texture.getTextureTarget()) {
      setup(texture.getTextureTarget());
    }

    //if (fbo == null) fbo = new FrameBufferObject();

    //Timber.d("Id : " +
    //        getId() +
    //        "/ " +
    //        "texCache == %d | cacheTexWidth == %d | textureWidth == %d | cacheTexHeight == %d | textureHeight == %d",
    //    texCache, cacheTexWidth, textureWidth, cacheTexHeight, textureHeight);
    //if (texCache == 0 || cacheTexWidth != textureWidth || cacheTexHeight != textureHeight) {
    //  resetCacheTexture();
    //}

    useProgram();

    glUniformMatrix4fv(getHandle("texMatrix"), 1, false, texMatrix, 0);

    internalDraw(texture, texMatrix, texMatrixFBO, viewportX, viewportY, viewportWidth,
        viewportHeight);
  }

  private void internalDraw(@NonNull Texture texture, final float[] texMatrix,
      final float[] texMatrixFBO, int viewportX, int viewportY, int viewportWidth,
      int viewportHeight) {

    glBindBuffer(GL_ARRAY_BUFFER, vertexBufferName);
    glEnableVertexAttribArray(getHandle(DEFAULT_ATTRIB_POSITION));
    glVertexAttribPointer(getHandle(DEFAULT_ATTRIB_POSITION), VERTICES_DATA_POS_SIZE, GL_FLOAT,
        false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_POS_OFFSET);
    glEnableVertexAttribArray(getHandle(DEFAULT_ATTRIB_TEXTURE_COORDINATE));
    glVertexAttribPointer(getHandle(DEFAULT_ATTRIB_TEXTURE_COORDINATE), VERTICES_DATA_UV_SIZE,
        GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_UV_OFFSET);

    //glUniformMatrix4fv(getHandle("texMatrix"), 1, false, texMatrixFBO, 0);
    //
    //fbo.bind();
    //glActiveTexture(GL_TEXTURE0);
    //glBindTexture(texture.getTextureTarget(), texture.getTextureId());
    //glUniform1i(getHandle(DEFAULT_UNIFORM_SAMPLER), 0);
    //
    //onDraw(viewportX, viewportY, cacheTexWidth, cacheTexHeight);

    //glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(texture.getTextureTarget(), texture.getTextureId());
    glUniform1i(getHandle(DEFAULT_UNIFORM_SAMPLER), 0);

    onDraw(viewportX, viewportY, viewportWidth, viewportHeight);

    glDisableVertexAttribArray(getHandle(DEFAULT_ATTRIB_POSITION));
    glDisableVertexAttribArray(getHandle(DEFAULT_ATTRIB_TEXTURE_COORDINATE));
    glBindTexture(GL_TEXTURE_2D, 0);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
  }

  protected void onDraw(int x, int y, int width, int height) {
    // Draw quad.
    GLES20.glViewport(x, y, width, height);
    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
  }

  protected final void useProgram() {
    glUseProgram(program);
  }

  protected final int getHandle(final String name) {
    final Integer value = mHandleMap.get(name);
    if (value != null) {
      return value;
    }

    int location = glGetAttribLocation(program, name);
    if (location == -1) {
      location = glGetUniformLocation(program, name);
    }
    if (location == -1) {
      throw new IllegalStateException("Could not get attrib or uniform location for " + name);
    }
    mHandleMap.put(name, location);
    return location;
  }

  protected void resetCacheTexture() {
    Timber.d("resetCacheTexture...");
    cacheTexWidth = textureWidth;
    cacheTexHeight = textureHeight;

    if (texCache == 0) {
      int[] texCacheArray = new int[1];
      GLES20.glGenTextures(1, texCacheArray, 0);
      texCache = texCacheArray[0];
    }

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texCache);
    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, cacheTexWidth, cacheTexHeight, 0,
        GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

    //fbo.bindTexture(texCache);
  }

  //public FrameBufferObject getFbo() {
  //  return fbo;
  //}

  protected void checkGlError(String op) {
    int error;
    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
      Timber.e(op + ": glError " + error);
      throw new RuntimeException(op + ": glError " + error);
    }
  }
}
