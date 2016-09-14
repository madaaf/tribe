package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
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

    @BindView(R.id.editTextPhoneNumber)
    EditTextFont editTextPhoneNumber;

    @BindView(R.id.imgCountryCode)
    ImageView imgCountryCode;

    @BindView(R.id.imageViewNextIcon)
    ImageView imageViewNextIcon;

    @BindView(R.id.countryButton)
    View countryButton;

    @BindView(R.id.circularProgressViewPhoneNumber)
    CircularProgressView circularProgressViewPhoneNumber;

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
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_phone_number, this);
        unbinder = ButterKnife.bind(this);

        subscriptions.add(RxView.clicks(countryButton)
                .subscribe(countryClickEventSubject));
    }

    public void initWithCodeCountry(String codeCountry) {
        countryCode = codeCountry;

        try {
            Drawable countryFlagImg = context.getDrawable(R.drawable.class.getField("picto_flag_" + codeCountry.toLowerCase()).getInt(null));
            imgCountryCode.setImageDrawable(countryFlagImg);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        checkValidPhoneNumber();
    }

    private void checkValidPhoneNumber() {
        if (editable) {
            currentPhoneNumber = phoneUtils.formatMobileNumber(PhoneNumberView.this.getPhoneNumberInput(), countryCode);
            String viewPhoneNumber = phoneUtils.formatPhoneNumberForView(PhoneNumberView.this.getPhoneNumberInput(), countryCode);
            if (viewPhoneNumber != null) {
                editable = false;
                editTextPhoneNumber.setText(viewPhoneNumber);
                editTextPhoneNumber.setSelection(editTextPhoneNumber.getText().length());
                editable = true;
            }
        }
    }

    public ImageView getImageViewNextIcon() {
        return  this.imageViewNextIcon;
    }

    public Observable<Boolean> phoneNumberValid() {
        return phoneNumberValid;
    }

    public String getPhoneNumberInput() {
        return editTextPhoneNumber.getText().toString();
    }

    public String getPhoneNumberFormatted() {
        return currentPhoneNumber;
    }

    public Observable<Void> countryClick() {
        return countryClickEventSubject;
    }

    public void setPhoneUtils(PhoneUtils phoneUtils) {
        this.phoneUtils = phoneUtils;

        subscriptions.add(RxTextView.textChanges(editTextPhoneNumber).map((charSequence) -> charSequence.toString())
                .filter(s -> s != null && s.length() > 2)
                .doOnNext(s -> checkValidPhoneNumber())
                .map(s -> currentPhoneNumber != null)
                .subscribe(phoneNumberValid));

        initWithCodeCountry(countryCode);
    }

    public void setNextEnabled(boolean enabled) {
        if (enabled) {
            imageViewNextIcon.setImageDrawable(context.getDrawable(R.drawable.picto_next_icon_black));
            imageViewNextIcon.setClickable(true);
        } else {
            imageViewNextIcon.setImageDrawable(context.getDrawable(R.drawable.picto_next_icon));
            imageViewNextIcon.setClickable(false);
        }
    }

    public void setNextVisible(boolean visible) {
        if (visible) {
            imageViewNextIcon.setVisibility(VISIBLE);
        } else {
            imageViewNextIcon.setVisibility(INVISIBLE);
        }
    }

    public void fadeOutNext() {
        AnimationUtils.fadeOutFast(imageViewNextIcon);
    }

    public void nextIconVisisble() {
        imageViewNextIcon.setAlpha(1f);
    }

    public void progressViewVisible(boolean visible) {
        if (visible) {
            circularProgressViewPhoneNumber.setVisibility(VISIBLE);
        } else {
            circularProgressViewPhoneNumber.setVisibility(INVISIBLE);
        }
    }
}
