package com.tribe.tribelivesdk.view.opengl.renderer;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.tribe.tribelivesdk.entity.CameraInfo;
import com.tribe.tribelivesdk.facetracking.UlseeManager;
import com.tribe.tribelivesdk.view.opengl.filter.ColorFilterBW;
import com.tribe.tribelivesdk.view.opengl.filter.ImageFilter;
import com.tribe.tribelivesdk.view.opengl.gles.GlSurfaceTexture;
import com.tribe.tribelivesdk.view.opengl.gles.PreviewTextureInterface;
import com.tribe.tribelivesdk.view.opengl.utils.UlsFaceAR;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.uls.renderer.GLRenderMask;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.microedition.khronos.egl.EGLConfig;
import org.webrtc.CameraEnumerationAndroid;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;

public class PreviewRenderer extends GlFrameBufferObjectRenderer
    implements PreviewTextureInterface.OnFrameAvailableListener {
  private static final String TAG = "PreviewRenderer";

  private static final File FILES_DIR = Environment.getExternalStorageDirectory();

  @NonNull private final Handler mainHandler;
  private Context context;
  private PreviewTextureInterface previewTexture;
  private boolean updateSurface = false;
  private UlseeManager ulseeManager;
  private final float[] mvpMatrix = new float[16], projMatrix = new float[16], matrix =
      new float[16], vMatrix = new float[16], stMatrix = new float[16], stMatrixBis = new float[16];
  private CameraInfo cameraInfo;
  private float cameraRatio = 1, surfaceWidth = 1, surfaceHeight = 1;
  private final RendererCallback rendererCallback;
  private ImageFilter filter;
  private GLRenderMask maskRender;
  private UlsRenderer ulsRenderer;
  private ByteBuffer byteBuffer;
  private Object frameListenerLock = new Object();
  private Frame frame;

  // TO PUT SOMEWHERE ELSE
  private String basePath, maskAndGlassesPath;
  private int maskFrameNumber = 1, stickerFrameNumber = 1;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<SurfaceTexture> onSurfaceTextureReady = PublishSubject.create();
  private PublishSubject<Frame> onFrameAvailable = PublishSubject.create();

  public PreviewRenderer(@NonNull Context context, final RendererCallback callback) {
    super();
    resetMatrix();
    this.context = context;
    rendererCallback = callback;
    mainHandler = new Handler(context.getMainLooper());
    ulseeManager = UlseeManager.getInstance(context);
    basePath = Environment.getExternalStorageDirectory().toString() +
        File.separator +
        "ULSee" +
        File.separator;
    maskAndGlassesPath = basePath + "maskAndGlasses" + File.separator;
    checkFiles();
  }

  private void computeMatrices() {
    final int orientation = (cameraInfo == null) ? 90 : cameraInfo.getFrameOrientation();

    resetMatrix();

    Matrix.setIdentityM(matrix, 0);
    Matrix.rotateM(matrix, 0, orientation, 0.0f, 0.0f, 1f);

    final CameraEnumerationAndroid.CaptureFormat captureFormat = cameraInfo.getCaptureFormat();
    cameraRatio = 1;
    if (captureFormat != null) {
      cameraRatio = captureFormat.width / (float) captureFormat.height;
    }
  }

  public void resetMatrix() {
    Matrix.setIdentityM(stMatrix, 0);
    Matrix.setIdentityM(matrix, 0);
    Matrix.rotateM(matrix, 0, 180, 0.0f, 0.0f, 1.0f);
    Matrix.scaleM(matrix, 0, -1f, 1f, 1f);
    Matrix.setLookAtM(vMatrix, 0, 0.0f, 0.0f, 5.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
  }

  private void checkFiles() {
    File clipartDir = new File(maskAndGlassesPath);
    if (!clipartDir.exists()) {
      clipartDir.mkdirs();
    }

    copyFolder("ulsdata");
  }

  private void copyFolder(String path) {
    AssetManager assetManager = context.getResources().getAssets();
    String[] files = null;
    try {
      files = assetManager.list(path);
    } catch (Exception e) {
      Log.e("read ulsdata ERROR", "" + path + " : " + e.toString());
      e.printStackTrace();
    }
    if (files != null) {
      for (String file : files) {
        InputStream in;
        OutputStream out;
        try {
          File targetFile = new File(maskAndGlassesPath + file);
          if (!targetFile.exists()) {
            in = assetManager.open(path + "/" + file);
            out = new FileOutputStream(maskAndGlassesPath + file);
            copyFile(in, out);
            in.close();
            out.flush();
            out.close();
          }
        } catch (Exception e) {
          Log.e("copy ulsdata ERROR", e.toString());
          e.printStackTrace();
        }
        Log.d("copy ", "" + path + "/" + file);
      }
    }
  }

  private void copyFile(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int read;
    while ((read = in.read(buffer)) != -1) {
      out.write(buffer, 0, read);
    }
  }

  public void computeMask() {
    boolean isFrontFacing = cameraInfo.isFrontFacing();

    String mouthUp = maskAndGlassesPath + "mouthUp.png";
    UlsFaceAR.insertAnimationObjectAtIndex(1, mouthUp, 51, true, 0.3f, isFrontFacing);

    String mouthDown = maskAndGlassesPath + "mouthBottom.png";
    UlsFaceAR.insertAnimationObjectAtIndex(2, mouthDown, 64, true, 0.35f, isFrontFacing);

    String glasses = maskAndGlassesPath + "sunglass_newyear.png";
    UlsFaceAR.insertAnimationObjectAtIndex(3, glasses, 27, true, 0.6f, isFrontFacing);

    String cheek = maskAndGlassesPath + "cosmetic_new.png";
    UlsFaceAR.insertAnimationObjectAtIndex(4, cheek, 29, true, 1f, isFrontFacing);

    String chickHead =
        maskAndGlassesPath + "ChickenHead" + Integer.toString(maskFrameNumber) + ".png";
    UlsFaceAR.insertAnimationObjectAtIndex(5, chickHead, 91, true, 0.5f, isFrontFacing);
    maskFrameNumber = maskFrameNumber < 7 ? maskFrameNumber + 1 : 1;

    stickerFrameNumber = 1;
    UlsFaceAR.cleanAnimationObjectAtIndex(6);
  }

  @Nullable public ImageFilter getFilter() {
    return filter;
  }

  public void setFilter(@Nullable final ImageFilter shader) {
    if (shader != null && shader.equals(this.filter)) {
      return;
    }

    if (this.filter != null) {
      this.filter.release();
    }

    this.filter = shader;
    rendererCallback.requestRender();
  }

  public void updateCameraInfo(CameraInfo cameraInfo) {
    this.cameraInfo = cameraInfo;
    computeMatrices();
    computeMask();

    if (ulsRenderer != null) ulsRenderer.updateCameraInfo(cameraInfo);
  }

  public void onStartPreview() {
    computeMatrices();

    if (previewTexture == null) {
      previewTexture = new GlSurfaceTexture();
      previewTexture.setOnFrameAvailableListener(this);
      previewTexture.onSurfaceTextureReady().subscribe(onSurfaceTextureReady);
    }

    previewTexture.setup();
  }

  @Override public void onSurfaceCreated(final EGLConfig config) {
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    filter = new ColorFilterBW();

    resetMatrix();

    synchronized (this) {
      updateSurface = false;
    }

    maskRender = new GLRenderMask(context);
    ulsRenderer = UlsRenderer.getInstance(context);
    ulsRenderer.ulsSurfaceCreated(null, null);
    if (cameraInfo != null) ulsRenderer.updateCameraInfo(cameraInfo);

    mainHandler.post(() -> rendererCallback.onRendererInitialized());
  }

  private float stageRatio = Float.MIN_VALUE;

  @Override public void onSurfaceChanged(final int width, final int height) {
    surfaceWidth = width;
    surfaceHeight = height;

    ulsRenderer.ulsSurfaceChanged(null, width, height);

    byteBuffer = ByteBuffer.allocateDirect((int) surfaceWidth * (int) surfaceHeight * 4);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

    stageRatio = (stageRatio == Float.MIN_VALUE) ? width / (float) height : stageRatio;
    try {
      float zoom = 4f;
      Matrix.frustumM(projMatrix, 0, -stageRatio, stageRatio, -1, 1, zoom, 25 * zoom);
    } catch (Exception ignored) {
      Timber.e("onSurfaceChanged exception", ignored);
    }

    if (rendererCallback != null) {
      rendererCallback.onSurfaceChanged(width, height);
    }
  }

  @Override public synchronized void onDrawFrame() {
    if (updateSurface && previewTexture != null) {
      previewTexture.updateTexImage();
      previewTexture.getTransformMatrix(stMatrix);
      updateSurface = false;
    }

    glClear(GL_COLOR_BUFFER_BIT);
    Matrix.multiplyMM(mvpMatrix, 0, vMatrix, 0, matrix, 0);
    Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvpMatrix, 0);

    if (previewTexture != null && byteBuffer != null) {
      filter.draw(previewTexture, mvpMatrix, stMatrix, cameraRatio);

      int rotation = ulseeManager.getCameraRotation();
      if (rotation != 90 && rotation != 270) rotation = 180 - ulseeManager.getCameraRotation();

      for (int i = 0; i < UlseeManager.MAX_TRACKER; i++) {
        draw(i, rotation);
      }
      //synchronized (frameListenerLock) {
      // //TODO not efficient enough, find another way to grab the frames, maybe through JNI?
      //  byteBuffer.rewind();
      //  long start = System.currentTimeMillis();
      //
      //  //  Timber.d("glReadPixels: " + (end - start));
      //
      //  int width = (int) surfaceWidth;
      //  int height = (int) surfaceHeight;
      //  GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
      //      byteBuffer);
      //  byteBuffer.flip();
      //  long end = System.currentTimeMillis();
      //  //Timber.d("glReadPixels: " + (end - start));
      //
      //  //BufferedOutputStream bos = null;
      //  //try {
      //  //  File file =
      //  //      new File(FILES_DIR, String.format("frame-" + System.currentTimeMillis() + ".png"));
      //  //  bos = new BufferedOutputStream(new FileOutputStream(file.toString()));
      //  //  Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      //  //  byteBuffer.rewind();
      //  //  bmp.copyPixelsFromBuffer(byteBuffer);
      //  //  bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
      //  //  bmp.recycle();
      //  //} catch (FileNotFoundException e) {
      //  //  e.printStackTrace();
      //  //} finally {
      //  //  if (bos != null) {
      //  //    try {
      //  //      bos.close();
      //  //    } catch (IOException e) {
      //  //      e.printStackTrace();
      //  //    }
      //  //  }
      //  //}
      //  frame = new Frame(byteBuffer.array(), width, height, 0, previewTexture.getTimestamp(),
      //      cameraInfo.isFrontFacing());
      //  onFrameAvailable.onNext(frame);
      //}
    }
  }

  public void draw(int index, int rotation) {
    //float ratioH =
    //    (float) cameraInfo.getCaptureFormat().height / cameraInfo.getCaptureFormat().width;
    float ratioH = surfaceWidth / surfaceHeight;
    float[][] shape = ulseeManager.getShape();
    float[][] pose = ulseeManager.getPose();
    float[][] confidence = ulseeManager.getConfidence();
    float[] poseQuality = ulseeManager.getPoseQuality();
    float[][] gaze = ulseeManager.getGaze();
    float[][] pupils = ulseeManager.getPupils();
    int cameraRotation = ulseeManager.getCameraRotation();
    boolean isFrontFacing = cameraInfo.isFrontFacing();

    if (shape[index] == null) {
      ulsRenderer.setTrackParamNoFace(isFrontFacing);
      ulsRenderer.ulsDrawFrame(null, index, ratioH, false);
    } else {
      if (pose != null && poseQuality[index] > 0.0f) {
        maskRender.drawMask(shape[index], confidence[index], 5.0f, rotation,
            cameraInfo.getCaptureFormat().width, cameraInfo.getCaptureFormat().height,
            ulsRenderer.getMaskFile(), isFrontFacing);
        if (isFrontFacing) {
          ulsRenderer.setTrackParam(cameraInfo.getCaptureFormat().width,
              cameraInfo.getCaptureFormat().height, shape[index], confidence[index], pupils[index],
              gaze[index], pose[index], poseQuality[index], isFrontFacing, cameraRotation);
        } else {
          float[] flippedFaceShape = new float[99 * 2];
          flipFaceShape(flippedFaceShape, shape[index]);

          ulsRenderer.setTrackParam(cameraInfo.getCaptureFormat().width,
              cameraInfo.getCaptureFormat().height, flippedFaceShape, confidence[index],
              pupils[index], gaze[index], pose[index], poseQuality[index], isFrontFacing,
              cameraRotation);
        }

        ulsRenderer.ulsDrawFrame(null, index, ratioH, true);
      }

      if (isFrontFacing) {
        ulsRenderer.setTrackParam(cameraInfo.getCaptureFormat().width,
            cameraInfo.getCaptureFormat().height, shape[index], confidence[index], pupils[index],
            gaze[index], pose[index], poseQuality[index], isFrontFacing, cameraRotation);
      } else {
        float[] flippedFaceShape = new float[99 * 2];
        flipFaceShape(flippedFaceShape, shape[index]);

        ulsRenderer.setTrackParam(cameraInfo.getCaptureFormat().width,
            cameraInfo.getCaptureFormat().height, flippedFaceShape, confidence[index],
            pupils[index], gaze[index], pose[index], poseQuality[index], isFrontFacing,
            cameraRotation);
      }
    }

    GLES20.glFlush();
  }

  private void flipFaceShape(float[] flippedFaceShape, float[] oriFaceShape) {
    if (oriFaceShape == null) return;
    for (int i = 0; i < 99; i++) {
      flippedFaceShape[i * 2] = cameraInfo.getCaptureFormat().width - oriFaceShape[i * 2];
      flippedFaceShape[i * 2 + 1] = cameraInfo.getCaptureFormat().height - oriFaceShape[i * 2 + 1];
    }
  }

  @Override
  public synchronized void onFrameAvailable(final PreviewTextureInterface previewTexture) {
    updateSurface = true;
    rendererCallback.requestRender();
  }

  public interface RendererCallback {
    void onRendererInitialized();

    void requestRender();

    void onSurfaceChanged(final int width, final int height);
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<SurfaceTexture> onSurfaceTextureReady() {
    return onSurfaceTextureReady;
  }

  public Observable<Frame> onFrameAvailable() {
    return onFrameAvailable;
  }
}
