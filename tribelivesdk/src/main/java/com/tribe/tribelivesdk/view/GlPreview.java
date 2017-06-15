package com.tribe.tribelivesdk.view;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.tribelivesdk.webrtc.RendererCommon;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;
import timber.log.Timber;

public abstract class GlPreview extends ViewGroup {

  /**
   * The scaling type to be utilized by default.
   *
   * The default value is in accord with
   * https://www.w3.org/TR/html5/embedded-content-0.html#the-video-element:
   *
   * In the absence of style rules to the contrary, video content should be
   * rendered inside the element's playback area such that the video content
   * is shown centered in the playback area at the largest possible size that
   * fits completely within it, with the video content's aspect ratio being
   * preserved. Thus, if the aspect ratio of the playback area does not match
   * the aspect ratio of the video, the video will be shown letterboxed or
   * pillarboxed. Areas of the element's playback area that do not contain the
   * video represent nothing.
   */
  protected static final RendererCommon.ScalingType DEFAULT_SCALING_TYPE =
      RendererCommon.ScalingType.SCALE_ASPECT_FILL;
  /**
   * {@link View#isInLayout()} as a <tt>Method</tt> to be invoked via
   * reflection in order to accommodate its lack of availability before API
   * level 18. {@link ViewCompat#isInLayout(View)} is the best solution but I
   * could not make it available along with
   * {@link ViewCompat#isAttachedToWindow(View)} at the time of this writing.
   */
  protected static final Method IS_IN_LAYOUT;

  static {
    // IS_IN_LAYOUT
    Method isInLayout = null;

    try {
      Method m = GlPreview.class.getMethod("isInLayout");
      if (boolean.class.isAssignableFrom(m.getReturnType())) {
        isInLayout = m;
      }
    } catch (NoSuchMethodException e) {
      // Fall back to the behavior of ViewCompat#isInLayout(View).
    }

    IS_IN_LAYOUT = isInLayout;
  }

  /**
   * The {@code Object} which synchronizes the access to the layout-related
   * state of this instance such as {@link #frameHeight},
   * {@link #frameRotation}, {@link #frameWidth}, and {@link #scalingType}.
   */
  protected final Object layoutSyncRoot = new Object();
  /**
   * The height of the last video frame rendered by
   * {@link #textureViewRenderer}.
   */
  protected int frameHeight;
  /**
   * The rotation (degree) of the last video frame rendered by
   * {@link #textureViewRenderer}.
   */
  protected int frameRotation;
  /**
   * The width of the last video frame rendered by
   * {@link #textureViewRenderer}.
   */
  protected int frameWidth;

  protected boolean mirror;

  protected RendererCommon.ScalingType scalingType;

  protected TextureViewRenderer textureViewRenderer;

  /**
   * The {@code VideoRenderer}, if any, which renders {@link #videoTrack} on
   * this {@code View}.
   */
  protected VideoRenderer remoteRenderer;

  /**
   * The {@code VideoTrack}, if any, rendered by this {@code PeerView}.
   */
  protected VideoTrack videoTrack;

  /**
   * Stops rendering {@link #videoTrack} and releases the associated acquired
   * resources (if rendering is in progress).
   */
  protected void removeRendererFromVideoTrack() {
    if (remoteRenderer != null) {
      Timber.d("Disposing renderer from video track");
      if (videoTrack != null) {
        Timber.d("Removing videoRenderer from videoTrack");
        videoTrack.removeRenderer(remoteRenderer);
      }
      Timber.d("videoRenderer dispose");
      remoteRenderer.dispose();
      remoteRenderer = null;
    }
  }

  protected void releaseTexture() {
    Timber.d("Releasing texture");
    getTextureViewRenderer().release();

    // Since this PeerView is no longer rendering anything, make sure
    // surfaceViewRenderer displays nothing as well.
    synchronized (layoutSyncRoot) {
      frameHeight = 0;
      frameRotation = 0;
      frameWidth = 0;
    }

    Timber.d("Request renderer layout");
    requestTextureViewRendererLayout();
    Timber.d("End disposing renderer from video track");
  }

