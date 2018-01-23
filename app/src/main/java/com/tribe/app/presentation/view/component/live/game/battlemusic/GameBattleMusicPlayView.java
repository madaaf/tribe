package com.tribe.app.presentation.view.component.live.game.battlemusic;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.battlemusic.BattleMusicTrack;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.component.PlayPauseBtnView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.StreamAudioPlayer;
import com.tribe.app.presentation.view.widget.CircularProgressBar;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/18/2018
 */

public class GameBattleMusicPlayView extends FrameLayout {

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewBgProgress) View viewBgProgress;
  @BindView(R.id.viewBgBlack) View viewBgBlack;
  @BindView(R.id.viewBgGreen) View viewBgGreen;
  @BindView(R.id.viewPlayPauseBtn) PlayPauseBtnView viewPlayPauseBtn;
  @BindView(R.id.progressBar) CircularProgressView progressBar;
  @BindView(R.id.txtCount) TextViewFont txtCount;
  @BindView(R.id.progressBarPlay) CircularProgressBar progressBarPlay;

  // VARIABLES
  private Unbinder unbinder;
  private boolean pause = true;
  private StreamAudioPlayer audioPlayer;
  private BattleMusicTrack track;
  private boolean buffered = false;
  private ValueAnimator animatorProgress;
  private int currentProgress = 0;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onResume = PublishSubject.create();
  private PublishSubject<Void> onStarted = PublishSubject.create();
  private PublishSubject<Void> onPause = PublishSubject.create();

  public GameBattleMusicPlayView(@NonNull Context context) {
    super(context);
  }

  public GameBattleMusicPlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    initResources();
    initDependencyInjector();
    initUI();
    initSubscriptions();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  private void initResources() {

  }

  private void initDependencyInjector() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
  }

  private void initUI() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_game_battlemusic_play, this);
    unbinder = ButterKnife.bind(this);

    GradientDrawable background = new GradientDrawable();
    background.setShape(GradientDrawable.OVAL);
    background.setColor(ContextCompat.getColor(getContext(), R.color.white_opacity_10));
    viewBgProgress.setBackground(background);

    background = new GradientDrawable();
    background.setShape(GradientDrawable.OVAL);
    background.setColor(ContextCompat.getColor(getContext(), R.color.black_almost));
    viewBgBlack.setBackground(background);

    background = new GradientDrawable();
    background.setShape(GradientDrawable.OVAL);
    background.setColor(ContextCompat.getColor(getContext(), R.color.green_status));
    viewBgGreen.setBackground(background);

    audioPlayer = new StreamAudioPlayer(getContext());

    viewPlayPauseBtn.setEnabled(false);

    progressBarPlay.setProgressColor(
        ContextCompat.getColor(getContext(), R.color.green_battlemusic));
    progressBarPlay.setProgressWidth(screenUtils.dpToPx(10));
    progressBarPlay.setProgress(0);
  }

  private void initSubscriptions() {

  }

  /**
   * ON CLICK
   */

  @OnClick(R.id.viewPlayPauseBtn) void click() {
    viewPlayPauseBtn.switchPauseToPlayBtn(!pause);

    if (pause) {
      startProgress();
      audioPlayer.play();
      pause = !pause;
      onResume.onNext(null);
    } else {
      stopProgress();
      audioPlayer.stop();
      pause = !pause;
      onPause.onNext(null);
    }
  }

  private void startProgress() {
    progressBarPlay.setProgress(
        (int) ((float) 100 / (float) audioPlayer.getDuration() * audioPlayer.getPosition()));
    progressBarPlay.setProgress(100, (int) (audioPlayer.getDuration() - audioPlayer.getPosition()), 0, null, null);
  }

  private void stopProgress() {
    progressBarPlay.stop();
  }

  /**
   * PUBLIC
   */

  public void dispose() {
    audioPlayer.dispose();
    progressBarPlay.stop();
    subscriptions.clear();
  }

  public void initTrack(BattleMusicTrack track) {
    this.track = track;
    subscriptions.clear();
    buffered = false;
    audioPlayer.prepare(track.getUrl());
    viewPlayPauseBtn.setVisibility(View.GONE);
    progressBar.setVisibility(VISIBLE);
    viewPlayPauseBtn.setEnabled(false);
    progressBarPlay.setProgress(0);

    subscriptions.add(audioPlayer.onDonePlaying().subscribe(aBoolean -> {
      pause = true;
      viewPlayPauseBtn.switchPauseToPlayBtn(true);
      audioPlayer.stop();
      audioPlayer.reset();
    }));
  }

  public void play() {
    if (!pause) return;

    viewPlayPauseBtn.switchPauseToPlayBtn(!pause);
    audioPlayer.play();
    pause = !pause;
    startProgress();
  }

  public void stop() {
    if (pause) return;

    viewPlayPauseBtn.switchPauseToPlayBtn(!pause);
    audioPlayer.stop();
    pause = !pause;
    stopProgress();
  }

  public void showDone() {
    setLayoutTransition(new LayoutTransition());
  }

  public void hide() {
    setLayoutTransition(null);
  }

  public void start() {
    viewPlayPauseBtn.switchPauseToPlayBtn(!pause);
    progressBar.setVisibility(GONE);
    txtCount.setVisibility(VISIBLE);

    subscriptions.add(Observable.timer(1, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> txtCount.setText("3")));

    subscriptions.add(Observable.timer(2, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> txtCount.setText("2")));

    subscriptions.add(Observable.timer(3, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> txtCount.setText("1")));

    subscriptions.add(Observable.timer(4, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          txtCount.setText("");
          viewPlayPauseBtn.setVisibility(View.VISIBLE);
          txtCount.setVisibility(View.GONE);
          onStarted.onNext(null);
          viewPlayPauseBtn.setEnabled(true);
        }));
  }

  /**
   * OBSERVABLES
   */

  public Observable<Boolean> onBuffered() {
    return audioPlayer.onBuffered().doOnNext(aBoolean -> buffered = true);
  }

  public Observable<Boolean> onDonePlaying() {
    return audioPlayer.onDonePlaying();
  }

  public Observable<Void> onStarted() {
    return onStarted;
  }

  public Observable<Void> onPause() {
    return onPause;
  }

  public Observable<Void> onResume() {
    return onResume;
  }
}
