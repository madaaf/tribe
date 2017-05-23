//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tribe.tribelivesdk.webrtc;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.Surface;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.webrtc.EglBase;
import org.webrtc.EglBase.Context;
import org.webrtc.EglBase10;
import org.webrtc.GlTextureFrameBuffer;
import org.webrtc.GlUtil;
import org.webrtc.Logging;
import org.webrtc.RendererCommon.GlDrawer;
import org.webrtc.ThreadUtils;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRenderer.Callbacks;
import org.webrtc.VideoRenderer.I420Frame;

public class EglRenderer implements Callbacks {
  private static final String TAG = "EglRenderer";
  private static final long LOG_INTERVAL_SEC = 4L;
  private static final int MAX_SURFACE_CLEAR_COUNT = 3;
  private final String name;
  private final Object handlerLock = new Object();
  private Handler renderThreadHandler;
  private final ArrayList<EglRenderer.FrameListenerAndParams> frameListeners = new ArrayList();
  private final Object fpsReductionLock = new Object();
  private long nextFrameTimeNs;
  private long minRenderPeriodNs;
  private EglBase eglBase;
  private final RendererCommon.YuvUploader yuvUploader = new RendererCommon.YuvUploader();
  private GlDrawer drawer;
  private int[] yuvTextures = null;
  private final Object frameLock = new Object();
  private I420Frame pendingFrame;
  private final Object layoutLock = new Object();
  private float layoutAspectRatio;
  private boolean mirror;
  private final Object statisticsLock = new Object();
  private int framesReceived;
  private int framesDropped;
  private int framesRendered;
  private long statisticsStartTimeNs;
  private long renderTimeNs;
  private long renderSwapBufferTimeNs;
  private GlTextureFrameBuffer bitmapTextureFramebuffer;
  private final Runnable renderFrameRunnable = new Runnable() {
    public void run() {
      EglRenderer.this.renderFrameOnRenderThread();
    }
  };
  private final Runnable logStatisticsRunnable = new Runnable() {
    public void run() {
      EglRenderer.this.logStatistics();
      synchronized (EglRenderer.this.handlerLock) {
        if (EglRenderer.this.renderThreadHandler != null) {
          EglRenderer.this.renderThreadHandler.removeCallbacks(
              EglRenderer.this.logStatisticsRunnable);
          EglRenderer.this.renderThreadHandler.postDelayed(EglRenderer.this.logStatisticsRunnable,
              TimeUnit.SECONDS.toMillis(4L));
        }
      }
    }
  };
  private final EglRenderer.EglSurfaceCreation eglSurfaceCreationRunnable =
      new EglRenderer.EglSurfaceCreation();

  public EglRenderer(String name) {
    this.name = name;
  }

  public void init(final Context sharedContext, final int[] configAttributes, GlDrawer drawer) {
    Object var4 = this.handlerLock;
    synchronized (this.handlerLock) {
      if (this.renderThreadHandler != null) {
        throw new IllegalStateException(this.name + "Already initialized");
      } else {
        this.logD("Initializing EglRenderer");
        this.drawer = drawer;
        HandlerThread renderThread = new HandlerThread(this.name + "EglRenderer");
        renderThread.start();
        this.renderThreadHandler = new Handler(renderThread.getLooper());
        ThreadUtils.invokeAtFrontUninterruptibly(this.renderThreadHandler, new Runnable() {
          public void run() {
            if (sharedContext == null) {
              EglRenderer.this.logD("EglBase10.create context");
              EglRenderer.this.eglBase =
                  new EglBase10((org.webrtc.EglBase10.Context) null, configAttributes);
            } else {
              EglRenderer.this.logD("EglBase.create shared context");
              EglRenderer.this.eglBase = EglBase.create(sharedContext, configAttributes);
            }
          }
        });
        this.renderThreadHandler.post(this.eglSurfaceCreationRunnable);
        long currentTimeNs = System.nanoTime();
        this.resetStatistics(currentTimeNs);
        this.renderThreadHandler.postDelayed(this.logStatisticsRunnable,
            TimeUnit.SECONDS.toMillis(4L));
      }
    }
  }

  public void createEglSurface(Surface surface) {
    this.createEglSurfaceInternal(surface);
  }

  public void createEglSurface(SurfaceTexture surfaceTexture) {
    this.createEglSurfaceInternal(surfaceTexture);
  }

