package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.List;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveWaitingAvatarView extends FrameLayout {

  private final static float OVERSHOOT_SCALE = 1.25f;

  @BindView(R.id.avatar) AvatarView avatar;

  @BindView(R.id.viewForegroundAvatar) View viewForegroundAvatar;

  @BindView(R.id.btnRemove) View btnRemove;

  @BindView(R.id.viewRing) View viewRing;

  @BindView(R.id.viewThreeDots) ThreeDotsView viewThreeDots;

  // VARIABLES
  private Unbinder unbinder;

  // RESOURCES
  private int strokeWidth;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LiveWaitingAvatarView(Context context) {
    super(context);
    init();
  }

  public LiveWaitingAvatarView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveWaitingAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();

    super.onDetachedFromWindow();
  }

  private void init() {
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_waiting_avatar_view, this);
    unbinder = ButterKnife.bind(this);

    setBackground(null);
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

  //////////////
  //  PUBLIC  //
  //////////////

  public void changeSize(int size) {
    UIUtils.changeSizeOfView(this, size);
    avatar.changeSize(size, true);
    int ponderedSize = (int) (size * (1 - avatar.getShadowRatio())) + 1;
    UIUtils.changeSizeOfView(viewForegroundAvatar, ponderedSize);
    UIUtils.changeSizeOfView(viewRing, ponderedSize);
    UIUtils.changeSizeOfView(viewThreeDots, ponderedSize);
  }

  public void showGuest() {
    avatar.setVisibility(View.VISIBLE);
    viewForegroundAvatar.setVisibility(View.VISIBLE);
  }

  public void startPulse() {
    viewThreeDots.setVisibility(View.VISIBLE);
    viewForegroundAvatar.setVisibility(View.VISIBLE);
  }

  public void clearViewAnimations() {
    avatar.clearAnimation();
    viewForegroundAvatar.clearAnimation();
    viewRing.clearAnimation();
    viewThreeDots.clearAnimation();
  }

  public void animateBuzzAlpha(float alpha) {
    viewForegroundAvatar.setAlpha(alpha);
    viewThreeDots.setAlpha(alpha);
  }

  public void animateRemovePeer(int duration, boolean reverse) {
    AnimatorSet animatorSet = new AnimatorSet();

    viewThreeDots.animate()
        .alpha(reverse ? 1 : 0)
        .setDuration(duration)
        .setStartDelay(reverse ? duration * 2 : 0)
        .start();

    ValueAnimator animatorScaleUpRemove =
        ValueAnimator.ofFloat(reverse ? 1f : 0f, reverse ? 0f : 1f);
    animatorScaleUpRemove.setInterpolator(new OvershootInterpolator(OVERSHOOT_SCALE));
    animatorScaleUpRemove.setStartDelay(duration);
    animatorScaleUpRemove.setDuration(duration);
    animatorScaleUpRemove.addUpdateListener(
        animation -> animateRemoveAlpha((float) animation.getAnimatedValue()));

    ValueAnimator animatorRing =
        ValueAnimator.ofFloat(reverse ? strokeWidth : 0f, reverse ? 0f : strokeWidth);
    animatorRing.setInterpolator(new DecelerateInterpolator());
    animatorRing.setStartDelay(duration);
    animatorRing.setDuration(duration);
    animatorRing.addUpdateListener(animation -> {
      float value = (float) animation.getAnimatedValue();

      if (viewRing.getBackground() instanceof ShapeDrawable) {
        ShapeDrawable sd = (ShapeDrawable) viewRing.getBackground();
        sd.getPaint().setStrokeWidth(value);
      }
    });

    animatorSet.playTogether(animatorScaleUpRemove, animatorRing);
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

  public void animateRemoveAlpha(float alpha) {
    btnRemove.setScaleX(alpha);
    btnRemove.setScaleY(alpha);
  }

  public void loadGroupAvatar(String url, String previousUrl, String groupId,
      List<String> membersPic) {
    avatar.loadGroupAvatar(url, previousUrl, groupId, membersPic);
  }

  public void load(String url) {
    avatar.load(url);
  }

  public AvatarView getAvatar() {
    return avatar;
  }

  public void dispose() {
    clearViewAnimations();
    viewThreeDots.dispose();
  }
}
