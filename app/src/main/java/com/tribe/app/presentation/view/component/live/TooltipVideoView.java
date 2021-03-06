package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.view.video.TribeMediaPlayer;
import com.tribe.app.presentation.view.widget.video.ScalableVideoView;
import rx.subscriptions.CompositeSubscription;

public class TooltipVideoView extends FrameLayout implements TextureView.SurfaceTextureListener {

  @BindView(R.id.viewVideoScalable) ScalableVideoView viewVideoScalable;

  // VARIABLES
  private TribeMediaPlayer mediaPlayer;
  private SurfaceTexture surfaceTexture;
  private boolean isPaused, shouldResume = false;
  private int videoWidth, videoHeight;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public TooltipVideoView(Context context) {
    this(context, null);
    init(context, null);
  }

  public TooltipVideoView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_auth_video, this, true);
    unbinder = ButterKnife.bind(this);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    initPlayer();
  }

  @Override protected void onDetachedFromWindow() {
    releasePlayer();
    super.onDetachedFromWindow();
  }

  public void onPause(boolean shouldResume) {
    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
      mediaPlayer.pause();
      isPaused = true;
      shouldResume = true;
    }
  }

  public void initPlayer() {
    viewVideoScalable.setSurfaceTextureListener(this);

    mediaPlayer = new TribeMediaPlayer.TribeMediaPlayerBuilder(getContext(),
        "asset:///video/tooltip.mp4").autoStart(true)
        .looping(true)
        .isLocal(true)
        .mute(true)
        .forceLegacy(true)
        .build();

    if (surfaceTexture != null) {
      mediaPlayer.setSurface(surfaceTexture);
      mediaPlayer.prepare();
    }

    subscriptions.add(mediaPlayer.onVideoSizeChanged().subscribe(videoSize -> {
      videoWidth = videoSize.getWidth();
      videoHeight = videoSize.getHeight();
      viewVideoScalable.scaleVideoSize(videoWidth, videoHeight);
    }));
  }

  public void releasePlayer() {
    if (mediaPlayer != null) mediaPlayer.release();

    if (subscriptions != null && subscriptions.hasSubscriptions()) {
      subscriptions.clear();
    }
  }

  public void play() {
    mediaPlayer.play();
    isPaused = false;
  }

  public boolean isPaused() {
    return isPaused;
  }

  @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    surfaceTexture = surface;

    if (mediaPlayer != null) {
      mediaPlayer.setSurface(surface);

      try {
        if (isPaused && shouldResume) {
          play();
          isPaused = false;
          shouldResume = false;
        } else if (isPaused && !shouldResume) {
          mediaPlayer.seekTo(mediaPlayer.getPosition());
        } else if (!isPaused) {
          mediaPlayer.prepare();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    viewVideoScalable.scaleVideoSize(videoWidth, videoHeight);
  }

  @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    surfaceTexture = null;
    return false;
  }

  @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {

  }
}