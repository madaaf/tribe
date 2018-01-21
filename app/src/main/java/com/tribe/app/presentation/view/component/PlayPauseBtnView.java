package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;

/**
 * Created by tiago on 01/18/18
 */
public class PlayPauseBtnView extends FrameLayout {

  private static int ANIM_DURATION = 300;

  public PlayPauseBtnView(Context context) {
    super(context);
  }

  public PlayPauseBtnView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public PlayPauseBtnView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private Unbinder unbinder;

  @BindView(R.id.playBtn) public ImageView playBtn;
  @BindView(R.id.pauseBtn) ImageView pauseBtn;

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    initUi();
  }

  public void initUi() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_play_pause_btn, this);
    unbinder = ButterKnife.bind(this);
  }

  private void resetPauseBtn(boolean hide) {
    float i = 1f;
    if (hide) {
      i = 0f;
    }

    pauseBtn.setScaleX(i);
    pauseBtn.setScaleY(i);
    pauseBtn.setAlpha(i);
  }

  private void resetPlayBtn(boolean hide) {
    float i = 1f;
    if (hide) {
      i = 0f;
    }

    playBtn.setScaleX(i);
    playBtn.setScaleY(i);
    playBtn.setAlpha(i);
  }

  public void switchPauseToPlayBtn(boolean fromPauseToPlay) {
    if (!fromPauseToPlay) {

      playBtn.animate()
          .scaleX(0f)
          .scaleY(0f)
          .rotation(45)
          .setDuration(ANIM_DURATION)
          .alpha(0f)
          .withStartAction(() -> {

            playBtn.setRotation(0);
            resetPlayBtn(false);

            pauseBtn.setRotation(90);
            resetPauseBtn(true);

            pauseBtn.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0)
                .setDuration(ANIM_DURATION)
                .setInterpolator(new AccelerateInterpolator())
                .alpha(1f)
                .withEndAction(() -> {

                })
                .start();
          })
          .start();
    } else {
      pauseBtn.animate()
          .scaleX(0f)
          .scaleY(0f)
          .rotation(0)
          .setDuration(ANIM_DURATION)
          .alpha(0f)
          .withStartAction(() -> {
            pauseBtn.setRotation(90);
            pauseBtn.setScaleX(1);
            pauseBtn.setScaleY(1);
            pauseBtn.setAlpha(1f);

            playBtn.setRotation(45);
            playBtn.setScaleX(0);
            playBtn.setScaleY(0);
            playBtn.setAlpha(0f);

            playBtn.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIM_DURATION)
                .alpha(1f)
                .withEndAction(() -> {

                })
                .start();
          })
          .start();
    }
  }
}
