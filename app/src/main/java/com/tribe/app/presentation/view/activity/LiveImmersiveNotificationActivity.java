package com.tribe.app.presentation.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.CircleView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarLiveView;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by madaaflak on 15/03/2017.
 */

public class LiveImmersiveNotificationActivity extends BaseActivity {
  public final static String PLAYLOAD_VALUE = "playload";
  private final static int MAX_DURATION_NOTIFICATION = 30;

  private float y1, y2;
  static final int MIN_DISTANCE = 10;
  private final static int TIMER_DISMISS_REMOVE = 5000;
  private final static int DURATION_FAST_FURIOUS = 60;
  private final static int DURATION_FAST = 300;
  private final static int DELAY_COUNTDOWN = 500;
  private final static int DURATION_PULSE_FAST = 150;
  private final static int DURATION_PULSE = 300;
  private final static int DURATION_SCALE = 1000;
  private final static int SCALE_DELAY = 250;
  private final static int DURATION_BUZZ = 300;
  private final static float OVERSHOOT_SCALE = 1.25f;
  private final static float SCALE_AVATAR = 1.15f;
  private boolean hasPulsed = false;

  @Inject ScreenUtils screenUtils;
  @Inject PaletteGrid paletteGrid;
  @Inject Navigator navigator;

  @BindView(R.id.txtDidplayName) TextViewFont txtDidplayName;
  @BindView(R.id.callAction) FrameLayout callAction;
  @BindView(R.id.avatar) AvatarLiveView avatar;
  @BindView(R.id.backview) CircleView viewCircle;

  @BindView(R.id.containerAction) LinearLayout containerAction;

  // VARIABLES
  private Unbinder unbinder;
  private UserComponent userComponent;
  private ValueAnimator animatorPulse;
  private ValueAnimator animatorScaleDown;
  private ValueAnimator animatorScaleUp;
  private AnimatorSet animatorScaleAvatar;
  private Paint circlePaint = new Paint();
  NotificationPayload playload = null;
  Animation scaleAnimation;
  Animation shake;
  Animation shake2;

  // OBSERVABLES
  private Subscription startSubscription;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_immerdive_call);
    unbinder = ButterKnife.bind(this);
    initDependencyInjector();
    setDownCounter();

    circlePaint.setStrokeWidth(screenUtils.dpToPx(1f));
    circlePaint.setAntiAlias(true);

    viewCircle.setPaint(circlePaint);

    if (savedInstanceState == null) {
      Bundle extras = getIntent().getExtras();
      if (extras == null) {
        playload = null;
      } else {
        playload = (NotificationPayload) extras.getSerializable(PLAYLOAD_VALUE);
        txtDidplayName.setText(EmojiParser.demojizedText(playload.getUserDisplayName()));
      }
    }

    scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_button_call);
    shake = AnimationUtils.loadAnimation(this, R.anim.vibrate);

  /*  shake2 = AnimationUtils.loadAnimation(this, R.anim.vibrate2);*/

    Animation translation = new TranslateAnimation(0, 0, 0, -80);
    translation.setDuration(3000);
    translation.setRepeatCount(-1);
    translation.setRepeatMode(Animation.REVERSE);
    translation.setInterpolator(new LinearInterpolator());

    AnimationSet setAnims = new AnimationSet(true);//false means don't share interpolators
    setAnims.addAnimation(scaleAnimation);
    setAnims.addAnimation(shake);
    //setAnims.addAnimation(translation);
    callAction.startAnimation(setAnims);
    containerAction.startAnimation(translation);

    callAction.setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            y1 = event.getY();
            break;
          case MotionEvent.ACTION_UP:
            y2 = event.getY();
            float deltaY = y2 - y1;
            if (deltaY < 0) {
              Timber.e("SOEF SLIDE UP " + deltaY);
              containerAction.animate()
                  .translationY(-200)
                  .alpha(0)
                  .setDuration(500)
                  .withEndAction(new Runnable() {
                    @Override public void run() {
                      startActivity(NotificationUtils.getIntentForLive(v.getContext(), playload));
                    }
                  })
                  .start();
            } else {
              Timber.e("SOEF SLIDE DOWN " + deltaY);
              containerAction.animate()
                  .translationY(200)
                  .alpha(0)
                  .setDuration(500)
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
    });

    //callAction.startAnimation(scaleAnimation);
