package com.tribe.app.presentation.view.component.onboarding;

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
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/09/16.
 */
public class AuthVideoView extends FrameLayout implements TextureView.SurfaceTextureListener {

  private static final String PAUSED = "PAUSED";

  @BindView(R.id.viewVideoScalable) ScalableVideoView viewVideoScalable;

  // VARIABLES
  private TribeMediaPlayer mediaPlayer;
  private SurfaceTexture surfaceTexture;
  private boolean isPaused, shouldResume = false;
  private int videoWidth, videoHeight;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private final PublishSubject<Boolean> videoStarted = PublishSubject.create();
  private final PublishSubject<Boolean> videoCompleted = PublishSubject.create();

  public AuthVideoView(Context context) {
    this(context, null);
    init(context, null);
  }

  public AuthVideoView(Context context, AttributeSet attrs) {
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
        "asset:///video/onboarding_video.mp4").autoStart(true)
        .looping(true)
        .isLocal(true)
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

    subscriptions.add(mediaPlayer.onVideoStarted().subscribe(videoStarted));

    subscriptions.add(mediaPlayer.onCompletion().subscribe(videoCompleted));
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

  public Observable<Boolean> videoStarted() {
    return videoStarted;
  }

  public Observable<Boolean> videoCompleted() {
    return videoCompleted;
  }
}