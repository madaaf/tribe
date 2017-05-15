package com.tribe.app.presentation.view.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.bumptech.glide.Glide;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.LiveImmersiveNotificationPresenter;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.preferences.FullscreenNotificationState;
import com.tribe.app.presentation.utils.preferences.ImmersiveCallState;
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.view.notification.Alerter;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.MissedCallManager;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import com.tribe.app.presentation.view.widget.PulseLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by madaaflak on 15/03/2017.
 */

public class LiveImmersiveNotificationActivity extends BaseActivity implements MVPView {
  public final static String PLAYLOAD_VALUE = "PLAYLOAD_VALUE";

  private final static int ACTION_BUTTON_Y_TRANSLATION = 200;
  private final static int ACTION_BUTTON_DURATION_Y_TRANSLATION = 500;

  private final static int MAX_DURATION_NOTIFICATION = 30;
  private final static int SLOW_TRANSLATION_DURATION = 3000;
  private final static int Y_TRANSLATION = -40;

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
  @Inject @FullscreenNotificationState Preference<Set<String>> fullscreenNotificationState;
  @Inject @ImmersiveCallState Preference<Boolean> immersiveCallState;
  @Inject LiveImmersiveNotificationPresenter presenter;
  @Inject MissedCallManager missedCallManager;

  @BindView(R.id.txtDisplayName) TextViewFont txtDisplayName;
  @BindView(R.id.txtCallerName) TextViewFont txtCallerName;
  @BindView(R.id.callAction) View callAction;
  @BindView(R.id.avatar) AvatarView avatar;
  @BindView(R.id.layoutPulse) PulseLayout pulseLayout;
  @BindView(R.id.containerAction) LinearLayout containerAction;
  @BindView(R.id.containerView) ImageView containerView;
  @BindView(R.id.txtSwipeDown) TextViewFont textSwipeDown;

  // VARIABLES
  private Unbinder unbinder;
  private NotificationPayload payload = null;
  private NotificationReceiver notificationReceiver;
  private boolean receiverRegistered = false;
  private boolean shouldStartHome = false;
  private AudioManager audioManager;

  // RESOURCES
  private int translationYAnimation = 0;
  private int translationYAction = 0;
  private String noUrl;

