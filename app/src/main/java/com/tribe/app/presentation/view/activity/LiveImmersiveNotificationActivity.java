package com.tribe.app.presentation.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
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
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.widget.PulseLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarLiveView;
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

  private final static int MAX_DURATION_NOTIFICATION = 30;
  private final static int SLOW_TRANSLATION_DURATION = 3000;
  private final static int Y_TRANSLATION = -20;

  private int yTranslation = 0;
  private float y1, y2;

  @Inject ScreenUtils screenUtils;
  @Inject PaletteGrid paletteGrid;
  @Inject Navigator navigator;
  @Inject SoundManager soundManager;

  @BindView(R.id.txtDidplayName) TextViewFont txtDidplayName;
  @BindView(R.id.callAction) FrameLayout callAction;
  @BindView(R.id.avatar) AvatarLiveView avatar;
  @BindView(R.id.layoutPulse) PulseLayout pulseLayout;
  @BindView(R.id.containerAction) LinearLayout containerAction;
  @BindView(R.id.containerView) ImageView containerView;

  // VARIABLES
  private Unbinder unbinder;
  private UserComponent userComponent;
  NotificationPayload playload = null;

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
        playload = null;
      } else {
        playload = (NotificationPayload) extras.getSerializable(PLAYLOAD_VALUE);
        txtDidplayName.setText(EmojiParser.demojizedText(playload.getUserDisplayName()));
      }
    }

    soundManager.playSoundEndlessly(SoundManager.CALL_RING, SoundManager.SOUND_MID);

    setAnimation();
    setDownCounter();
    yTranslation = screenUtils.dpToPx(Y_TRANSLATION);
    avatar.load(playload.getUserPicture());
    GlideUtils.load(this, playload.getUserPicture(), containerView);
    callAction.setOnTouchListener(new onTouchJoinButton());
  }

  ////////////////
  // LIFE CYCLE //
  ////////////////

  @Override public void finish() {
    soundManager.killAllSound();
    Intent mIntent = new Intent(this, HomeActivity.class);
    finishAffinity();
    startActivity(mIntent);
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

    Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_button_call);
    Animation shakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake);
    Animation xTranslationAnim = new TranslateAnimation(0, 0, 0, screenUtils.dpToPx(-40));

    xTranslationAnim.setDuration(SLOW_TRANSLATION_DURATION);
    xTranslationAnim.setRepeatCount(Animation.INFINITE);
    xTranslationAnim.setRepeatMode(Animation.REVERSE);
    xTranslationAnim.setInterpolator(new LinearInterpolator());

    AnimationSet setAnims = new AnimationSet(true);
    setAnims.addAnimation(scaleAnim);
    setAnims.addAnimation(shakeAnim);

    callAction.startAnimation(setAnims);
    containerAction.startAnimation(xTranslationAnim);
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
                .translationY(-200)
                .alpha(0)
                .setDuration(500)
                .withEndAction(new Runnable() {
                  @Override public void run() {
                    finish();
                    startActivity(NotificationUtils.getIntentForLive(v.getContext(), playload));
                  }
                })
                .start();
          } else {
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
  }
}
