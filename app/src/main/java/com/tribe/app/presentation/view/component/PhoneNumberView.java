package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 10/06/2016.
 */
public class PhoneNumberView extends FrameLayout {

    @BindView(R.id.editTextPhoneNumber)
    EditTextFont editTextPhoneNumber;

    @BindView(R.id.txtCountryCode)
    TextViewFont txtCountryCode;

    // VARIABLES
    private PhoneUtils phoneUtils;
    private String countryCode = "FR";
    private String currentPhoneNumber;

    // OBSERVABLES

    private Unbinder unbinder;
    private PublishSubject<Boolean> phoneNumberValid = PublishSubject.create();
    private PublishSubject<Void> countryClickEventSubject = PublishSubject.create();

    public PhoneNumberView(Context context) {
        super(context);
    }

    public PhoneNumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhoneNumberView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

        RxView.clicks(txtCountryCode)
                .subscribe(countryClickEventSubject);
    }

    public void initWithCodeCountry(String codeCountry) {
        countryCode = codeCountry;
        String countryName = (new Locale("", codeCountry).getDisplayCountry()).toUpperCase();
        String countryCode = "+" + phoneUtils.getCountryCodeForRegion(codeCountry);
        txtCountryCode.setText(countryName + " (" + countryCode + ")");
        txtCountryCode.setText(countryCode);
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

        RxTextView.textChanges(editTextPhoneNumber).map(CharSequence::toString)
                .doOnNext(s -> currentPhoneNumber = phoneUtils.formatMobileNumber(getPhoneNumberInput(), countryCode))
                .map(s -> currentPhoneNumber != null)
                .subscribe(phoneNumberValid);

        initWithCodeCountry(countryCode);
    }
}