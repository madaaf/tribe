package com.tribe.app.presentation.view.component.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.video.TribeMediaPlayer;
import com.tribe.app.presentation.view.widget.video.ScalableVideoView;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class HomeWalkthroughVideoView extends CardView
    implements TextureView.SurfaceTextureListener {

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewVideoScalable) ScalableVideoView viewVideoScalable;

  // VARIABLES
  private TribeMediaPlayer mediaPlayer;
  private SurfaceTexture surfaceTexture;
  private boolean isPaused, shouldResume = false;
  private int videoWidth, videoHeight;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Integer> onProgress = PublishSubject.create();
  private PublishSubject<Boolean> onCompletion = PublishSubject.create();

  public HomeWalkthroughVideoView(Context context) {
    this(context, null);
    init(context, null);
  }

  public HomeWalkthroughVideoView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_auth_video, this, true);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    setRadius(screenUtils.dpToPx(5));
    setPreventCornerOverlap(false);
    setMaxCardElevation(0);
    setCardElevation(0);
    setCardBackgroundColor(Color.TRANSPARENT);
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
      this.shouldResume = shouldResume;
    }
  }

  public void initPlayer() {
    viewVideoScalable.setSurfaceTextureListener(this);

    mediaPlayer =
        new TribeMediaPlayer.TribeMediaPlayerBuilder(getContext(), "asset:///video/walkthrough.mp4")
            .autoStart(false)
            .looping(false)
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

    subscriptions.add(mediaPlayer.onProgress().subscribe(onProgress));

    subscriptions.add(mediaPlayer.onCompletion().subscribe(onCompletion));
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

  public void seekTo(long position) {
    mediaPlayer.seekTo(position);
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

  /**
   * OBSERVABLES
   */

  public Observable<Integer> onProgress() {
    return onProgress;
  }

  public Observable<Boolean> onCompletion() {
    return onCompletion;
  }
}