package com.tribe.app.presentation.view.component.onboarding;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * PhoneNumberView.java
 * Created by tiago on 10/06/2016.
 * Last modified by Tiago
 * Component used in AuthActivity.java for a user to input their phone number, country code, and
 * verify that it is a valid phone number.
 */

public class PhoneNumberView extends FrameLayout {

  private static final int DELAY = 300;
  private static final int DURATION = 300;
  private static final int DURATION_MEDIUM = 500;
  private static final int DURATION_FAST = 150;

  @Inject ScreenUtils screenUtils;

  @Inject PhoneUtils phoneUtils;

  @BindView(R.id.editTxtPhoneNumber) EditTextFont editTxtPhoneNumber;

  @BindView(R.id.imgCountry) ImageView imgCountry;

  @BindView(R.id.btnNext) ImageView btnNext;

  @BindView(R.id.progressView) CircularProgressView progressView;

  @BindView(R.id.imgConnected) ImageView imgConnected;

  @BindView(R.id.txtConnected) TextViewFont txtConnected;

  @BindView(R.id.layoutPhone) ViewGroup layoutPhone;

  // VARIABLES
  private String countryCode = "US";
  private String currentPhoneNumber;
  private Context context;
  private boolean editable = true;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  private Unbinder unbinder;
  private PublishSubject<Boolean> phoneNumberValid = PublishSubject.create();
  private PublishSubject<Void> countryClickEventSubject = PublishSubject.create();
  private PublishSubject<Void> nextClick = PublishSubject.create();

  public PhoneNumberView(Context context) {
    super(context);
    this.context = context;
  }

  public PhoneNumberView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
  }

  public PhoneNumberView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.context = context;
  }

  @Override protected void onDetachedFromWindow() {
    unbinder.unbind();

    if (subscriptions != null && subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
      subscriptions.clear();
    }

    super.onDetachedFromWindow();
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    LayoutInflater.from(getContext()).inflate(R.layout.view_phone_number, this);
    unbinder = ButterKnife.bind(this);

    initDependencyInjector();

    imgConnected.setScaleX(0);
    imgConnected.setScaleY(0);

    txtConnected.setTranslationX(screenUtils.getWidthPx());

    btnNext.setEnabled(false);

    countryCode = context.getResources().getConfiguration().locale.getCountry();

    subscriptions.add(RxView.clicks(imgCountry).subscribe(countryClickEventSubject));

    subscriptions.add(RxView.clicks(btnNext).subscribe(nextClick));

    subscriptions.add(RxTextView.textChanges(editTxtPhoneNumber)
        .map((charSequence) -> charSequence.toString())
        .filter(s -> s != null && s.length() > 2)
        .doOnNext(s -> {
          if ((countryCode.equals(PhoneUtils.COUNTRY_CODE_DEV) && s.startsWith(
              PhoneUtils.PHONE_PREFIX_DEV) && s.length() == 8)) {
            currentPhoneNumber = "+" + PhoneUtils.COUNTRY_PREFIX_DEV + s;
          } else {
            checkValidPhoneNumber();
          }
        })
        .map(s -> currentPhoneNumber != null)
        .subscribe(phoneNumberValid));

    initWithCodeCountry(countryCode);
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

  public String getCountryCode() {
    return countryCode;
  }

  public void initWithCodeCountry(String codeCountry) {
    countryCode = codeCountry;

    try {
      Drawable countryFlagImg = ContextCompat.getDrawable(getContext(),
          R.drawable.class.getField("picto_flag_" + codeCountry.toLowerCase()).getInt(null));
      imgCountry.setImageDrawable(countryFlagImg);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }

    checkValidPhoneNumber();
  }

  private void checkValidPhoneNumber() {
    String phoneInput = getPhoneNumberInput();
    if (editable && !StringUtils.isEmpty(phoneInput)) {
      phoneInput = phoneInput.trim().replace(" ", "");
      currentPhoneNumber = phoneUtils.formatMobileNumber(phoneInput, countryCode);
      String viewPhoneNumber = phoneUtils.formatPhoneNumberForView(phoneInput, countryCode);
      if (viewPhoneNumber != null) {
        editable = false;
        editTxtPhoneNumber.setText(viewPhoneNumber);
        editTxtPhoneNumber.setSelection(editTxtPhoneNumber.getText().length());
        editable = true;
      }
    }
  }

  public void setPhoneNumber(String str) {
    editTxtPhoneNumber.setText(str);
  }

  public String getPhoneNumberInput() {
    return editTxtPhoneNumber.getText().toString();
  }

  public String getPhoneNumberFormatted() {
    return currentPhoneNumber;
  }

  public void setNextEnabled(boolean enabled) {
    if (enabled && !btnNext.isEnabled()) {
      ((TransitionDrawable) btnNext.getBackground()).startTransition(DURATION);
      btnNext.setEnabled(true);
    } else if (!enabled && btnNext.isEnabled()) {
      ((TransitionDrawable) btnNext.getBackground()).reverseTransition(DURATION);
      btnNext.setEnabled(false);
    }
  }

  public void showLoading() {
    btnNext.setVisibility(GONE);
    progressView.setVisibility(VISIBLE);
  }

  public void hideLoading() {
    btnNext.setVisibility(VISIBLE);
    progressView.setVisibility(GONE);
  }

  public void openKeyboard(int delay) {
    screenUtils.showKeyboard(editTxtPhoneNumber, delay);
  }

  public void hideKeyboard() {
    screenUtils.hideKeyboard(editTxtPhoneNumber);
  }

  public void clearFocus() {
    editTxtPhoneNumber.clearFocus();
    editTxtPhoneNumber.setEnabled(false);
  }

  public void enableFocus() {
    editTxtPhoneNumber.setEnabled(true);
    editTxtPhoneNumber.requestFocus();
  }

  public void showConnected() {
    btnNext.animate()
        .scaleX(0)
        .scaleY(0)
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    progressView.animate()
        .scaleX(0)
        .scaleY(0)
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .start();

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

    layoutPhone.animate()
        .translationX(-screenUtils.getWidthPx())
        .setDuration(DURATION)
        .setStartDelay(DELAY)
        .setInterpolator(new DecelerateInterpolator())
        .setListener(null)
        .start();
  }

  public boolean isDebug() {
    return phoneUtils.isDebugPhone(countryCode, currentPhoneNumber);
  }

  // OBSERVABLES
  public Observable<Boolean> phoneNumberValid() {
    return phoneNumberValid;
  }

  public Observable<Void> countryClick() {
    return countryClickEventSubject;
  }

  public Observable<Void> nextClick() {
    return nextClick;
  }
}
