package com.tribe.app.presentation.view.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.widget.PulseLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by madaaflak on 15/03/2017.
 */

public class LiveImmersiveNotificationActivity extends BaseActivity {
  public final static String PLAYLOAD_VALUE = "PLAYLOAD_VALUE";

  private final static int ACTION_BUTTON_Y_TRANSLATION = 200;
  private final static int ACTION_BUTTON_DURATION_Y_TRANSLATION = 500;

  private final static int MAX_DURATION_NOTIFICATION = 30;
  private final static int SLOW_TRANSLATION_DURATION = 3000;
  private final static int Y_TRANSLATION = -100;

  private final static int SHAKE_TRANSLATION = 5;
  private final static int SHAKE_DURATION = 20;

  private final static int SCALE_DURATION = 300;
  private final static float SCALE_RATIO_MAX = 1.1f;
  private final static float SCALE_RATIO_MIN = 1f;

  private final Float[] ratioY = new Float[1];
  private float y1, y2;

  @Inject ScreenUtils screenUtils;
  @Inject PaletteGrid paletteGrid;
  @Inject Navigator navigator;
  @Inject SoundManager soundManager;

  @BindView(R.id.txtDisplayName) TextViewFont txtDidplayName;
  @BindView(R.id.callAction) FrameLayout callAction;
  @BindView(R.id.avatar) AvatarView avatar;
  @BindView(R.id.layoutPulse) PulseLayout pulseLayout;
  @BindView(R.id.containerAction) LinearLayout containerAction;
  @BindView(R.id.containerView) ImageView containerView;
  @BindView(R.id.txtSwipeDown) TextViewFont textSwipeDown;

  // VARIABLES
  private Unbinder unbinder;
  NotificationPayload payload = null;

