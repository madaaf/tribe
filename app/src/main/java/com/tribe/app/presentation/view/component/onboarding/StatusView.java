package com.tribe.app.presentation.view.component.onboarding;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.IntDef;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/10/2016.
 */

public class StatusView extends FrameLayout {

    private static final int DURATION = 300;

    public static final int DISCLAIMER = 0;
    public static final int SENDING = 1;
    public static final int SENT = 2;
    public static final int RESEND = 3;

    @IntDef({DISCLAIMER, SENDING, SENT, RESEND})
    public @interface StatusType{}

    @BindView(R.id.txtStatus)
    TextViewFont txtStatus;

    @BindView(R.id.viewBG)
    View viewBG;

    // VARIABLES
    private @StatusType int status;
    private LayerDrawable background;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    private Unbinder unbinder;

    public StatusView(Context context) {
        super(context);
    }

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

        LayoutInflater.from(getContext()).inflate(R.layout.view_status, this);
        unbinder = ButterKnife.bind(this);

        init();
        initDependencyInjector();
    }

    private void init() {
        status = DISCLAIMER;

        background = (LayerDrawable) viewBG.getBackground();
        background.getDrawable(0).setAlpha(0);
        background.getDrawable(1).setAlpha(0);
        background.getDrawable(2).setAlpha(255);
    }

    public @StatusType int getStatus() {
        return status;
    }

    public void showDisclaimer() {
        AnimationUtils.crossFadeDrawable(background, drawableFrom(), 2, DURATION);
        TextViewCompat.setTextAppearance(txtStatus, R.style.Small_1_Black40);
        txtStatus.setText(R.string.onboarding_phone_disclaimer);
        status = DISCLAIMER;
    }

    public void showSendingCode() {
        AnimationUtils.crossFadeDrawable(background, drawableFrom(), 1, DURATION);
        TextViewCompat.setTextAppearance(txtStatus, R.style.Title_1_White);
        txtStatus.setText(R.string.onboarding_code_sending_status);
        status = SENDING;
    }

    public void showCodeSent(String phoneNumber) {
        TextViewCompat.setTextAppearance(txtStatus, R.style.Title_1_White);
        txtStatus.setText(getContext().getString(R.string.onboarding_code_sent_status, phoneNumber));
        status = SENT;
    }

    public void showResend() {
        AnimationUtils.crossFadeDrawable(background, drawableFrom(), 0, DURATION);
        TextViewCompat.setTextAppearance(txtStatus, R.style.Title_1_White);
        txtStatus.setText(R.string.onboarding_code_resend_status);
        status = RESEND;
    }

    private int drawableFrom() {
        int drawableFrom = 0;

        switch (status) {
            case RESEND :
                drawableFrom = 0;
                break;

            case SENDING : case SENT :
                drawableFrom = 1;
                break;

            case DISCLAIMER :
                drawableFrom = 2;
                break;
        }

        return drawableFrom;
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
                .build().inject(this);
    }
}