  private void createEglSurfaceInternal(Object surface) {
    this.eglSurfaceCreationRunnable.setSurface(surface);
    this.postToRenderThread(this.eglSurfaceCreationRunnable);
  }

  public void release() {
    this.logD("Releasing.");
    final CountDownLatch eglCleanupBarrier = new CountDownLatch(1);
    Object var2 = this.handlerLock;
    synchronized (this.handlerLock) {
      if (this.renderThreadHandler == null) {
        this.logD("Already released");
        return;
      }

      this.renderThreadHandler.removeCallbacks(this.logStatisticsRunnable);
      this.renderThreadHandler.postAtFrontOfQueue(new Runnable() {
        public void run() {
          if (EglRenderer.this.drawer != null) {
            EglRenderer.this.drawer.release();
            EglRenderer.this.drawer = null;
          }

          if (EglRenderer.this.yuvTextures != null) {
            GLES20.glDeleteTextures(3, EglRenderer.this.yuvTextures, 0);
            EglRenderer.this.yuvTextures = null;
          }

          if (EglRenderer.this.bitmapTextureFramebuffer != null) {
            EglRenderer.this.bitmapTextureFramebuffer.release();
            EglRenderer.this.bitmapTextureFramebuffer = null;
          }

          if (EglRenderer.this.eglBase != null) {
            EglRenderer.this.logD("eglBase detach and release.");
            EglRenderer.this.eglBase.detachCurrent();
            EglRenderer.this.eglBase.release();
            EglRenderer.this.eglBase = null;
          }

          eglCleanupBarrier.countDown();
        }
      });
      final Looper renderLooper = this.renderThreadHandler.getLooper();
      this.renderThreadHandler.post(new Runnable() {
        public void run() {
          EglRenderer.this.logD("Quitting render thread.");
          renderLooper.quit();
        }
      });
      this.renderThreadHandler = null;
    }

    ThreadUtils.awaitUninterruptibly(eglCleanupBarrier);
    var2 = this.frameLock;
    synchronized (this.frameLock) {
      if (this.pendingFrame != null) {
        VideoRenderer.renderFrameDone(this.pendingFrame);
        this.pendingFrame = null;
      }
    }

    this.logD("Releasing done.");
  }

  private void resetStatistics(long currentTimeNs) {
    Object var3 = this.statisticsLock;
    synchronized (this.statisticsLock) {
      this.statisticsStartTimeNs = currentTimeNs;
      this.framesReceived = 0;
      this.framesDropped = 0;
      this.framesRendered = 0;
      this.renderTimeNs = 0L;
      this.renderSwapBufferTimeNs = 0L;
    }
  }

  public void printStackTrace() {
    Object var1 = this.handlerLock;
    synchronized (this.handlerLock) {
      Thread renderThread = this.renderThreadHandler == null ? null
          : this.renderThreadHandler.getLooper().getThread();
      if (renderThread != null) {
        StackTraceElement[] renderStackTrace = renderThread.getStackTrace();
        if (renderStackTrace.length > 0) {
          this.logD("EglRenderer stack trace:");
          StackTraceElement[] arr$ = renderStackTrace;
          int len$ = renderStackTrace.length;

          for (int i$ = 0; i$ < len$; ++i$) {
            StackTraceElement traceElem = arr$[i$];
            this.logD(traceElem.toString());
          }
        }
      }
    }
  }

  public void setMirror(boolean mirror) {
    this.logD("setMirror: " + mirror);
    Object var2 = this.layoutLock;
    synchronized (this.layoutLock) {
      this.mirror = mirror;
    }
  }

  public void setLayoutAspectRatio(float layoutAspectRatio) {
    this.logD("setLayoutAspectRatio: " + layoutAspectRatio);
    Object var2 = this.layoutLock;
    synchronized (this.layoutLock) {
      this.layoutAspectRatio = layoutAspectRatio;
    }
  }

  public void setFpsReduction(float fps) {
    this.logD("setFpsReduction: " + fps);
    Object var2 = this.fpsReductionLock;
    synchronized (this.fpsReductionLock) {
      long previousRenderPeriodNs = this.minRenderPeriodNs;
      if (fps <= 0.0F) {
        this.minRenderPeriodNs = 9223372036854775807L;
      } else {
        this.minRenderPeriodNs = (long) ((float) TimeUnit.SECONDS.toNanos(1L) / fps);
      }

      if (this.minRenderPeriodNs != previousRenderPeriodNs) {
        this.nextFrameTimeNs = System.nanoTime();
      }
    }
  }