  /**
   * The {@code Runnable} representation of
   * {@link #requestTextureViewRendererLayout()} ()}. Explicitly defined in order
   * to allow the use of the latter with {@link #post(Runnable)} without
   * initializing new instances on every (method) call.
   */
  protected final Runnable requestSurfaceViewRendererLayoutRunnable =
      () -> requestTextureViewRendererLayout();

  /**
   * The {@code RendererEvents} which listens to rendering events reported by
   * {@link #textureViewRenderer}.
   */
  protected final RendererCommon.RendererEvents rendererEvents =
      new RendererCommon.RendererEvents() {
        @Override public void onFirstFrameRendered() {
          GlPreview.this.onFirstFrameRendered();
        }

        @Override
        public void onFrameResolutionChanged(int videoWidth, int videoHeight, int rotation) {
          GlPreview.this.onFrameResolutionChanged(videoWidth, videoHeight, rotation);
        }

        @Override public void onPreviewSizeChanged(int width, int height) {
          GlPreview.this.onPreviewSizeChanged(width, height);
        }
      };

  public GlPreview(Context context) {
    super(context);
    textureViewRenderer = new TextureViewRenderer(context);
    init(context);
  }

  public GlPreview(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    textureViewRenderer = new TextureViewRenderer(context);
    init(context);
  }

  protected void init(Context context) {
    addView(textureViewRenderer);
    setMirror(false);
    setScalingType(DEFAULT_SCALING_TYPE);
  }

  protected final TextureViewRenderer getTextureViewRenderer() {
    return textureViewRenderer;
  }

