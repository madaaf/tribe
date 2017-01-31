package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.video.TribeMediaPlayer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 17/02/2016.
 */
public class PlayerView extends FrameLayout implements TextureView.SurfaceTextureListener {

  @BindView(R.id.textureViewLayout) CardView textureViewLayout;

  // VARIABLES
  private VideoTextureView videoTextureView;
  private TribeMediaPlayer mediaPlayer;

  // OBSERVABLES
  private Unbinder unbinder;
  private final PublishSubject<Boolean> videoStarted = PublishSubject.create();
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public PlayerView(Context context) {
    this(context, null);
    init(context, null);
  }

  public PlayerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_player, this, true);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (getWidth() != 0 && getHeight() != 0) {
      FrameLayout.LayoutParams params =
          (FrameLayout.LayoutParams) textureViewLayout.getLayoutParams();
      params.width = getWidth();
      params.height = getHeight();
      textureViewLayout.setRadius(params.width / 2);
      textureViewLayout.setLayoutParams(params);
      textureViewLayout.invalidate();
      textureViewLayout.requestLayout();
    }
  }

  public void createPlayer(String media) {
    videoTextureView = new VideoTextureView(getContext());
    CardView.LayoutParams params = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    textureViewLayout.addView(videoTextureView, params);
    videoTextureView.setScaleType(ScalableTextureView.CENTER_CROP);
    videoTextureView.setSurfaceTextureListener(this);

    mediaPlayer = new TribeMediaPlayer.TribeMediaPlayerBuilder(getContext(), media).autoStart(true)
        .looping(true)
        .mute(true)
        .build();

    subscriptions.add(mediaPlayer.onVideoStarted().subscribe(videoStarted));

    subscriptions.add(mediaPlayer.onPreparedPlayer().subscribe(prepared -> {

    }));

    subscriptions.add(mediaPlayer.onVideoSizeChanged().subscribe(videoSize -> {
      if (videoTextureView != null
          && videoTextureView.getContentHeight() != videoSize.getHeight()) {
        videoTextureView.setContentWidth(videoSize.getWidth() * 2);
        videoTextureView.setContentHeight(videoSize.getHeight() * 2);
        videoTextureView.updateTextureViewSize();
      }

      subscriptions.add(mediaPlayer.onErrorPlayer().subscribe(error -> {
      }));
    }));
  }

  public void releasePlayer() {
    if (mediaPlayer != null) mediaPlayer.release();

    if (subscriptions != null && subscriptions.hasSubscriptions()) {
      subscriptions.clear();
    }
  }

  public void hideVideo() {
    releasePlayer();
    textureViewLayout.removeView(videoTextureView);
    videoTextureView = null;
  }

  public void play() {
    mediaPlayer.play();
  }

  @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    mediaPlayer.setSurface(surface);
    mediaPlayer.prepare();
  }

  @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

  }

  @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    return false;
  }

  @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {

  }

  public Observable<Boolean> videoStarted() {
    return videoStarted;
  }
}