  // OBSERVABLES
  private Subscription startSubscription;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_immersive_call);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    unbinder = ButterKnife.bind(this);
    initDependencyInjector();

    if (savedInstanceState == null) {
      Bundle extras = getIntent().getExtras();
      if (extras == null) {
        payload = null;
      } else {
        payload = (NotificationPayload) extras.getSerializable(PLAYLOAD_VALUE);
        if (payload != null) {
          boolean isGroup = !StringUtils.isEmpty(payload.getGroupId());
          String name = isGroup ? payload.getGroupName() : payload.getUserDisplayName();
          String picture = isGroup ? payload.getGroupPicture() : payload.getUserPicture();
          Glide.with(this).load(picture).centerCrop().into(containerView);
          txtDidplayName.setText(EmojiParser.demojizedText(name));
          avatar.setType(AvatarView.LIVE);
          avatar.load(picture);
        }
      }
    }

    soundManager.playSound(SoundManager.CALL_RING, SoundManager.SOUND_MAX);

    setAnimation();
    setDownCounter();
    callAction.setOnTouchListener(new onTouchJoinButton());
  }

  ////////////////
  // LIFE CYCLE //
  ////////////////

  @Override public void finish() {
    soundManager.cancelMediaPlayer();
    dispose();
    navigator.navigateToHomeAndFinishAffinity(this);
  }

  @Override protected void onResume() {
    super.onResume();
    onResumeLockPhone();
  }

  @Override public void onBackPressed() {
    moveTaskToBack(true);
    super.onBackPressed();
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    if (startSubscription != null) startSubscription.unsubscribe();
    super.onDestroy();
  }

  ////////////////
  //   PRIVATE  //
  ////////////////

  private void dispose() {
    pulseLayout.clearAnimation();
    callAction.clearAnimation();
    containerAction.clearAnimation();
    textSwipeDown.clearAnimation();
  }

  private void setAnimation() {
    pulseLayout.start();
    setTranslationAnim();
    setShakeAnimation();
    setScaleAnimation();
  }

  private void setTranslationAnim() {
    ObjectAnimator translateYAnimation =
        ObjectAnimator.ofFloat(containerAction, "translationY", 0f, Y_TRANSLATION);
    translateYAnimation.setDuration(SLOW_TRANSLATION_DURATION);
    translateYAnimation.setRepeatCount(ValueAnimator.INFINITE);
    translateYAnimation.setRepeatMode(ValueAnimator.REVERSE);
    translateYAnimation.setInterpolator(new LinearInterpolator());
    translateYAnimation.addUpdateListener(animation -> {
      ratioY[0] = (Float) animation.getAnimatedValue() / Y_TRANSLATION;
      if (textSwipeDown == null) {
        return;
      }
      textSwipeDown.setAlpha(ratioY[0]);
    });
    translateYAnimation.start();
  }

  private void setShakeAnimation() {
    ObjectAnimator shakeAnim =
        ObjectAnimator.ofFloat(callAction, "translationX", -SHAKE_TRANSLATION, SHAKE_TRANSLATION);
    shakeAnim.setDuration(SHAKE_DURATION);
    shakeAnim.setRepeatCount(ValueAnimator.INFINITE);
    shakeAnim.setRepeatMode(ValueAnimator.REVERSE);
    shakeAnim.addUpdateListener(
        animation -> animation.setFloatValues(-SHAKE_TRANSLATION * ratioY[0],
            SHAKE_TRANSLATION * ratioY[0]));
    shakeAnim.start();
  }

  private void setScaleAnimation() {
    AnimatorSet resizeAvenger = new AnimatorSet();
    ObjectAnimator animResizeX =
        ObjectAnimator.ofFloat(callAction, "scaleX", SCALE_RATIO_MIN, SCALE_RATIO_MAX);
    animResizeX.setDuration(SCALE_DURATION);
    animResizeX.setRepeatMode(ValueAnimator.REVERSE);
    animResizeX.setRepeatCount(ValueAnimator.INFINITE);
    animResizeX.setInterpolator(new DecelerateInterpolator());

    ObjectAnimator animResizeY =
        ObjectAnimator.ofFloat(callAction, "scaleY", SCALE_RATIO_MIN, SCALE_RATIO_MAX);
    animResizeY.setDuration(SCALE_DURATION);
    animResizeY.setRepeatMode(ValueAnimator.REVERSE);
    animResizeY.setRepeatCount(ValueAnimator.INFINITE);
    animResizeY.setInterpolator(new DecelerateInterpolator());

    animResizeY.addUpdateListener(
        animation -> animation.setFloatValues(SCALE_RATIO_MIN, SCALE_RATIO_MIN + (ratioY[0] / 10)));
    animResizeX.addUpdateListener(
        animation -> animation.setFloatValues(SCALE_RATIO_MIN, SCALE_RATIO_MIN + (ratioY[0] / 10)));

    resizeAvenger.playTogether(animResizeX, animResizeY);
    resizeAvenger.start();
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build();

    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  private void setDownCounter() {
    startSubscription = Observable.timer(MAX_DURATION_NOTIFICATION, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aVoid -> finish());
  }

  private class onTouchJoinButton implements View.OnTouchListener {
    @Override public boolean onTouch(View v, MotionEvent event) {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          y1 = event.getY();
          break;
        case MotionEvent.ACTION_UP:
          y2 = event.getY();
          float deltaY = y2 - y1;

          if (deltaY < 0) {
            containerAction.animate()
                .translationY(-ACTION_BUTTON_Y_TRANSLATION)
                .alpha(0)
                .setDuration(ACTION_BUTTON_DURATION_Y_TRANSLATION)
                .withEndAction(() -> {
                  finish();
                  startActivity(NotificationUtils.getIntentForLive(v.getContext(), payload));
                })
                .start();
          } else {
            containerAction.animate()
                .translationY(ACTION_BUTTON_Y_TRANSLATION)
                .alpha(0)
                .setDuration(ACTION_BUTTON_DURATION_Y_TRANSLATION)
                .withEndAction(() -> finish())
                .start();
          }
          break;
      }

      return false;
    }
  }
}
