package com.tribe.app.presentation.view.component.home;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 05/08/2017.
 */
public class NewCallView extends FrameLayout {

  private static final int DURATION = 150;
  private static final float OVERSHOOT = 3f;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.layoutNewCall) ViewGroup layoutNewCall;

  @BindView(R.id.imgBackToTop) ImageView imgBackToTop;

  // VARIABLES
  private boolean isNewCall = true;

  // RESOURCES
  int sizeInit, sizeHeight, translationY;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onNewCall = PublishSubject.create();
  private PublishSubject<Void> onGoBackToTop = PublishSubject.create();

  public NewCallView(Context context) {
    super(context);
    init(context, null);
  }

  public NewCallView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  @Override protected void onDetachedFromWindow() {
    unbinder.unbind();

    if (subscriptions != null && subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
    }

    super.onDetachedFromWindow();
  }

  @Override protected void onFinishInflate() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_new_call, this);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    initResources();
    initUI();

    super.onFinishInflate();
  }

  private void init(Context context, AttributeSet attrs) {

  }

  private void initUI() {
    imgBackToTop.setTranslationY(translationY);
    imgBackToTop.setAlpha(0f);

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        sizeInit = getMeasuredWidth();
      }
    });

    setOnClickListener(v -> {
      if (isNewCall) {
        onNewCall.onNext(null);
      } else {
        onGoBackToTop.onNext(null);
      }
    });
  }

  private void initResources() {
    sizeHeight = getResources().getDimensionPixelSize(R.dimen.bottom_bar_height);
    translationY = screenUtils.dpToPx(10);
  }

  private void animateWidth(int widthStart, int widthEnd) {
    Animator animator = AnimationUtils.getWidthAnimator(this, widthStart, widthEnd);
    animator.setDuration(DURATION);
    animator.setInterpolator(new DecelerateInterpolator());
    animator.start();
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void showNewCall() {
    if (isNewCall) return;
    isNewCall = true;

    imgBackToTop.animate()
        .alpha(0f)
        .translationY(translationY)
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    layoutNewCall.animate()
        .alpha(1f)
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    animateWidth(sizeHeight, sizeInit);
  }

  public void showBackToTop() {
    if (!isNewCall) return;
    isNewCall = false;

    imgBackToTop.animate()
        .alpha(1f)
        .translationY(0)
        .setDuration(DURATION * 2)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT))
        .start();

    layoutNewCall.animate()
        .alpha(0f)
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    animateWidth(sizeInit, sizeHeight);
  }

  //////////////////////
  //   OBSERVABLES    //
  //////////////////////

  public Observable<Void> onNewCall() {
    return onNewCall;
  }

  public Observable<Void> onBackToTop() {
    return onGoBackToTop;
  }
}

