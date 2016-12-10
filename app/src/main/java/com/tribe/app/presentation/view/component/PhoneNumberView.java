package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * PhoneNumberView.java
 * Created by tiago on 10/06/2016.
 * Last modified by Horatio
 * Component used in IntroViewFragment.java for a user to input their phone number, country code, and verify that it is a valid phone number.
 */

public class PhoneNumberView extends FrameLayout {

    @BindView(R.id.editTxtPhoneNumber)
    EditTextFont editTxtPhoneNumber;

    @BindView(R.id.imgCountry)
    ImageView imgCountry;

    @BindView(R.id.btnNext)
    ImageView btnNext;

    @BindView(R.id.progressView)
    CircularProgressView progressView;

    // VARIABLES
    private PhoneUtils phoneUtils;
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

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_phone_number, this);
        unbinder = ButterKnife.bind(this);

        countryCode = context.getResources().getConfiguration().locale.getCountry();

        subscriptions.add(RxView.clicks(imgCountry)
                .subscribe(countryClickEventSubject));

        subscriptions.add(RxView.clicks(btnNext)
                .subscribe(nextClick));
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void initWithCodeCountry(String codeCountry) {
        countryCode = codeCountry;

        try {
            Drawable countryFlagImg = ContextCompat.getDrawable(getContext(), R.drawable.class.getField("picto_flag_" + codeCountry.toLowerCase()).getInt(null));
            imgCountry.setImageDrawable(countryFlagImg);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        checkValidPhoneNumber();
    }

    private void checkValidPhoneNumber() {
        if (editable && !StringUtils.isEmpty(PhoneNumberView.this.getPhoneNumberInput())) {
            currentPhoneNumber = phoneUtils.formatMobileNumber(PhoneNumberView.this.getPhoneNumberInput(), countryCode);
            String viewPhoneNumber = phoneUtils.formatPhoneNumberForView(PhoneNumberView.this.getPhoneNumberInput(), countryCode);
            if (viewPhoneNumber != null) {
                editable = false;
                editTxtPhoneNumber.setText(viewPhoneNumber);
                editTxtPhoneNumber.setSelection(editTxtPhoneNumber.getText().length());
                editable = true;
            }
        }
    }

    public Observable<Boolean> phoneNumberValid() {
        return phoneNumberValid;
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

    public Observable<Void> countryClick() {
        return countryClickEventSubject;
    }

    public Observable<Void> nextClick() {
        return nextClick;
    }

    public void setPhoneUtils(PhoneUtils phoneUtils) {
        this.phoneUtils = phoneUtils;

        subscriptions.add(RxTextView.textChanges(editTxtPhoneNumber).map((charSequence) -> charSequence.toString())
                .filter(s -> s != null && s.length() > 2)
                .doOnNext(s -> checkValidPhoneNumber())
                .map(s -> currentPhoneNumber != null)
                .subscribe(phoneNumberValid));

        initWithCodeCountry(countryCode);
    }

    public void setNextEnabled(boolean enabled) {
        if (enabled) {
            btnNext.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_next_icon_black));
            btnNext.setClickable(true);
            btnNext.setEnabled(true);
        } else {
            btnNext.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_next_icon));
            btnNext.setClickable(false);
            btnNext.setEnabled(false);
        }
    }

    public void setNextVisible(boolean visible) {
        if (visible) {
            btnNext.clearAnimation();
            btnNext.setAlpha(1f);
            btnNext.setVisibility(VISIBLE);
        } else {
            btnNext.setVisibility(INVISIBLE);
        }
    }

    public void openKeyboard() {
        editTxtPhoneNumber.requestFocus();
        editTxtPhoneNumber.postDelayed(() -> {
            InputMethodManager keyboard = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(editTxtPhoneNumber, 0);
        }, 200);
    }


    public void fadeOutNext() {
        AnimationUtils.fadeOutFast(btnNext);
    }

    public void nextIconVisisble() {
        btnNext.setAlpha(1f);
    }

    public void progressViewVisible(boolean visible) {
        if (visible) {
            progressView.setVisibility(VISIBLE);
        } else {
            progressView.setVisibility(INVISIBLE);
        }
    }
}
