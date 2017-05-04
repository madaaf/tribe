package com.tribe.app.presentation.view.component.onboarding;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * CodeView.java
 * Created by tiago on 10/06/2016.
 * Last Modified by Horatio.
 * Component used in MVPView Pager in AuthViewFragment.java for a user to input their verification
 * code.
 */
public class CodeView extends FrameLayout {

  private static final int DELAY = 300;
  private static final int DURATION = 300;
  private static final int DURATION_MEDIUM = 500;
  private static final int DURATION_FAST = 150;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.editTxtCode) EditTextFont editTxtCode;

  @BindView(R.id.progressView) CircularProgressView progressView;

  @BindView(R.id.progressBarCountdown) ProgressBar progressBarCountdown;

  @BindView(R.id.layoutCountdown) ViewGroup layoutCountdown;

  @BindView(R.id.txtCountdown) TextViewFont txtCountdown;

  @BindView(R.id.imgBack) ImageView imgBack;

  @BindView(R.id.layoutPin) ViewGroup layoutPin;

  @BindView(R.id.pinCircle1) ImageView pinCircle1;

  @BindView(R.id.pinCircle2) ImageView pinCircle2;

  @BindView(R.id.pinCircle3) ImageView pinCircle3;

  @BindView(R.id.pinCircle4) ImageView pinCircle4;

  @BindView(R.id.txtCode1) TextViewFont txtCode1;

  @BindView(R.id.txtCode2) TextViewFont txtCode2;

  @BindView(R.id.txtCode3) TextViewFont txtCode3;

  @BindView(R.id.txtCode4) TextViewFont txtCode4;

  @BindView(R.id.imgConnected) ImageView imgConnected;

  @BindView(R.id.txtConnected) TextViewFont txtConnected;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Boolean> codeValid = PublishSubject.create();
  private PublishSubject<Void> backClicked = PublishSubject.create();
  private PublishSubject<Void> countdownExpired = PublishSubject.create();

  // VARIABLES
  private int timeCodeCountdown;
  private int currentCountdown = 0;
  private ObjectAnimator animator;

  public CodeView(Context context) {
    super(context);
  }

  public CodeView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * Lifecycle methods
   */

  @Override protected void onDetachedFromWindow() {
    unbinder.unbind();
    super.onDetachedFromWindow();

    if (subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
      subscriptions.clear();
    }

    if (animator != null) animator.cancel();
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    LayoutInflater.from(getContext()).inflate(R.layout.view_code, this);
    unbinder = ButterKnife.bind(this);

    initDependencyInjector();

    imgConnected.setScaleX(0);
    imgConnected.setScaleY(0);

    txtConnected.setTranslationX(screenUtils.getWidthPx());

    subscriptions.add(RxTextView.textChanges(editTxtCode).map(CharSequence::toString).map(s -> {
      switch (s.length()) {
        case 0:
          pinCircle1.setVisibility(VISIBLE);
          pinCircle2.setVisibility(VISIBLE);
          pinCircle3.setVisibility(VISIBLE);
          pinCircle4.setVisibility(VISIBLE);
          txtCode1.setText("");
          break;
        case 1:
          pinCircle1.setVisibility(INVISIBLE);
          pinCircle2.setVisibility(VISIBLE);
          pinCircle3.setVisibility(VISIBLE);
          pinCircle4.setVisibility(VISIBLE);
          txtCode1.setText(s);
          txtCode2.setText("");
          break;
        case 2:
          pinCircle1.setVisibility(INVISIBLE);
          pinCircle2.setVisibility(INVISIBLE);
          pinCircle3.setVisibility(VISIBLE);
          pinCircle4.setVisibility(VISIBLE);
          txtCode1.setText(s.substring(0, 1));
          txtCode2.setText(s.substring(1, 2));
          txtCode3.setText("");
          break;
        case 3:
          pinCircle1.setVisibility(INVISIBLE);
          pinCircle2.setVisibility(INVISIBLE);
          pinCircle3.setVisibility(INVISIBLE);
          pinCircle4.setVisibility(VISIBLE);
          txtCode1.setText(s.substring(0, 1));
          txtCode2.setText(s.substring(1, 2));
          txtCode3.setText(s.substring(2, 3));
          txtCode4.setText("");
          break;
        case 4:
          pinCircle1.setVisibility(INVISIBLE);
          pinCircle2.setVisibility(INVISIBLE);
          pinCircle3.setVisibility(INVISIBLE);
          pinCircle4.setVisibility(INVISIBLE);
          txtCode1.setText(s.substring(0, 1));
          txtCode2.setText(s.substring(1, 2));
          txtCode3.setText(s.substring(2, 3));
          txtCode4.setText(s.substring(3, 4));
          break;
      }

      return s.length() == 4;
    }).subscribe(codeValid));

    subscriptions.add(RxView.clicks(imgBack).subscribe(aVoid -> {
      resetPinCodeView();
      backClicked.onNext(null);
    }));
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

  @OnClick(R.id.layoutPin) void clickLayoutPin() {
    openKeyboard(0);
  }

  public void openKeyboard(int delay) {
    editTxtCode.requestFocus();
    editTxtCode.postDelayed(() -> {
      InputMethodManager keyboard =
          (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      keyboard.showSoftInput(editTxtCode, 0);
    }, delay);
  }

  public void startCountdown(boolean isCall) {
    timeCodeCountdown =
        isCall ? getContext().getResources().getInteger(R.integer.time_code_countdown_call)
            : getContext().getResources().getInteger(R.integer.time_code_countdown_pin);
    progressBarCountdown.setProgress(timeCodeCountdown);
    progressBarCountdown.setMax(timeCodeCountdown);
    layoutCountdown.setVisibility(View.VISIBLE);

    animator = ObjectAnimator.ofInt(progressBarCountdown, "progress", 0);
    animator.setDuration(timeCodeCountdown);
    animator.addUpdateListener(animation -> {
      currentCountdown = (Integer) animation.getAnimatedValue();
      txtCountdown.setText("" + (currentCountdown / 1000));
    });

    animator.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        countdownExpired.onNext(null);
        currentCountdown = 0;
      }
    });

    animator.start();
  }

  public void removeCountdown() {
    if (animator != null) animator.cancel();
    layoutCountdown.setVisibility(GONE);
  }

  public int getCurrentCountdown() {
    return currentCountdown;
  }

  public void requestCodeFocus() {
    editTxtCode.requestFocus();
  }

  /**
   * Observable
   */

  public Observable<Boolean> codeValid() {
    return codeValid;
  }

  public Observable<Void> backClicked() {
    return backClicked;
  }

  public Observable<Void> countdownExpired() {
    return countdownExpired;
  }

  /**
   * Public view methods
   */

  public String getCode() {
    return editTxtCode.getText().toString();
  }

  public void showLoading() {
    progressBarCountdown.setVisibility(GONE);
    progressView.setVisibility(VISIBLE);
  }

  public void hideLoading() {
    progressView.setVisibility(GONE);
    progressBarCountdown.setVisibility(VISIBLE);
  }

  public void showConnected() {
    imgConnected.animate()
        .scaleY(1)
        .scaleX(1)
        .setDuration(DURATION)
        .setInterpolator(new OvershootInterpolator(1.20f))
        .start();
  }

  public void showConnectedEnd() {
    imgConnected.animate()
        .translationX(-screenUtils.getWidthPx() + 2 * getContext().getResources()
            .getDimensionPixelSize(R.dimen.horizontal_margin_small) + imgConnected.getWidth())
        .setDuration(DURATION)
        .setStartDelay(DELAY)
        .setInterpolator(new DecelerateInterpolator())
        .setListener(null)
        .start();

    txtConnected.animate()
        .translationX(0)
        .setDuration(DURATION_MEDIUM)
        .setStartDelay(DELAY)
        .setInterpolator(new OvershootInterpolator(0.25f))
        .start();

    imgBack.animate()
        .translationX(-screenUtils.getWidthPx())
        .setDuration(DURATION)
        .setStartDelay(DELAY)
        .setInterpolator(new DecelerateInterpolator())
        .setListener(null)
        .start();

    layoutPin.animate()
        .translationX(-screenUtils.getWidthPx())
        .setDuration(DURATION)
        .setStartDelay(DELAY)
        .setInterpolator(new DecelerateInterpolator())
        .setListener(null)
        .start();
  }

  public void setCode(String code) {
    editTxtCode.setText(code);
  }

  private void resetPinCodeView() {
    pinCircle1.setVisibility(VISIBLE);
    pinCircle2.setVisibility(VISIBLE);
    pinCircle3.setVisibility(VISIBLE);
    pinCircle4.setVisibility(VISIBLE);
    editTxtCode.setText("");
    txtCode1.setText("");
    txtCode2.setText("");
    txtCode3.setText("");
    txtCode4.setText("");
  }
}
