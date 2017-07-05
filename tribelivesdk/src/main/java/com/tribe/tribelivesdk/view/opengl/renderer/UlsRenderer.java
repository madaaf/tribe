package com.tribe.tribelivesdk.view.opengl.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import com.tribe.tribelivesdk.entity.CameraInfo;
import com.tribe.tribelivesdk.view.opengl.gles.SimplePlane;
import com.tribe.tribelivesdk.view.opengl.gles.ToolsUtil;
import com.tribe.tribelivesdk.view.opengl.objloader.OBJParser;
import com.tribe.tribelivesdk.view.opengl.objloader.TDModelPart;
import com.tribe.tribelivesdk.view.opengl.utils.ResUtils;
import com.uls.gl.Mesh;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * This class implements our custom renderer. Note that the GL10 parameter passed in is unused for
 * OpenGL ES 2.0
 * renderers -- the static class GLES20 is used instead.
 */
public class UlsRenderer {

  private static final int GLASSES_MAX_PITCH_WHEN_SHIFT = 5;   // In degree.

  private static UlsRenderer instance;

  public static UlsRenderer getInstance(Context context) {
    if (instance == null) {
      instance = new UlsRenderer(context);
    }

    return instance;
  }

  private Context context;
  private CameraInfo cameraInfo;
  private int cameraRotation;
  /** This is a handle to our cube shading program. */
  private int programHandle;
  private int programHandleX;
  private OBJParser parser;
  private TDModelPart tdModelPart = null;
  private float[] pose;
  private float facePoseElement0 = Float.MAX_VALUE, facePoseElement1 = Float.MAX_VALUE,
      facePoseElement2 = Float.MAX_VALUE;
  private double glassesShiftRatio;
  private double glassesManualAdjustPitchInRadian, glassesManualAdjustPitchInRadianOld;
  private String fileMaskToLoad;