/*    Rect rect = new Rect();
    rect.set(avatar.getTop() + (avatar.getWidth() / 2), avatar.getTop() + (avatar.getHeight() / 2),
        50, 50);
    viewCircle.setRect(rect);*/

    viewCircle.setPaint(circlePaint);
    initAnimation();
    avatar.load(playload.getUserPicture());
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

  @Override public void onBackPressed() {
    moveTaskToBack(true);
  }

  private void initAnimation() {
    startPulse();
  }

  private void clearAnimator(Animator animator) {
    if (animator != null) {
      animator.cancel();
      animator.removeAllListeners();
    }
  }

  public void setColor(int color) {
    viewCircle.setBackgroundColor(color);
    viewCircle.setRadius(0);
    circlePaint.setColor(paletteGrid.getRandomColorExcluding(color));
  }

  public void startPulse() {
    animateScaleAvatar();
  }

  private void animatePulse(int duration) {
    int finalHeight = screenUtils.getHeightPx() >> 1;

    clearAnimator(animatorPulse);

    animatorPulse = ValueAnimator.ofInt(avatar.getWidth() >> 1, finalHeight);
    animatorPulse.setDuration(duration);
    animatorPulse.addUpdateListener(animation -> {
      Integer value = (Integer) animation.getAnimatedValue();
      viewCircle.setRadius(value);
    });

    animatorPulse.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationCancel(Animator animation) {
        animatorPulse.removeAllListeners();
      }

      @Override public void onAnimationEnd(Animator animation) {
        setColor(circlePaint.getColor());
      }
    });

    animatorPulse.start();
  }

  private void animateScaleAvatar() {
    animatorScaleAvatar = new AnimatorSet();

    animatorScaleUp = ValueAnimator.ofFloat(1f, SCALE_AVATAR);
    animatorScaleUp.setInterpolator(new OvershootInterpolator(OVERSHOOT_SCALE));
    animatorScaleUp.setDuration(DURATION_SCALE);
    animatorScaleUp.setStartDelay(SCALE_DELAY);
    animatorScaleUp.addUpdateListener(animation -> {
      float value = (float) animation.getAnimatedValue();
      updateScaleWithValue(value);
    });
    animatorScaleUp.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationCancel(Animator animation) {
        animatorScaleUp.removeAllListeners();
      }

      @Override public void onAnimationEnd(Animator animation) {
        animatorScaleAvatar.start();
      }
    });

    animatorScaleDown = ValueAnimator.ofFloat(SCALE_AVATAR, 1f);
    animatorScaleDown.addUpdateListener(animation -> {
      float value = (float) animation.getAnimatedValue();
      if (value < 1f && !hasPulsed) {
        animatePulse(DURATION_PULSE);
        hasPulsed = true;
      }

      updateScaleWithValue(value);
    });
    animatorScaleDown.setInterpolator(new OvershootInterpolator(OVERSHOOT_SCALE));
    animatorScaleDown.setDuration(DURATION_SCALE >> 1);
    animatorScaleDown.setStartDelay(SCALE_DELAY);
    animatorScaleDown.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationCancel(Animator animation) {
        animatorScaleDown.removeAllListeners();
      }

      @Override public void onAnimationStart(Animator animation) {
        hasPulsed = false;
      }
    });

    animatorScaleAvatar.play(animatorScaleDown).before(animatorScaleUp);
    animatorScaleAvatar.start();
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    if (startSubscription != null) startSubscription.unsubscribe();
    super.onDestroy();
  }

  private void updateScaleWithValue(float value) {
/*    avatar.setScaleX(value);
    avatar.setScaleY(value);*/
  }
}