  /**
   * If this <tt>View</tt> has {@link View#isInLayout()}, invokes it and
   * returns its return value; otherwise, returns <tt>false</tt> like
   * {@link ViewCompat#isInLayout(View)}.
   *
   * @return If this <tt>View</tt> has <tt>View#isInLayout()</tt>, invokes it
   * and returns its return value; otherwise, returns <tt>false</tt>.
   */
  protected boolean invokeIsInLayout() {
    Method m = IS_IN_LAYOUT;
    boolean b = false;

    if (m != null) {
      try {
        b = (boolean) m.invoke(this);
      } catch (IllegalAccessException e) {
        // Fall back to the behavior of ViewCompat#isInLayout(View).
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    return b;
  }

  /**
   * Callback fired by {@link #textureViewRenderer} when the resolution or
   * rotation of the frame it renders has changed.
   *
   * @param videoWidth The new width of the rendered video frame.
   * @param videoHeight The new height of the rendered video frame.
   * @param rotation The new rotation of the rendered video frame.
   */
  protected void onFrameResolutionChanged(int videoWidth, int videoHeight, int rotation) {
    boolean changed = false;

    synchronized (layoutSyncRoot) {
      if (this.frameHeight != videoHeight) {
        this.frameHeight = videoHeight;
        changed = true;
      }
      if (this.frameRotation != rotation) {
        this.frameRotation = rotation;
        changed = true;
      }
      if (this.frameWidth != videoWidth) {
        this.frameWidth = videoWidth;
        changed = true;
      }
    }
    if (changed) {
      // The onFrameResolutionChanged method call executes on the
      // surfaceViewRenderer's render Thread.
      post(requestSurfaceViewRendererLayoutRunnable);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    int height = b - t;
    int width = r - l;

    if (height == 0 || width == 0) {
      l = t = r = b = 0;
    } else {
      int frameHeight;
      int frameRotation;
      int frameWidth;
      RendererCommon.ScalingType scalingType;

      synchronized (layoutSyncRoot) {
        frameHeight = this.frameHeight;
        frameRotation = this.frameRotation;
        frameWidth = this.frameWidth;
        scalingType = this.scalingType;
      }

      TextureViewRenderer textureViewRenderer = getTextureViewRenderer();

      switch (scalingType) {
        case SCALE_ASPECT_FILL:
          // Fill this ViewGroup with surfaceViewRenderer and the latter
          // will take care of filling itself with the video similarly to
          // the cover value the CSS property object-fit.
          r = width;
          l = 0;
          b = height;
          t = 0;
          break;
        case SCALE_ASPECT_FIT:
        default:
          // Lay surfaceViewRenderer out inside this ViewGroup in accord
          // with the contain value of the CSS property object-fit.
          // SurfaceViewRenderer will fill itself with the video similarly
          // to the cover or contain value of the CSS property object-fit
          // (which will not matter, eventually).
          if (frameHeight == 0 || frameWidth == 0) {
            l = t = r = b = 0;
          } else {
            float frameAspectRatio = (frameRotation % 180 == 0) ? frameWidth / (float) frameHeight
                : frameHeight / (float) frameWidth;
            Point frameDisplaySize =
                RendererCommon.getDisplaySize(scalingType, frameAspectRatio, width, height);

            l = (width - frameDisplaySize.x) / 2;
            t = (height - frameDisplaySize.y) / 2;
            r = l + frameDisplaySize.x;
            b = t + frameDisplaySize.y;
          }
          break;
      }
    }
    textureViewRenderer.layout(l, t, r, b);
  }

  /**
   * Request that {@link #textureViewRenderer} be laid out (as soon as
   * possible) because layout-related state either of this instance or of
   * {@code surfaceViewRenderer} has changed.
   */
  protected void requestTextureViewRendererLayout() {
    // Google/WebRTC just call requestLayout() on surfaceViewRenderer when
    // they change the value of its mirror or surfaceType property.
    getTextureViewRenderer().requestLayout();
    // The above is not enough though when the video frame's dimensions or
    // rotation change. The following will suffice.
    if (!invokeIsInLayout()) {
      layout(getLeft(), getTop(), getRight(), getBottom());
    }
  }

  public void setMirror(boolean mirror) {
    if (this.mirror != mirror) {
      this.mirror = mirror;

      TextureViewRenderer textureViewRenderer = getTextureViewRenderer();

      textureViewRenderer.setMirror(mirror);
      // SurfaceViewRenderer takes the value of its mirror property into
      // account upon its layout.
      requestTextureViewRendererLayout();
    }
  }

  /**
   * In the fashion of
   * https://www.w3.org/TR/html5/embedded-content-0.html#dom-video-videowidth
   * and https://www.w3.org/TR/html5/rendering.html#video-object-fit,
   * resembles the CSS style {@code object-fit}.
   *
   * @param objectFit For details, refer to the documentation of the
   * {@code objectFit} property of the JavaScript counterpart of
   * {@code PeerView} i.e. {@code RTCView}.
   */
  public void setObjectFit(String objectFit) {
    RendererCommon.ScalingType scalingType =
        "cover".equals(objectFit) ? RendererCommon.ScalingType.SCALE_ASPECT_FILL
            : RendererCommon.ScalingType.SCALE_ASPECT_FIT;

    setScalingType(scalingType);
  }

  protected void setScalingType(RendererCommon.ScalingType scalingType) {
    TextureViewRenderer textureViewRenderer;

    synchronized (layoutSyncRoot) {
      if (this.scalingType == scalingType) {
        return;
      }

      this.scalingType = scalingType;

      textureViewRenderer = getTextureViewRenderer();
      textureViewRenderer.setScalingType(scalingType);
    }
    // Both this instance ant its SurfaceViewRenderer take the value of
    // their scalingType properties into account upon their layouts.
    requestTextureViewRendererLayout();
  }

  public VideoRenderer getRemoteRenderer() {
    return remoteRenderer;
  }

  public void dispose() {
    removeRendererFromVideoTrack();
    releaseTexture();
  }

  public abstract void onFirstFrameRendered();

  public abstract void onPreviewSizeChanged(int width, int height);
}
