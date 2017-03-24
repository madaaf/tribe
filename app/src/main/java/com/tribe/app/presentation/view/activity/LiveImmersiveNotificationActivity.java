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
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.GlideUtils;
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

  private final static int ACTION_BUTTON_DURATION_Y_TRANSLATION = 500;
  private final static int ACTION_BUTTON_Y_TRANSLATION = 200;

  private final static int MAX_DURATION_NOTIFICATION = 30;
  private final static int SLOW_TRANSLATION_DURATION = 3000;
  private final static int Y_TRANSLATION = -100;
  private final static int SHAKE_TRANSLATION = 5;

  private float yTranslation = 0;
  private float y1, y2;

  @Inject ScreenUtils screenUtils;
  @Inject PaletteGrid paletteGrid;
  @Inject Navigator navigator;
  @Inject SoundManager soundManager;

  @BindView(R.id.txtDidplayName) TextViewFont txtDidplayName;
  @BindView(R.id.callAction) FrameLayout callAction;
  @BindView(R.id.avatar) AvatarView avatar;
  @BindView(R.id.layoutPulse) PulseLayout pulseLayout;
  @BindView(R.id.containerAction) LinearLayout containerAction;
  @BindView(R.id.containerView) ImageView containerView;
  @BindView(R.id.txtSwipeDown) TextViewFont textSwipeDown;

  // VARIABLES
  private Unbinder unbinder;
  private UserComponent userComponent;
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

          txtDidplayName.setText(EmojiParser.demojizedText(name));
          avatar.load(picture);
          new GlideUtils.Builder(this).hasPlaceholder(true)
              .url(picture)
              .target(containerView)
              .load();
          avatar.load(picture);
        }
      }
    }

    soundManager.playSoundEndlessly(SoundManager.CALL_RING, SoundManager.SOUND_MID);

    setAnimation();
    setDownCounter();
    yTranslation = screenUtils.pxToDp(Y_TRANSLATION);
    callAction.setOnTouchListener(new onTouchJoinButton());
  }

  ////////////////
  // LIFE CYCLE //
  ////////////////

  @Override public void finish() {
 /*   soundManager.killAllSound();
    Intent mIntent = new Intent(this, HomeActivity.class);
    finishAffinity();
    startActivity(mIntent)*/
    ;
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

  private void setAnimation() {
    pulseLayout.start();
    final Float[] ratioY = new Float[1];

    /* Y ANIMATION **/
    ObjectAnimator translateYAnimation =
        ObjectAnimator.ofFloat(containerAction, "translationY", 0f, Y_TRANSLATION);
    translateYAnimation.setDuration(SLOW_TRANSLATION_DURATION);
    translateYAnimation.setRepeatCount(ValueAnimator.INFINITE);
    translateYAnimation.setRepeatMode(ValueAnimator.REVERSE);
    translateYAnimation.setInterpolator(new LinearInterpolator());
    translateYAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        ratioY[0] = (Float) animation.getAnimatedValue() / Y_TRANSLATION;
        textSwipeDown.setAlpha(ratioY[0]);
      }
    });
    translateYAnimation.start();

    /* SHAKE ANIMATION **/
    ObjectAnimator shakeAnim = ObjectAnimator.ofFloat(callAction, "translationX", -5, 5);
    shakeAnim.setDuration(20);
    shakeAnim.setRepeatCount(ValueAnimator.INFINITE);
    shakeAnim.setRepeatMode(ValueAnimator.REVERSE);
    shakeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        animation.setFloatValues(-5 * ratioY[0], 5 * ratioY[0]);
      }
    });
    shakeAnim.start();

    /* SCALE ANIMATION **/
    AnimatorSet resizeAvenger = new AnimatorSet();
    ObjectAnimator animResizeX = ObjectAnimator.ofFloat(callAction, "scaleX", 1f, 1.1f);
    animResizeX.setDuration(300);
    animResizeX.setRepeatMode(ValueAnimator.REVERSE);
    animResizeX.setRepeatCount(ValueAnimator.INFINITE);
    animResizeX.setInterpolator(new DecelerateInterpolator());
    ObjectAnimator animResizeY = ObjectAnimator.ofFloat(callAction, "scaleY", 1f, 1.1f);
    animResizeY.setDuration(300);
    animResizeY.setRepeatMode(ValueAnimator.REVERSE);
    animResizeY.setRepeatCount(ValueAnimator.INFINITE);
    animResizeY.setInterpolator(new DecelerateInterpolator());

    animResizeY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        animation.setFloatValues(1f, 1 + (ratioY[0] / 10));
      }
    });
    animResizeX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        animation.setFloatValues(1f, 1 + (ratioY[0] / 10));
      }
    });

    resizeAvenger.playTogether(animResizeX, animResizeY);
    resizeAvenger.start();
  }

  private void initDependencyInjector() {
    this.userComponent = DaggerUserComponent.builder()
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
        .subscribe(aVoid -> {
          finish();
        });
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
                .withEndAction(new Runnable() {
                  @Override public void run() {
                    finish();
                    startActivity(NotificationUtils.getIntentForLive(v.getContext(), payload));
                  }
                })
                .start();
          } else {
            containerAction.animate()
                .translationY(ACTION_BUTTON_Y_TRANSLATION)
                .alpha(0)
                .setDuration(ACTION_BUTTON_DURATION_Y_TRANSLATION)
                .withEndAction(new Runnable() {
                  @Override public void run() {
                    finish();
                  }
                })
                .start();
          }
          break;
      }
      return false;
    }
  }
}
