package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
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
import rx.functions.Action1;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 10/06/2016.
 */
public class PhoneNumberView extends FrameLayout {

    @BindView(R.id.editTextPhoneNumber)
    EditTextFont editTextPhoneNumber;

    @BindView(R.id.imgCountryCode)
    ImageView imgCountryCode;

    @BindView(R.id.imageViewNextIcon)
    ImageView imageViewNextIcon;

    @BindView(R.id.circularProgressViewPhoneNumber)
    CircularProgressView circularProgressViewPhoneNumber;

    // VARIABLES
    private PhoneUtils phoneUtils;
    private String countryCode = "US";
    private String currentPhoneNumber;
    private Context context;

    // OBSERVABLES

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

        RxView.clicks(imgCountryCode)
                .subscribe(countryClickEventSubject);


    }

    public void initWithCodeCountry(String codeCountry) {
        countryCode = codeCountry;
        String countryName = (new Locale("", codeCountry).getDisplayCountry()).toUpperCase();
        String countryCode = "+" + phoneUtils.getCountryCodeForRegion(codeCountry);
        try {
            Drawable countryFlagImg = context.getDrawable(R.drawable.class.getField("picto_flag_" + codeCountry.toLowerCase()).getInt(null));
            imgCountryCode.setImageDrawable(countryFlagImg);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
//        txtCountryCode.setText(countryName + " (" + countryCode + ")");
//        txtCountryCode.setText(countryCode);
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

        RxTextView.textChanges(editTextPhoneNumber).map((charSequence) -> charSequence.toString())
                .filter(s -> {
                    return s != null && !s.isEmpty();
                })
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        currentPhoneNumber = phoneUtils.formatMobileNumber(PhoneNumberView.this.getPhoneNumberInput(), countryCode);
                    }
                })
                .map(s -> currentPhoneNumber != null)
                .subscribe(phoneNumberValid);

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

    public void progressViewVisible(boolean visible) {
        if (visible) {
            circularProgressViewPhoneNumber.setVisibility(VISIBLE);
        } else {
            circularProgressViewPhoneNumber.setVisibility(INVISIBLE);
        }
    }

}
