/*
 *  Copyright 2015 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.tribe.tribelivesdk.view;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;
import com.tribe.tribelivesdk.webrtc.EglRenderer;
import com.tribe.tribelivesdk.webrtc.RendererCommon;
import com.tribe.tribelivesdk.webrtc.TribeI420Frame;
import java.util.concurrent.CountDownLatch;
import org.webrtc.EglBase;
import org.webrtc.Logging;
import org.webrtc.ThreadUtils;
import org.webrtc.VideoRenderer;

/**
 * Implements org.webrtc.VideoRenderer.Callbacks by displaying the video stream on a SurfaceView.
 * renderFrame() is asynchronous to avoid blocking the calling thread.
 * This class is thread safe and handles access from potentially four different threads:
 * Interaction from the main app in init, release, setMirror, and setScalingtype.
 * Interaction from C++ rtc::VideoSinkInterface in renderFrame.
 * Interaction from the Activity lifecycle in surfaceCreated, surfaceChanged, and surfaceDestroyed.
 * Interaction with the layout framework in onMeasure and onSizeChanged.
 */
public class TextureViewRenderer extends TextureView
    implements TextureView.SurfaceTextureListener, VideoRenderer.Callbacks,
    com.tribe.tribelivesdk.webrtc.VideoRenderer {
  private static final String TAG = "SurfaceViewRenderer";
  // Cached resource name.
  private final String resourceName;
  private final RendererCommon.VideoLayoutMeasure videoLayoutMeasure =
      new RendererCommon.VideoLayoutMeasure();
  private final EglRenderer eglRenderer;
  private final Object layoutLock = new Object();
  // Callback for reporting renderer events. Read-only after initilization so no lock required.
  private RendererCommon.RendererEvents rendererEvents;
  private boolean isFirstFrameRendered;
  private int rotatedFrameWidth, rotatedFrameHeight, frameWidth, frameHeight;
  private int frameRotation;
  // Accessed only on the main thread.
  private boolean enableFixedSize;
  private int surfaceWidth;
  private int surfaceHeight;

  // Detecting FPS DROP
  private int framesRendered, framesRenderedFreeze;
  private long lastRenderingCheck = 0L, lastFreezeCheck = 0L;

  /**
   * Standard View constructor. In order to render something, you must first call init().
   */
  public TextureViewRenderer(Context context) {
    super(context);
    this.resourceName = getResourceName();
    eglRenderer = new EglRenderer(resourceName);
    this.setSurfaceTextureListener(this);
  }

  /**
   * Standard View constructor. In order to render something, you must first call init().
   */
  public TextureViewRenderer(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.resourceName = getResourceName();
    eglRenderer = new EglRenderer(resourceName);
    this.setSurfaceTextureListener(this);
  }

  /**
   * Initialize this class, sharing resources with |sharedContext|. It is allowed to call init() to
   * reinitialize the renderer after a previous init()/release() cycle.
   */
  public void init(EglBase.Context sharedContext, RendererCommon.RendererEvents rendererEvents) {
    GlRectDrawer glRectDrawer = new GlRectDrawer();
    init(sharedContext, rendererEvents, EglBase.CONFIG_PLAIN, glRectDrawer);
  }

  /**
   * Initialize this class, sharing resources with |sharedContext|. The custom |drawer| will be
   * used
   * for drawing frames on the EGLSurface. This class is responsible for calling release() on
   * |drawer|. It is allowed to call init() to reinitialize the renderer after a previous
   * init()/release() cycle.
   */
  public void init(final EglBase.Context sharedContext,
      RendererCommon.RendererEvents rendererEvents, final int[] configAttributes,
      RendererCommon.GlDrawer drawer) {
    ThreadUtils.checkIsOnMainThread();
    this.rendererEvents = rendererEvents;
    synchronized (layoutLock) {
      rotatedFrameWidth = 0;
      rotatedFrameHeight = 0;
      frameHeight = 0;
      frameWidth = 0;
      frameRotation = 0;
    }
    eglRenderer.init(sharedContext, configAttributes, drawer);
  }

  /**
   * Block until any pending frame is returned and all GL resources released, even if an interrupt
   * occurs. If an interrupt occurs during release(), the interrupt flag will be set. This function
   * should be called before the Activity is destroyed and the EGLContext is still valid. If you
   * don't call this function, the GL resources might leak.
   */
  public void release() {
    eglRenderer.release();
  }

  /**
   * Register a callback to be invoked when a new video frame has been received.
   *
   * @param listener The callback to be invoked.
   * @param scale The scale of the Bitmap passed to the callback, or 0 if no Bitmap is
   * required.
   * @param drawer Custom drawer to use for this frame listener.
   */
  public void addFrameListener(EglRenderer.FrameListener listener, float scale,
      final RendererCommon.GlDrawer drawer) {
    eglRenderer.addFrameListener(listener, scale);
  }

  /**
   * Register a callback to be invoked when a new video frame has been received. This version uses
   * the drawer of the EglRenderer that was passed in init.
   *
   * @param listener The callback to be invoked.
   * @param scale The scale of the Bitmap passed to the callback, or 0 if no Bitmap is
   * required.
   */
  public void addFrameListener(EglRenderer.FrameListener listener, float scale) {
    eglRenderer.addFrameListener(listener, scale);
  }

  public void removeFrameListener(EglRenderer.FrameListener listener) {
    eglRenderer.removeFrameListener(listener);
  }

  /**
   * Enables fixed size for the surface. This provides better performance but might be buggy on
   * some
   * devices. By default this is turned off.
   */
  public void setEnableHardwareScaler(boolean enabled) {
    ThreadUtils.checkIsOnMainThread();
    enableFixedSize = enabled;
    updateSurfaceSize();
  }

  /**
   * Set if the video stream should be mirrored or not.
   */
  public void setMirror(final boolean mirror) {
    eglRenderer.setMirror(mirror);
  }

  /**
   * Set how the video will fill the allowed layout area.
   */
  public void setScalingType(RendererCommon.ScalingType scalingType) {
    ThreadUtils.checkIsOnMainThread();
    videoLayoutMeasure.setScalingType(scalingType);
  }

  public void setScalingType(RendererCommon.ScalingType scalingTypeMatchOrientation,
      RendererCommon.ScalingType scalingTypeMismatchOrientation) {
    ThreadUtils.checkIsOnMainThread();
    videoLayoutMeasure.setScalingType(scalingTypeMatchOrientation, scalingTypeMismatchOrientation);
  }

  /**
   * Limit render framerate.
   *
   * @param fps Limit render framerate to this value, or usex Float.POSITIVE_INFINITY to disable
   * fps
   * reduction.
   */
  public void setFpsReduction(float fps) {
    eglRenderer.setFpsReduction(fps);
  }

  public void disableFpsReduction() {
    eglRenderer.disableFpsReduction();
  }

  public void pauseVideo() {
    eglRenderer.pauseVideo();
  }

  // VideoRenderer.Callbacks interface.
  @Override public void renderFrame(VideoRenderer.I420Frame frame) {
    updateFrameDimensionsAndReportEvents(frame);
    eglRenderer.renderFrame(frame);

    framesRendered++;
    framesRenderedFreeze++;
  }

  @Override public void renderFrame(TribeI420Frame frame) {
    VideoRenderer.I420Frame webRtcFrame = frame.getWebRtcI420Frame();
    updateFrameDimensionsAndReportEvents(webRtcFrame);
    eglRenderer.renderFrame(webRtcFrame);

    framesRendered++;
    framesRenderedFreeze++;
  }

  // View layout interface.
  @Override protected void onMeasure(int widthSpec, int heightSpec) {
    ThreadUtils.checkIsOnMainThread();
    final Point size;
    synchronized (layoutLock) {
      size =
          videoLayoutMeasure.measure(widthSpec, heightSpec, rotatedFrameWidth, rotatedFrameHeight);
    }
    setMeasuredDimension(size.x, size.y);
    logD("onMeasure(). New size: " + size.x + "x" + size.y);
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    ThreadUtils.checkIsOnMainThread();
    eglRenderer.setLayoutAspectRatio((right - left) / (float) (bottom - top));
    updateSurfaceSize();
  }

  private void updateSurfaceSize() {
    ThreadUtils.checkIsOnMainThread();
    synchronized (layoutLock) {
      if (enableFixedSize &&
          rotatedFrameWidth != 0 &&
          rotatedFrameHeight != 0 &&
          getWidth() != 0 &&
          getHeight() != 0) {
        final float layoutAspectRatio = getWidth() / (float) getHeight();
        final float frameAspectRatio = rotatedFrameWidth / (float) rotatedFrameHeight;
        final int drawnFrameWidth;
        final int drawnFrameHeight;
        if (frameAspectRatio > layoutAspectRatio) {
          drawnFrameWidth = (int) (rotatedFrameHeight * layoutAspectRatio);
          drawnFrameHeight = rotatedFrameHeight;
        } else {
          drawnFrameWidth = rotatedFrameWidth;
          drawnFrameHeight = (int) (rotatedFrameWidth / layoutAspectRatio);
        }
        // Aspect ratio of the drawn frame and the view is the same.
        final int width = Math.min(getWidth(), drawnFrameWidth);
        final int height = Math.min(getHeight(), drawnFrameHeight);
        logD("updateSurfaceSize. Layout size: " +
            getWidth() +
            "x" +
            getHeight() +
            ", frame size: " +
            rotatedFrameWidth +
            "x" +
            rotatedFrameHeight +
            ", requested surface size: " +
            width +
            "x" +
            height +
            ", old surface size: " +
            surfaceWidth +
            "x" +
            surfaceHeight);
        if (width != surfaceWidth || height != surfaceHeight) {
          surfaceWidth = width;
          surfaceHeight = height;
          //getHolder().setFixedSize(width, height);
        }
      } else {
        surfaceWidth = surfaceHeight = 0;
        //getHolder().setSizeFromLayout();
      }
    }
  }

  private String getResourceName() {
    try {
      return getResources().getResourceEntryName(getId()) + ": ";
    } catch (NotFoundException e) {
      return "";
    }
  }

  // Update frame dimensions and report any changes to |rendererEvents|.
  private void updateFrameDimensionsAndReportEvents(VideoRenderer.I420Frame frame) {
    synchronized (layoutLock) {
      if (!isFirstFrameRendered) {
        isFirstFrameRendered = true;
        logD("Reporting first rendered frame.");
        if (rendererEvents != null) {
          rendererEvents.onFirstFrameRendered();
        }
      }
      if (rotatedFrameWidth != frame.rotatedWidth() ||
          rotatedFrameHeight != frame.rotatedHeight() ||
          frameRotation != frame.rotationDegree) {
        logD("Reporting frame resolution changed to " +
            frame.width +
            "x" +
            frame.height +
            " with rotation " +
            frame.rotationDegree);
        if (rendererEvents != null) {
          rendererEvents.onFrameResolutionChanged(frame.width, frame.height, frame.rotationDegree);
        }
        rotatedFrameWidth = frame.rotatedWidth();
        rotatedFrameHeight = frame.rotatedHeight();
        frameWidth = frame.width;
        frameHeight = frame.height;
        frameRotation = frame.rotationDegree;
        post(() -> {
          updateSurfaceSize();
          requestLayout();
        });
      }
    }
  }

  private void logD(String string) {
    Logging.d(TAG, resourceName + string);
  }

  @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    ThreadUtils.checkIsOnMainThread();
    eglRenderer.createEglSurface(surface);
    surfaceWidth = surfaceHeight = 0;
    updateSurfaceSize();
  }

  @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    ThreadUtils.checkIsOnMainThread();
    rendererEvents.onPreviewSizeChanged(width, height);
  }

  @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    ThreadUtils.checkIsOnMainThread();
    final CountDownLatch completionLatch = new CountDownLatch(1);
    eglRenderer.releaseEglSurface(() -> completionLatch.countDown());
    ThreadUtils.awaitUninterruptibly(completionLatch);
    return true;
  }

  @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {
  }

  public boolean isRenderingWell() {
    boolean isRenderingWell = true;

    if (lastRenderingCheck != 0L) {
      long timeIntervalSinceLastCheck = System.currentTimeMillis() - lastRenderingCheck;
      int framesRenderedSinceLastCheck = framesRendered;
      float timeIntervalInSecs = (timeIntervalSinceLastCheck / 1000);
      if (timeIntervalInSecs > 0) {
        float fps = (framesRenderedSinceLastCheck / timeIntervalInSecs);
        isRenderingWell = fps > 5;
      }
    }

    framesRendered = 0;
    lastRenderingCheck = System.currentTimeMillis();

    return isRenderingWell;
  }

  public boolean isFreeze() {
    boolean isFreeze = false;

    if (lastFreezeCheck != 0L) {
      long timeIntervalSinceLastCheck = System.currentTimeMillis() - lastFreezeCheck;
      int framesRenderedSinceLastCheck = framesRenderedFreeze;
      float timeIntervalInSecs = (timeIntervalSinceLastCheck / 1000);
      if (timeIntervalInSecs > 0) {
        float fps = (framesRenderedSinceLastCheck / timeIntervalInSecs);
        isFreeze = fps == 0;
      }
    }

    framesRenderedFreeze = 0;
    lastFreezeCheck = System.currentTimeMillis();

    return isFreeze;
  }

  public int getRotatedFrameWidth() {
    return rotatedFrameWidth;
  }

  public int getRotatedFrameHeight() {
    return rotatedFrameHeight;
  }

  public int getSurfaceWidth() {
    return surfaceWidth;
  }

  public int getSurfaceHeight() {
    return surfaceHeight;
  }

  public int getFrameHeight() {
    return frameHeight;
  }

  public int getFrameWidth() {
    return frameWidth;
  }
}