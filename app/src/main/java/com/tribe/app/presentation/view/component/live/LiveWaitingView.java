package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.CircleView;
import com.tribe.app.presentation.view.widget.CircularProgressBar;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveWaitingView extends FrameLayout implements View.OnClickListener {

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

  @Inject ScreenUtils screenUtils;

  @Inject PaletteGrid paletteGrid;

  @BindView(R.id.avatar) AvatarView avatar;

  @BindView(R.id.viewShadowAvatar) View viewShadow;

  @BindView(R.id.viewCircle) CircleView viewCircle;

  @BindView(R.id.viewForegroundAvatar) View viewForegroundAvatar;

  @BindView(R.id.txtDropInTheLive) TextViewFont txtDropInTheLive;

  @BindView(R.id.viewThreeDots) ThreeDotsView viewThreeDots;

  @BindView(R.id.viewBuzz) BuzzView viewBuzz;

  @BindView(R.id.progressBar) CircularProgressBar progressBar;

  @BindView(R.id.btnRemove) View btnRemove;

  @BindView(R.id.viewRing) View viewRing;

  // VARIABLES
  private Unbinder unbinder;
  private Rect rect = new Rect();
  private Paint circlePaint = new Paint();
  private TribeGuest guest;
  private @LiveRoomView.TribeRoomViewType int type = LiveRoomView.GRID;
  private ValueAnimator animatorPulse;
  private AnimatorSet animatorScaleAvatar;
  private ValueAnimator animatorScaleUp;
  private ValueAnimator animatorScaleDown;
  private AnimatorSet animatorTransition;
  private ValueAnimator animatorAlpha;
  private ValueAnimator animatorScaleUpTransition;
  private ValueAnimator animatorAlphaTransition;
  private ObjectAnimator animatorBuzzAvatar;
  private boolean hasPulsed = false, removeMode = false, shouldShowRemoveAgain = true;

  // RESOURCES
  private int timeJoinRoom, strokeWidth;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private Subscription subscriptionDismissRemove;
  private PublishSubject<Void> onShouldJoinRoom = PublishSubject.create();
  private PublishSubject<TribeGuest> onShouldRemoveGuest = PublishSubject.create();

  public LiveWaitingView(Context context) {
    super(context);
    init();
  }

  public LiveWaitingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveWaitingView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  private void init() {
    initDependencyInjector();
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_waiting, this);
    unbinder = ButterKnife.bind(this);

    circlePaint.setStrokeWidth(screenUtils.dpToPx(1f));
    circlePaint.setAntiAlias(true);

    setBackground(null);
    setOnClickListener(this);

    progressBar.setAnimationDuration(timeJoinRoom);
    progressBar.setProgressColor(Color.WHITE);
    progressBar.setProgressWidth(screenUtils.dpToPx(7.5f));

    subscriptions.add(viewBuzz.onBuzzCompleted()
        .doOnNext(aVoid -> endBuzz())
        .delay(DURATION_BUZZ, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aVoid -> {
          startPulse();
        }));
  }

  private void initResources() {
    timeJoinRoom = getContext().getResources().getInteger(R.integer.time_join_room);
    strokeWidth = getContext().getResources().getDimensionPixelSize(R.dimen.stroke_width_ring);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  ///////////////
  //   DRAW    //
  ///////////////

  @Override protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);

    if (animatorPulse == null || viewCircle.getRect() == null) {
      rect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

      viewCircle.setRect(rect);
      viewCircle.setPaint(circlePaint);
    }
  }

  ///////////////
  //  ONCLICK  //
  ///////////////

  @Override public void onClick(View v) {
    if (!removeMode) {
      showRemovePeer();
    } else {
      hideRemovePeer();
    }
  }

  @OnClick(R.id.btnRemove) void remove() {
    subscriptionDismissRemove.unsubscribe();

    subscriptions.add(DialogFactory.dialog(getContext(),
        EmojiParser.demojizedText(getContext().getString(R.string.live_dismiss_invitation_title)),
        getContext().getString(R.string.live_dismiss_invitation_message),
        getContext().getString(R.string.live_dismiss_invitation_validate, guest.getDisplayName()),
        getContext().getString(R.string.live_dismiss_invitation_cancel)).filter(x -> {
      if (!x) {
        shouldShowRemoveAgain = false;
        hideRemovePeer();
      }
      return x == true;
    }).map(aBoolean -> guest).subscribe(tribeGuest -> onShouldRemoveGuest.onNext(guest)));
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void showGuest() {
    txtDropInTheLive.setVisibility(View.GONE);
    avatar.setVisibility(View.VISIBLE);
    viewShadow.setVisibility(View.VISIBLE);
    viewForegroundAvatar.setVisibility(View.VISIBLE);
    viewBuzz.setVisibility(View.VISIBLE);
    progressBar.setVisibility(View.GONE);
  }

  public void startCountdown() {
    progressBar.setVisibility(View.VISIBLE);
    progressBar.setProgress(100, DELAY_COUNTDOWN, new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        ValueAnimator animatorScaleUp = ValueAnimator.ofFloat(avatar.getScaleX(), SCALE_AVATAR);
        animatorScaleUp.setInterpolator(new OvershootInterpolator(OVERSHOOT_SCALE));
        animatorScaleUp.setDuration(DURATION_FAST);
        animatorScaleUp.addUpdateListener(animationScaleUp -> {
          float value = (float) animationScaleUp.getAnimatedValue();
          updateScaleWithValue(value);
        });
        animatorScaleUp.start();

        progressBar.animate()
            .scaleX(0)
            .scaleY(0)
            .setDuration(DURATION_FAST)
            .setListener(new AnimatorListenerAdapter() {
              @Override public void onAnimationEnd(Animator animation) {
                onShouldJoinRoom.onNext(null);
                animatorScaleUp.cancel();
                progressBar.setVisibility(View.GONE);
                progressBar.animate().setListener(null).start();
              }
            })
            .start();
      }

      @Override public void onAnimationCancel(Animator animation) {
        animation.removeAllListeners();
      }
    });
  }

  public void startPulse() {
    clearAnimator(animatorAlpha);
    clearViewAnimations();

    viewThreeDots.setVisibility(View.VISIBLE);
    viewForegroundAvatar.setVisibility(View.VISIBLE);

    animateScaleAvatar();
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

  private void updateScaleWithValue(float value) {
    avatar.setScaleX(value);
    avatar.setScaleY(value);
    viewShadow.setScaleX(value);
    viewShadow.setScaleY(value);
    viewForegroundAvatar.setScaleX(value);
    viewForegroundAvatar.setScaleY(value);
    viewRing.setScaleX(value);
    viewRing.setScaleY(value);
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

  public void stopPulse() {
    clearAnimator(animatorPulse);
    clearAnimator(animatorScaleAvatar);
    clearAnimator(animatorScaleUp);
    clearAnimator(animatorScaleAvatar);
    clearAnimator(animatorScaleDown);
    clearAnimator(animatorScaleUpTransition);
    clearAnimator(animatorAlphaTransition);
    clearAnimator(animatorAlpha);
    clearAnimator(animatorBuzzAvatar);
    viewCircle.setRadius(0);
    clearViewAnimations();
  }

  private void clearViewAnimations() {
    avatar.clearAnimation();
    viewShadow.clearAnimation();
    viewForegroundAvatar.clearAnimation();
    viewCircle.clearAnimation();
    viewRing.clearAnimation();
    viewThreeDots.clearAnimation();
  }

  private void clearAnimator(Animator animator) {
    if (animator != null) {
      animator.cancel();
      animator.removeAllListeners();
    }
  }

  public void buzz() {
    stopPulse();
    readyForBuzz();
    viewBuzz.buzz();
  }

  public void readyForBuzz() {
    animatorTransition = new AnimatorSet();

    animatorScaleUpTransition = ValueAnimator.ofFloat(avatar.getScaleX(), SCALE_AVATAR);
    animatorScaleUpTransition.setInterpolator(new DecelerateInterpolator());
    animatorScaleUpTransition.setDuration(DURATION_FAST);
    animatorScaleUpTransition.addUpdateListener(animation -> {
      float value = (float) animation.getAnimatedValue();
      updateScaleWithValue(value);
    });

    animatorBuzzAvatar = ObjectAnimator.ofFloat(avatar, TRANSLATION_X, 3, -3);
    animatorBuzzAvatar.setDuration(DURATION_FAST_FURIOUS);
    animatorBuzzAvatar.setRepeatCount(ValueAnimator.INFINITE);
    animatorBuzzAvatar.setRepeatMode(ValueAnimator.REVERSE);
    animatorBuzzAvatar.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationCancel(Animator animation) {
        animatorBuzzAvatar.removeAllListeners();
        avatar.setTranslationX(0);
      }
    });
    animatorBuzzAvatar.start();

    animatorAlphaTransition = ValueAnimator.ofFloat(1f, 0);
    animatorAlphaTransition.setDuration(DURATION_FAST);
    animatorAlphaTransition.addUpdateListener(animation -> {
      float value = (float) animation.getAnimatedValue();
      viewForegroundAvatar.setAlpha(value);
      viewThreeDots.setAlpha(value);
    });

    animatorTransition.playTogether(animatorScaleUpTransition, animatorAlphaTransition);
    animatorTransition.start();
  }

  private void endBuzz() {
    clearAnimator(animatorTransition);
    clearAnimator(animatorAlphaTransition);
    clearAnimator(animatorScaleUpTransition);
    clearAnimator(animatorBuzzAvatar);
    clearViewAnimations();

    animatorAlpha = ValueAnimator.ofFloat(0f, 1f);
    animatorAlpha.setDuration(DURATION_BUZZ);
    animatorAlpha.addUpdateListener(animation -> {
      float value = (float) animation.getAnimatedValue();
      viewForegroundAvatar.setAlpha(value);
      viewThreeDots.setAlpha(value);
    });
    animatorAlpha.start();
  }

  public void incomingPeer() {
    stopPulse();
    subscriptions.add(Observable.interval(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          animatePulse(DURATION_PULSE_FAST);
        }));
  }

  private void showRemovePeer() {
    if (!shouldShowRemoveAgain) return;

    removeMode = true;
    stopPulse();

    subscriptions.add(Observable.timer(DURATION_FAST, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> animateRemovePeer(false)));

    subscriptionDismissRemove = Observable.timer(TIMER_DISMISS_REMOVE, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          hideRemovePeer();
        });
  }

  private void hideRemovePeer() {
    subscriptionDismissRemove.unsubscribe();
    removeMode = false;
    subscriptions.add(Observable.timer(DURATION_FAST, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> startPulse()));
    animateRemovePeer(true);
  }

  private void animateRemovePeer(boolean reverse) {
    AnimatorSet animatorSet = new AnimatorSet();

    ValueAnimator animatorScaleDownTransition =
        ValueAnimator.ofFloat(avatar.getScaleX(), SCALE_AVATAR);
    animatorScaleDownTransition.setInterpolator(new DecelerateInterpolator());
    animatorScaleDownTransition.setDuration(DURATION_FAST);
    animatorScaleDownTransition.addUpdateListener(animation -> {
      float value = (float) animation.getAnimatedValue();
      updateScaleWithValue(value);
    });

    viewThreeDots.animate()
        .alpha(reverse ? 1 : 0)
        .setDuration(DURATION_FAST)
        .setStartDelay(reverse ? DURATION_FAST * 2 : 0)
        .start();

    ValueAnimator animatorScaleUpRemove =
        ValueAnimator.ofFloat(reverse ? 1f : 0f, reverse ? 0f : 1f);
    animatorScaleUpRemove.setInterpolator(new OvershootInterpolator(OVERSHOOT_SCALE));
    animatorScaleUpRemove.setStartDelay(DURATION_FAST);
    animatorScaleUpRemove.setDuration(DURATION_FAST);
    animatorScaleUpRemove.addUpdateListener(animation -> {
      float value = (float) animation.getAnimatedValue();
      btnRemove.setScaleX(value);
      btnRemove.setScaleY(value);
    });

    ValueAnimator animatorRing =
        ValueAnimator.ofFloat(reverse ? strokeWidth : 0f, reverse ? 0f : strokeWidth);
    animatorRing.setInterpolator(new DecelerateInterpolator());
    animatorRing.setStartDelay(DURATION_FAST);
    animatorRing.setDuration(DURATION_FAST);
    animatorRing.addUpdateListener(animation -> {
      float value = (float) animation.getAnimatedValue();

      if (viewRing.getBackground() instanceof ShapeDrawable) {
        ShapeDrawable sd = (ShapeDrawable) viewRing.getBackground();
        sd.getPaint().setStrokeWidth(value);
      }
    });

    animatorSet.playTogether(animatorScaleDownTransition, animatorScaleUpRemove, animatorRing);
    animatorSet.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationStart(Animator animation) {
        if (!reverse) {
          viewRing.setVisibility(View.VISIBLE);
          btnRemove.setVisibility(View.VISIBLE);
        }
      }

      @Override public void onAnimationEnd(Animator animation) {
        if (reverse) {
          viewRing.setVisibility(View.GONE);
          btnRemove.setVisibility(View.GONE);
        }
      }

      @Override public void onAnimationCancel(Animator animation) {
        animatorSet.removeAllListeners();
      }
    });

    animatorSet.start();
  }

  public void setColor(int color) {
    viewCircle.setBackgroundColor(color);
    viewCircle.setRadius(0);
    circlePaint.setColor(paletteGrid.getRandomColorExcluding(color));
  }

  public void prepareForDrop() {
    txtDropInTheLive.setVisibility(View.GONE);
  }

  public void setGuest(TribeGuest guest) {
    this.guest = guest;
    if (guest != null) {
      if ((guest.isGroup() && StringUtils.isEmpty(guest.getPicture()))
          || guest.getMemberPics() != null) {
        avatar.loadGroupAvatar(guest.getPicture(), null, guest.getId(), guest.getMemberPics());
      } else {
        avatar.load(guest.getPicture());
      }
    }
  }

  public void setRoomType(@LiveRoomView.TribeRoomViewType int type) {
    this.type = type;
  }

  public AvatarView avatar() {
    return avatar;
  }

  public void dispose() {
    subscriptions.clear();
    stopPulse();
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Void> onShouldJoinRoom() {
    return onShouldJoinRoom;
  }

  public Observable<TribeGuest> onShouldRemoveGuest() {
    return onShouldRemoveGuest;
  }
}