  public void disableFpsReduction() {
    this.setFpsReduction((float) (1.0f / 0.0));
  }

  public void pauseVideo() {
    this.setFpsReduction(0.0F);
  }

  public void addFrameListener(final EglRenderer.FrameListener listener, final float scale) {
    this.postToRenderThread(new Runnable() {
      public void run() {
        EglRenderer.this.frameListeners.add(
            new EglRenderer.FrameListenerAndParams(listener, scale, EglRenderer.this.drawer));
      }
    });
  }

  public void addFrameListener(final EglRenderer.FrameListener listener, final float scale,
      final GlDrawer drawer) {
    this.postToRenderThread(new Runnable() {
      public void run() {
        EglRenderer.this.frameListeners.add(
            new EglRenderer.FrameListenerAndParams(listener, scale, drawer));
      }
    });
  }

  public void removeFrameListener(final EglRenderer.FrameListener listener) {
    final CountDownLatch latch = new CountDownLatch(1);
    this.postToRenderThread(new Runnable() {
      public void run() {
        latch.countDown();
        Iterator iter = EglRenderer.this.frameListeners.iterator();

        while (iter.hasNext()) {
          if (((EglRenderer.FrameListenerAndParams) iter.next()).listener == listener) {
            iter.remove();
          }
        }
      }
    });
    ThreadUtils.awaitUninterruptibly(latch);
  }

  public void renderFrame(I420Frame frame) {
    Object dropOldFrame = this.statisticsLock;
    synchronized (this.statisticsLock) {
      ++this.framesReceived;
    }

    Object var3 = this.handlerLock;
    boolean dropOldFrame1;
    synchronized (this.handlerLock) {
      if (this.renderThreadHandler == null) {
        this.logD("Dropping frame - Not initialized or already released.");
        VideoRenderer.renderFrameDone(frame);
        return;
      }

      Object var4 = this.fpsReductionLock;
      synchronized (this.fpsReductionLock) {
        if (this.minRenderPeriodNs > 0L) {
          long currentTimeNs = System.nanoTime();
          if (currentTimeNs < this.nextFrameTimeNs) {
            this.logD("Dropping frame - fps reduction is active.");
            VideoRenderer.renderFrameDone(frame);
            return;
          } else {
            this.nextFrameTimeNs += this.minRenderPeriodNs;
            this.nextFrameTimeNs = Math.max(this.nextFrameTimeNs, currentTimeNs);
          }
        }
      }

      var4 = this.frameLock;
      synchronized (this.frameLock) {
        dropOldFrame1 = this.pendingFrame != null;
        if (dropOldFrame1) {
          VideoRenderer.renderFrameDone(this.pendingFrame);
        }

        this.pendingFrame = frame;
        this.renderThreadHandler.post(this.renderFrameRunnable);
      }
    }

    if (dropOldFrame1) {
      var3 = this.statisticsLock;
      synchronized (this.statisticsLock) {
        ++this.framesDropped;
      }
    }
  }

  public void releaseEglSurface(final Runnable completionCallback) {
    this.eglSurfaceCreationRunnable.setSurface((Object) null);
    Object var2 = this.handlerLock;
    synchronized (this.handlerLock) {
      if (this.renderThreadHandler != null) {
        this.renderThreadHandler.removeCallbacks(this.eglSurfaceCreationRunnable);
        this.renderThreadHandler.postAtFrontOfQueue(new Runnable() {
          public void run() {
            if (EglRenderer.this.eglBase != null) {
              EglRenderer.this.eglBase.detachCurrent();
              EglRenderer.this.eglBase.releaseSurface();
            }

            completionCallback.run();
          }
        });
        return;
      }
    }

    completionCallback.run();
  }

  private void postToRenderThread(Runnable runnable) {
    Object var2 = this.handlerLock;
    synchronized (this.handlerLock) {
      if (this.renderThreadHandler != null) {
        this.renderThreadHandler.post(runnable);
      }
    }
  }

  private void clearSurfaceOnRenderThread() {
    if (this.eglBase != null && this.eglBase.hasSurface()) {
      this.logD("clearSurface");
      GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      GLES20.glClear(16384);
      this.eglBase.swapBuffers();
    }
  }