  // OBSERVABLES
  private Subscription startSubscription;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_immersive_call);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    unbinder = ButterKnife.bind(this);
    initDependencyInjector();
    initResources();

    Bundle extras = getIntent().getExtras();
    if (extras != null && immersiveCallState.get()) {
      payload = (NotificationPayload) getIntent().getExtras().getSerializable(PLAYLOAD_VALUE);
      updateFromPayload(payload);
    } else {
      shouldStartHome = true;
      finish();
      return;
    }

    audioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));

    if (audioManager != null) {
      switch (audioManager.getRingerMode()) {
        case AudioManager.RINGER_MODE_SILENT:
          Timber.d("Silent mode");
          break;
        case AudioManager.RINGER_MODE_VIBRATE:
          vibrate();
          break;
        case AudioManager.RINGER_MODE_NORMAL:
          Timber.d("Normal mode");
          soundManager.playSound(SoundManager.CALL_RING, SoundManager.SOUND_MAX);
          break;
      }
    }

    if (payload != null) {
      PreferencesUtils.addToSet(fullscreenNotificationState, payload.getThread());
    }

    setAnimation();
    setDownCounter();
    callAction.setOnTouchListener(new onTouchJoinButton());

    presenter.onViewAttached(this);
  }

  ////////////////
  // LIFE CYCLE //
  ////////////////

  @Override public void finish() {
    if (shouldStartHome) {
      shouldStartHome = false;
      navigator.navigateToHomeAndFinishAffinity(this);
    }
    soundManager.cancelMediaPlayer();
    super.finish();
  }

  @Override protected void onResume() {
    super.onResume();
    onResumeLockPhone();

    if (!receiverRegistered) {
      if (notificationReceiver == null) notificationReceiver = new NotificationReceiver();

      registerReceiver(notificationReceiver,
          new IntentFilter(BroadcastUtils.BROADCAST_NOTIFICATIONS));
      receiverRegistered = true;
    }
  }

  @Override public void onBackPressed() {
    moveTaskToBack(true);
    super.onBackPressed();
  }

  @Override protected void onDestroy() {
    if (receiverRegistered) {
      unregisterReceiver(notificationReceiver);
      receiverRegistered = false;
    }

    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    if (v.hasVibrator()) v.cancel();

    soundManager.cancelMediaPlayer();
    dispose();
    if (presenter != null) presenter.onViewDetached();
    subscriptions.unsubscribe();
    if (unbinder != null) unbinder.unbind();
    if (startSubscription != null) startSubscription.unsubscribe();
    super.onDestroy();
  }

  ////////////////
  //   PRIVATE  //
  ////////////////

  private void initResources() {
    translationYAnimation = screenUtils.dpToPx(Y_TRANSLATION);
    translationYAction = screenUtils.dpToPx(ACTION_BUTTON_Y_TRANSLATION);
    noUrl = getString(R.string.no_profile_picture_url);
  }

  private void updateFromPayload(NotificationPayload payload) {
    if (payload != null) {
      boolean isGroup = !StringUtils.isEmpty(payload.getGroupId());
      String name = isGroup ? payload.getGroupName() : payload.getUserDisplayName();
      String picture = isGroup ? payload.getGroupPicture() : payload.getUserPicture();

      if (StringUtils.isEmpty(picture) || picture.equals(noUrl)) {
        Random random = new Random();
        int r = random.nextInt(9 - 1) + 1;
        int randomPlaceholder =
            getResources().getIdentifier("bg_call_" + r, "drawable", getPackageName());
        Glide.with(this).load(randomPlaceholder).centerCrop().into(containerView);
      } else {
        Glide.with(this).load(picture).centerCrop().into(containerView);
      }

      txtDisplayName.setText(EmojiParser.demojizedText(name));

      if (isGroup) {
        txtCallerName.setText(payload.getUserDisplayName());
      } else {
        txtCallerName.setVisibility(View.GONE);
      }

      avatar.setType(AvatarView.LIVE);
      avatar.load(picture);
    }
  }

  private void vibrate() {
    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    long[] pattern = { 0, 1000, 1000 };

    // Output yes if can vibrate, no otherwise
    if (v.hasVibrator()) {
      v.vibrate(pattern, 0);
    }
  }

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
  }

  private void setTranslationAnim() {
    ObjectAnimator translateYAnimation =
        ObjectAnimator.ofFloat(containerAction, "translationY", 0f, translationYAnimation);
    translateYAnimation.setDuration(SLOW_TRANSLATION_DURATION);
    translateYAnimation.setRepeatCount(ValueAnimator.INFINITE);
    translateYAnimation.setRepeatMode(ValueAnimator.REVERSE);
    translateYAnimation.setInterpolator(new LinearInterpolator());
    translateYAnimation.addUpdateListener(animation -> {
      ratioY[0] = (Float) animation.getAnimatedValue() / translationYAnimation;
      if (textSwipeDown == null) {
        return;
      }

      float scaleProgress = 1 + (SCALE_RATIO_MAX - SCALE_RATIO_MIN) * ratioY[0];
      callAction.setScaleX(scaleProgress);
      callAction.setScaleY(scaleProgress);
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

  private void endDismiss() {
    shouldStartHome = false;
    presenter.declineInvite(payload.getSessionId());
    immersiveCallState.set(false);
    finish();
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
                .translationY(-translationYAction)
                .alpha(0)
                .setDuration(ACTION_BUTTON_DURATION_Y_TRANSLATION)
                .withEndAction(() -> {
                  shouldStartHome = true;
                  finish();
                  startActivity(NotificationUtils.getIntentForLive(v.getContext(), payload, true));
                })
                .start();
          } else {
            containerAction.animate()
                .translationY(translationYAction)
                .alpha(0)
                .setDuration(ACTION_BUTTON_DURATION_Y_TRANSLATION)
                .withEndAction(() -> endDismiss())
                .start();
          }

          break;
      }

      return false;
    }
  }

  /////////////////
  //  BROADCAST  //
  /////////////////

  class NotificationReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
      NotificationPayload notificationPayload =
          (NotificationPayload) intent.getSerializableExtra(BroadcastUtils.NOTIFICATION_PAYLOAD);

      if (payload.equals(notificationPayload) && !notificationPayload.getClickAction()
          .equals(NotificationPayload.CLICK_ACTION_BUZZ)) {
        if (notificationPayload.getClickAction()
            .equals(NotificationPayload.CLICK_ACTION_END_LIVE)) {
          finish();
        } else {
          updateFromPayload(notificationPayload);
        }

        return;
      }

      LiveNotificationView liveNotificationView =
          NotificationUtils.getNotificationViewFromPayload(context, notificationPayload,
              missedCallManager);

      if (liveNotificationView != null) {
        subscriptions.add(liveNotificationView.onClickAction()
            .delay(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(action -> {
              if (action.getIntent() != null) {
                navigator.navigateToIntent(LiveImmersiveNotificationActivity.this,
                    action.getIntent());
              }
            }));

        Alerter.create(LiveImmersiveNotificationActivity.this, liveNotificationView).show();
      }
    }
  }
}
