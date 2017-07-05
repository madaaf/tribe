package com.tribe.tribelivesdk.view.opengl.objloader;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;
import javax.microedition.khronos.opengles.GL10;
import org.opencv.core.Mat;

public class TDModel {

  Vector<Float> v;
  Vector<Float> vn;
  Vector<Float> vt;
  Vector<TDModelPart> parts;
  FloatBuffer vertexBuffer;

  private Context activityContext;
  private int textureId = -1;
  private static int textureIdX = -1;
  private boolean shouldLoadTexture = false;
  private ByteBuffer dataBufGL;
  private int bufGLW;
  private int bufGLH;
  private float[] modelMatrix = new float[16];

  public TDModel(Vector<Float> v, Vector<Float> vn, Vector<Float> vt, Vector<TDModelPart> parts) {
    super();
    this.v = v;
    this.vn = vn;
    this.vt = vt;
    this.parts = parts;
  }

  public String toString() {
    String str = new String();
    str += "Number of parts: " + parts.size();
    str += "\nNumber of vertexes: " + v.size();
    str += "\nNumber of vns: " + vn.size();
    str += "\nNumber of vts: " + vt.size();
    str += "\n/////////////////////////\n";
    for (int i = 0; i < parts.size(); i++) {
      str += "Part " + i + '\n';
      str += parts.get(i).toString();
      str += "\n/////////////////////////";
    }
    return str;
  }

  public void draw(GL10 gl) {
    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
  }

  public void buildVertexBuffer() {
    ByteBuffer vBuf = ByteBuffer.allocateDirect(v.size() * 4);
    vBuf.order(ByteOrder.nativeOrder());
    vertexBuffer = vBuf.asFloatBuffer();
    vertexBuffer.put(toPrimitiveArrayF(v));
    vertexBuffer.position(0);
  }

  private static float[] toPrimitiveArrayF(Vector<Float> vector) {
    float[] f;
    f = new float[vector.size()];
    for (int i = 0; i < vector.size(); i++) {
      f[i] = vector.get(i);
    }
    return f;
  }

  public void checkGlError(String glOperation) {
    int error;
    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
      Log.e("checkGlError_triangle", glOperation + ": glError " + error);
      throw new RuntimeException(glOperation + ": glError " + error);
    }
  }

  public void setContext(Context c) {
    activityContext = c;
  }

  public void loadPixels(Mat mat) { // New function.
    if (dataBufGL == null) {
      dataBufGL =
          ByteBuffer.allocateDirect(mat.cols() * mat.rows() * 4 * 2).order(ByteOrder.nativeOrder());
    }
    //dataBufGL = ByteBuffer.allocateDirect(mat.cols() * mat.rows() * 4).order(ByteOrder.nativeOrder());
    //mat.get(0, 0, dataBufGL.array());
    mat.getdata(0, 0, dataBufGL.array(), mat.cols() * mat.rows() * 4);
    //dataBufGL = (ByteBuffer)buf;
    bufGLW = mat.cols();
    bufGLH = mat.rows();
    shouldLoadTexture = true;
  }

  int[] textures_gen = { 0 };
  int[] textures_del = { 0 };

  private void loadGLTexture(GL10 gl) {
    GLES20.glGenTextures(1, textures_gen, 0);

    textures_del[0] = textureId;
    textureId = textures_gen[0];

    // ...and bind it to our array
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

    // Create Nearest Filtered Texture
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
        GLES20.GL_NEAREST); // GL10.GL_NEAREST for clear image
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
        GLES20.GL_NEAREST); // GL10.GL_NEAREST for clear image

    // Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE/*GL10.GL_REPEAT*/);

    // Use the Android GLUtils to specify a two-dimensional texture image
    // from our bitmap
    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bufGLW, bufGLH, 0, GLES20.GL_RGBA,
        GLES20.GL_UNSIGNED_BYTE, dataBufGL);

    // delete texture of previous frame 
    GLES20.glDeleteTextures(1, textures_del, 0);

    if (textureIdX == -1) {
      GLES20.glGenTextures(1, textures_gen, 0);

      textureIdX = textures_gen[0];

      // ...and bind it to our array
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIdX);

      // Create Nearest Filtered Texture
      GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
          GLES20.GL_NEAREST); // GL10.GL_NEAREST for clear image
      GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
          GLES20.GL_NEAREST); // GL10.GL_NEAREST for clear image

      // Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
      GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
          GLES20.GL_CLAMP_TO_EDGE);
      GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE/*GL10.GL_REPEAT*/);
    }
  }
}