  /**
   * Initialize the model data.
   */
  public UlsRenderer(final Context context) {
    this.context = context;
    parser = new OBJParser(context);
    try {
      InputStream inputStream = context.getAssets().open("ulsdata/lens.obj");
      tdModelPart = parser.parseOBJ(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public enum ImageDataType {
    I420(1), NV21(2), ARGB(3);

    private final int miValue;

    ImageDataType(int value) {
      miValue = value;
    }

    public int getValue() {
      return miValue;
    }
  }

  protected String getVertexShader(String shader) {
    return ToolsUtil.readTextFileFromRawResource(context,
        ResUtils.getResourceId(context, ResUtils.ResType.raw, shader));
  }

  protected String getFragmentShader(String shader) {
    return ToolsUtil.readTextFileFromRawResource(context,
        ResUtils.getResourceId(context, ResUtils.ResType.raw, shader));
  }

  public void ulsSurfaceCreated(GL10 glUnused, EGLConfig config) {
    Log.d("onSurfaceCreated", "GL_RENDERER = " + GLES20.glGetString(GLES20.GL_RENDERER));
    Log.d("onSurfaceCreated", "GL_VENDOR = " + GLES20.glGetString(GLES20.GL_VENDOR));
    Log.d("onSurfaceCreated", "GL_VERSION = " + GLES20.glGetString(GLES20.GL_VERSION));
    Log.i("onSurfaceCreated", "GL_EXTENSIONS = " + GLES20.glGetString(GLES20.GL_EXTENSIONS));

    // Set the background clear color to black.
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    // Position the eye in front of the origin.
    final float eyeX = 0.0f;
    final float eyeY = 0.0f;
    final float eyeZ = -3.5f;

    // We are looking toward the distance
    final float lookX = 0.0f;
    final float lookY = 0.0f;
    final float lookZ = -5.0f;

    // Set our up vector. This is where our head would be pointing were we holding the camera.
    final float upX = 0.0f;
    final float upY = 1.0f;
    final float upZ = 0.0f;

    // Set the view matrix. This matrix can be said to represent the camera position.
    // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
    // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
    Matrix.setLookAtM(Mesh.mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

    final String vertexShader = getVertexShader("per_pixel_vertex_shader");
    final String fragmentShader = getFragmentShader("per_pixel_fragment_shader");

    final int vertexShaderHandle = ToolsUtil.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
    final int fragmentShaderHandle =
        ToolsUtil.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

    programHandle = ToolsUtil.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
        new String[] { "a_Position", "a_Color", "a_TexCoordinate" });

    final String vertexShaderX = getVertexShader("blend_per_pixel_vertex_shader");
    final String fragmentShaderX = getFragmentShader("blend_per_pixel_fragment_shader");

    final int vertexShaderHandleX = ToolsUtil.compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderX);
    final int fragmentShaderHandleX =
        ToolsUtil.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderX);

    programHandleX = ToolsUtil.createAndLinkProgram(vertexShaderHandleX, fragmentShaderHandleX,
        new String[] { "a_PositionX", "a_ColorX", "a_TexCoordinateX", "a_TexCoordinateX2" });
  }

  public void ulsSurfaceChanged(GL10 glUnused, int width, int height) {

  }

  public void reverseCamera(boolean isFront, int width, int height) {

  }

  public void setTrackParam(int width, int height, float[] shape, float[] shapeQuality,
      float[] pupils, float[] gaze, float[] pose, float poseQuality, boolean isFront,
      int cameraRotation) {
    Mesh.isFront = isFront;
    if (pose != null) {
      pose = Arrays.copyOf(pose, pose.length);

      // Modified.
      float fGlasMaxPitchInRadian = GLASSES_MAX_PITCH_WHEN_SHIFT / 180.0f * (float) Math.PI;
      float fGlasCurrPitchInRadian = (float) (glassesShiftRatio * fGlasMaxPitchInRadian);

      // Modified. Set a filter to depress shaking.
      if (facePoseElement0 == Float.MAX_VALUE ||
          Math.abs(pose[0] - facePoseElement0) +
              Math.abs(pose[1] - facePoseElement1) +
              Math.abs(pose[2] - facePoseElement2) > 0.05 ||
          glassesManualAdjustPitchInRadianOld != glassesManualAdjustPitchInRadian) {
        facePoseElement0 = pose[0];
        facePoseElement1 = pose[1];
        facePoseElement2 = pose[2];
        glassesManualAdjustPitchInRadianOld = glassesManualAdjustPitchInRadian;
      }

      for (int i = 0; i < 11; i++) {
        AnimationObject obj = AnimationObjectHolder.getNewAnimationObject(i);
        if (obj.alive) {
          SimplePlane plane = obj.animPlane;
          plane.setVerticesWHScale(obj.animTempMat.cols(), obj.animTempMat.rows());
          if (obj.isRotationNeeded) {
            if (isFront) {
              switch (cameraRotation) {
                case 90:
                case 270:
                  plane.setTrackRotation(pose[0] * 180.0f / (float) Math.PI, (pose[1] +
                          fGlasCurrPitchInRadian +
                          (float) glassesManualAdjustPitchInRadian) * 180.0f / (float) Math.PI,
                      pose[2] * 180.0f / (float) Math.PI - 4);
                  break;
                case 0:
                case 180:
                  plane.setTrackRotation((pose[1] +
                          fGlasCurrPitchInRadian +
                          (float) glassesManualAdjustPitchInRadian) * 180.0f / (float) Math.PI,
                      pose[0] * 180.0f / (float) Math.PI,
                      cameraRotation + 90 + pose[2] * 180.0f / (float) Math.PI - 4);
                  break;
              }
            } else {
              switch (cameraRotation) {
                case 90:
                case 270:
                  plane.setTrackRotation(pose[0] * 180.0f / (float) Math.PI, (pose[1] +
                          fGlasCurrPitchInRadian +
                          (float) glassesManualAdjustPitchInRadian) * 180.0f / (float) Math.PI,
                      180f - (pose[2] * 180.0f / (float) Math.PI - 4));
                  break;
                case 0:
                case 180:
                  plane.setTrackRotation((pose[1] +
                          fGlasCurrPitchInRadian +
                          (float) glassesManualAdjustPitchInRadian) * 180.0f / (float) Math.PI,
                      pose[0] * 180.0f / (float) Math.PI,
                      180f - (cameraRotation + 90 + pose[2] * 180.0f / (float) Math.PI - 4));
                  break;
              }
            }
          } else {
            plane.setTrackRotation(0, 0, cameraRotation);
          }
          if (!obj.useCoordinate) {
            plane.setUlsTrackScale(pose[5] * obj.imgScale * 0.9f);
            try {
              if (cameraRotation == 0) {
                float realX = 2.0f * (1 - shape[2 * obj.pointIndex + 1] / height) - 1.0f;
                float realY = 2.0f * shape[2 * obj.pointIndex] / width - 1.0f;
                plane.setTrackPostion(realX, realY, 0);
              } else if (cameraRotation == 180) {
                float realX = 2.0f * shape[2 * obj.pointIndex + 1] / height - 1.0f;
                float realY = 2.0f * (1 - shape[2 * obj.pointIndex] / width) - 1.0f;
                plane.setTrackPostion(realX, realY, 0);
              } else {
                float realX;
                float realY;
                if (isFront) {
                  realX = (2.0f * shape[2 * obj.pointIndex] / width - 1.0f) * 1280 / 720;
                  realY = 2.0f * shape[2 * obj.pointIndex + 1] / height - 1.0f;
                } else {
                  realX = (2.0f * shape[2 * obj.pointIndex] / width - 1.0f) * 1280 / 720;
                  realY = -(2.0f * shape[2 * obj.pointIndex + 1] / height - 1.0f);
                }
                plane.setTrackPostion(realX, realY, 0);
              }
            } catch (ArrayIndexOutOfBoundsException e) {
              //handle this
            }
          } else {
            plane.setUlsTrackScale(obj.imgScale * 5.5f);
            float glXcoord = (float) (640 - obj.animateAtXcoord) / 360;
            float glYcoord = (float) (360 - obj.animateAtYcoord) / 360;
            plane.setTrackPostion(glXcoord, glYcoord, 0);
          }
          AnimationObjectHolder.setAnimationObjects(i, obj);
        }
      }
    }
  }

  public void setTrackParamNoFace(boolean isFront) {
    Mesh.isFront = isFront;

    for (int i = 0; i < 11; i++) {
      AnimationObject obj = AnimationObjectHolder.getNewAnimationObject(i);
      if (obj.alive && obj.useCoordinate) {
        SimplePlane plane = obj.animPlane;
        plane.setVerticesWHScale(obj.animTempMat.cols(), obj.animTempMat.rows());
        plane.setTrackRotation(0, 0, cameraRotation);
        plane.setUlsTrackScale(obj.imgScale * 5.5f);
        float glXcoord = (float) (640 - obj.animateAtXcoord) / 360;
        float glYcoord = (float) (360 - obj.animateAtYcoord) / 360;
        plane.setTrackPostion(glXcoord, glYcoord, 0);
        AnimationObjectHolder.setAnimationObjects(i, obj);
      }
    }
  }

  public void ulsDrawFrame(GL10 glUnused, int index, float ratioH, boolean withFace) {
    // enable alpha channel
    GLES20.glEnable(GLES20.GL_BLEND);
    GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

    // Set program handles for cube drawing.
    Mesh.mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
    Mesh.mMVMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVMatrix");
    Mesh.mTextureUniformHandle = GLES20.glGetUniformLocation(programHandle, "u_Texture");
    Mesh.mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
    Mesh.mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");
    Mesh.mNoMatPtHandle = GLES20.glGetAttribLocation(programHandle, "a_NoMatrixPt");
    Mesh.mDefMatPtHandle = GLES20.glGetUniformLocation(programHandle, "u_DefMatrix");
    Mesh.mTextureCoordinateHandle = GLES20.glGetAttribLocation(programHandle, "a_TexCoordinate");

    Mesh.mMVPMatrixHandleX = GLES20.glGetUniformLocation(programHandleX, "u_MVPMatrixX");
    Mesh.mTextureUniformHandleX = GLES20.glGetUniformLocation(programHandleX, "u_TextureX");
    Mesh.mTextureUniformHandle2X = GLES20.glGetUniformLocation(programHandleX, "u_Texture2X");
    Mesh.mPositionHandleX = GLES20.glGetAttribLocation(programHandleX, "a_PositionX");
    Mesh.mColorHandleX = GLES20.glGetAttribLocation(programHandleX, "a_ColorX");
    Mesh.mTextureCoordinateHandleX = GLES20.glGetAttribLocation(programHandleX, "a_TexCoordinateX");
    Mesh.mTextureCoordinateHandle2X =
        GLES20.glGetAttribLocation(programHandleX, "a_TexCoordinate2X");

    Mesh.mUniformHandleWH = GLES20.glGetAttribLocation(programHandleX, "u_DevWH");

    Mesh.mEnvRotateHandle = GLES20.glGetAttribLocation(programHandleX, "a_Rotation");
    Mesh.mEnvMoveHandle = GLES20.glGetAttribLocation(programHandleX, "a_Move");
    Mesh.mTextureCenterPosHandle = GLES20.glGetAttribLocation(programHandleX, "a_mid");
    Mesh.mScaleHandle = GLES20.glGetAttribLocation(programHandleX, "a_scale");

    for (int i = 0; i < 11; i++) {
      if (i == 10) {
        continue;
      }
      AnimationObject obj = AnimationObjectHolder.getAnimationObject(i);
      if (obj.alive) {
        if ((!obj.useCoordinate && withFace) || (obj.useCoordinate && index == 0)) {
          switch (cameraRotation) {
            case 90:
              if (cameraInfo.isFrontFacing()) {   //Test OK
                Matrix.orthoM(Mesh.mProjectionMatrix, 0, -1.0f, 1.0f, -1.0f / ratioH, 1.0f / ratioH,
                    -50.0f, 100.0f);
              } else {   //Test OK
                Matrix.orthoM(Mesh.mProjectionMatrix, 0, 1.0f, -1.0f, 1.0f / ratioH, -1.0f / ratioH,
                    -50.0f, 100.0f);
              }
              break;
            case 270:
              if (cameraInfo.isFrontFacing()) {   //Test OK
                Matrix.orthoM(Mesh.mProjectionMatrix, 0, 1.0f, -1.0f, 1.0f / ratioH, -1.0f / ratioH,
                    -50.0f, 100.0f);
              } else {    //Test OK
                Matrix.orthoM(Mesh.mProjectionMatrix, 0, -1.0f, 1.0f, -1.0f / ratioH, 1.0f / ratioH,
                    -50.0f, 100.0f);
              }
              break;
          }

          obj.animPlane.draw(glUnused, programHandle, i);

          switch (cameraRotation) {
            case 90:
              if (cameraInfo.isFrontFacing()) {   //Test OK
                Matrix.orthoM(Mesh.mProjectionMatrix, 0, -1.0f, 1.0f, -1.0f, 1.0f, -50.0f, 100.0f);
              } else {    //Test OK
                Matrix.orthoM(Mesh.mProjectionMatrix, 0, 1.0f, -1.0f, -1.0f, 1.0f, -50.0f, 100.0f);
              }
              break;
            case 270:
              if (cameraInfo.isFrontFacing()) {   //Test OK
                Matrix.orthoM(Mesh.mProjectionMatrix, 0, -1.0f, 1.0f, -1.0f, 1.0f, -50.0f, 100.0f);
              } else {    //Test OK
                Matrix.orthoM(Mesh.mProjectionMatrix, 0, 1.0f, -1.0f, -1.0f, 1.0f, -50.0f, 100.0f);
              }
              break;
          }
        }
      }
    }
  }

  public void setMaskFile(String sFileMask) {
    fileMaskToLoad = sFileMask;
  }

  public String getMaskFile() {
    return fileMaskToLoad;
  }

  private static void loadPngFile(String sFilePath, Mat mat, boolean isFront) {
    File file = new File(sFilePath);
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    Bitmap bmp = BitmapFactory.decodeFile(file.getPath(), options);
    if (bmp == null) {
      return;
    }
    // Flip bitmap vertically.
    android.graphics.Matrix matrix = new android.graphics.Matrix();
    if (isFront) {
      matrix.preScale(1.0f, -1.0f);
    } else {
      matrix.preScale(-1.0f, -1.0f);
    }
    Bitmap bmpFlipped =
        Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

    mat.create(bmpFlipped.getHeight(), bmpFlipped.getWidth(), CvType.CV_8U);
    Utils.bitmapToMat(bmpFlipped, mat);
    bmp.recycle();
    bmpFlipped.recycle();
  }
}