  public void clearImage() {
    Object var1 = this.handlerLock;
    synchronized (this.handlerLock) {
      if (this.renderThreadHandler != null) {
        this.renderThreadHandler.postAtFrontOfQueue(new Runnable() {
          public void run() {
            EglRenderer.this.clearSurfaceOnRenderThread();
          }
        });
      }
    }
  }

  private void renderFrameOnRenderThread() {
    Object startTimeNs = this.frameLock;
    I420Frame frame;
    synchronized (this.frameLock) {
      if (this.pendingFrame == null) {
        return;
      }

      frame = this.pendingFrame;
      this.pendingFrame = null;
    }

    if (this.eglBase != null && this.eglBase.hasSurface()) {
      long var17 = System.nanoTime();
      float[] texMatrix =
          RendererCommon.rotateTextureMatrix(frame.samplingMatrix, (float) frame.rotationDegree);
      Object swapBuffersStartTimeNs = this.layoutLock;
      float[] drawMatrix;
      int drawnFrameWidth;
      int drawnFrameHeight;
      synchronized (this.layoutLock) {
        float[] layoutMatrix;
        if (this.layoutAspectRatio > 0.0F) {
          float currentTimeNs = (float) frame.rotatedWidth() / (float) frame.rotatedHeight();
          layoutMatrix =
              RendererCommon.getLayoutMatrix(this.mirror, currentTimeNs, this.layoutAspectRatio);
          if (currentTimeNs > this.layoutAspectRatio) {
            drawnFrameWidth = (int) ((float) frame.rotatedHeight() * this.layoutAspectRatio);
            drawnFrameHeight = frame.rotatedHeight();
          } else {
            drawnFrameWidth = frame.rotatedWidth();
            drawnFrameHeight = (int) ((float) frame.rotatedWidth() / this.layoutAspectRatio);
          }
        } else {
          layoutMatrix =
              this.mirror ? RendererCommon.horizontalFlipMatrix() : RendererCommon.identityMatrix();
          drawnFrameWidth = frame.rotatedWidth();
          drawnFrameHeight = frame.rotatedHeight();
        }

        drawMatrix = RendererCommon.multiplyMatrices(texMatrix, layoutMatrix);
      }

      GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      GLES20.glClear(16384);
      if (frame.yuvFrame) {
        yuvTextures =
            yuvUploader.uploadYuvData(frame.width, frame.height, frame.yuvStrides, frame.yuvPlanes);
        this.drawer.drawYuv(this.yuvTextures, drawMatrix, drawnFrameWidth, drawnFrameHeight, 0, 0,
            this.eglBase.surfaceWidth(), this.eglBase.surfaceHeight());
      } else {
        this.drawer.drawOes(frame.textureId, drawMatrix, drawnFrameWidth, drawnFrameHeight, 0, 0,
            this.eglBase.surfaceWidth(), this.eglBase.surfaceHeight());
      }

      long var19 = System.nanoTime();
      this.eglBase.swapBuffers();
      long var20 = System.nanoTime();
      Object var12 = this.statisticsLock;
      synchronized (this.statisticsLock) {
        ++this.framesRendered;
        this.renderTimeNs += var20 - var17;
        this.renderSwapBufferTimeNs += var20 - var19;
      }

      this.notifyCallbacks(frame, texMatrix);
      VideoRenderer.renderFrameDone(frame);
    } else {
      this.logD("Dropping frame - No surface");
      VideoRenderer.renderFrameDone(frame);
    }
  }

  private void notifyCallbacks(I420Frame frame, float[] texMatrix) {
    if (!this.frameListeners.isEmpty()) {
      ArrayList tmpList = new ArrayList(this.frameListeners);
      this.frameListeners.clear();
      float[] bitmapMatrix = RendererCommon.multiplyMatrices(
          RendererCommon.multiplyMatrices(texMatrix,
              this.mirror ? RendererCommon.horizontalFlipMatrix()
                  : RendererCommon.identityMatrix()), RendererCommon.verticalFlipMatrix());
      Iterator i$ = tmpList.iterator();

      while (true) {
        while (i$.hasNext()) {
          EglRenderer.FrameListenerAndParams listenerAndParams =
              (EglRenderer.FrameListenerAndParams) i$.next();
          int scaledWidth = (int) (listenerAndParams.scale * (float) frame.rotatedWidth());
          int scaledHeight = (int) (listenerAndParams.scale * (float) frame.rotatedHeight());
          if (scaledWidth != 0 && scaledHeight != 0) {
            if (this.bitmapTextureFramebuffer == null) {
              this.bitmapTextureFramebuffer = new GlTextureFrameBuffer(6408);
            }

            this.bitmapTextureFramebuffer.setSize(scaledWidth, scaledHeight);
            GLES20.glBindFramebuffer('赀', this.bitmapTextureFramebuffer.getFrameBufferId());
            GLES20.glFramebufferTexture2D('赀', '賠', 3553,
                this.bitmapTextureFramebuffer.getTextureId(), 0);
            if (frame.yuvFrame) {
              listenerAndParams.drawer.drawYuv(this.yuvTextures, bitmapMatrix, frame.rotatedWidth(),
                  frame.rotatedHeight(), 0, 0, scaledWidth, scaledHeight);
            } else {
              listenerAndParams.drawer.drawOes(frame.textureId, bitmapMatrix, frame.rotatedWidth(),
                  frame.rotatedHeight(), 0, 0, scaledWidth, scaledHeight);
            }

            ByteBuffer bitmapBuffer = ByteBuffer.allocateDirect(scaledWidth * scaledHeight * 4);
            GLES20.glViewport(0, 0, scaledWidth, scaledHeight);
            GLES20.glReadPixels(0, 0, scaledWidth, scaledHeight, 6408, 5121, bitmapBuffer);
            GLES20.glBindFramebuffer('赀', 0);
            GlUtil.checkNoGLES2Error("EglRenderer.notifyCallbacks");
            Bitmap bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(bitmapBuffer);
            listenerAndParams.listener.onFrame(bitmap);
          } else {
            listenerAndParams.listener.onFrame((Bitmap) null);
          }
        }

        return;
      }
    }
  }

  private String averageTimeAsString(long sumTimeNs, int count) {
    return count <= 0 ? "NA" : TimeUnit.NANOSECONDS.toMicros(sumTimeNs / (long) count) + " μs";
  }

  private void logStatistics() {
    long currentTimeNs = System.nanoTime();
    Object var3 = this.statisticsLock;
    synchronized (this.statisticsLock) {
      long elapsedTimeNs = currentTimeNs - this.statisticsStartTimeNs;
      if (elapsedTimeNs > 0L) {
        float renderFps = (float) ((long) this.framesRendered * TimeUnit.SECONDS.toNanos(1L))
            / (float) elapsedTimeNs;
        this.logD("Duration: "
            + TimeUnit.NANOSECONDS.toMillis(elapsedTimeNs)
            + " ms."
            + " Frames received: "
            + this.framesReceived
            + "."
            + " Dropped: "
            + this.framesDropped
            + "."
            + " Rendered: "
            + this.framesRendered
            + "."
            + " Render fps: "
            + String.format("%.1f", new Object[] { Float.valueOf(renderFps) })
            + "."
            + " Average render time: "
            + this.averageTimeAsString(this.renderTimeNs, this.framesRendered)
            + "."
            + " Average swapBuffer time: "
            + this.averageTimeAsString(this.renderSwapBufferTimeNs, this.framesRendered)
            + ".");
        this.resetStatistics(currentTimeNs);
      }
    }
  }

  private void logD(String string) {
    Logging.d("EglRenderer", this.name + string);
  }

  private class EglSurfaceCreation implements Runnable {
    private Object surface;

    private EglSurfaceCreation() {
    }

    public synchronized void setSurface(Object surface) {
      this.surface = surface;
    }

    public synchronized void run() {
      if (this.surface != null
          && EglRenderer.this.eglBase != null
          && !EglRenderer.this.eglBase.hasSurface()) {
        if (this.surface instanceof Surface) {
          EglRenderer.this.eglBase.createSurface((Surface) this.surface);
        } else {
          if (!(this.surface instanceof SurfaceTexture)) {
            throw new IllegalStateException("Invalid surface: " + this.surface);
          }

          EglRenderer.this.eglBase.createSurface((SurfaceTexture) this.surface);
        }

        EglRenderer.this.eglBase.makeCurrent();
        GLES20.glPixelStorei(3317, 1);
      }
    }
  }

  private static class FrameListenerAndParams {
    public final EglRenderer.FrameListener listener;
    public final float scale;
    public final GlDrawer drawer;

    public FrameListenerAndParams(EglRenderer.FrameListener listener, float scale,
        GlDrawer drawer) {
      this.listener = listener;
      this.scale = scale;
      this.drawer = drawer;
    }
  }

  public interface FrameListener {
    void onFrame(Bitmap var1);
  }
}
