package com.tribe.app.presentation.view.component.onboarding;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.IntDef;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * AccessView.java
 * Created by tiago on 12/14/16
 */
public class AccessView extends LinearLayout {

    private final static int DURATION = 300;
    private final static int DURATION_SHORT = 100;
    private final static int DURATION_MEDIUM = 400;
    private final static int PULSATING_DURATION = 1200;

    @IntDef({NONE, LOADING, DONE})
    public @interface StatusType{}

    public static final int NONE = 0;
    public static final int LOADING = 1;
    public static final int DONE = 2;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.layoutPulse)
    ViewGroup layoutPulse;

    @BindView(R.id.viewPulse)
    View viewPulse;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.imgCircle)
    ImageView imgCircle;

    @BindView(R.id.imgIcon)
    ImageView imgIcon;

    @BindView(R.id.txtNumFriends)
    TextSwitcher txtNumFriends;

    @BindView(R.id.layoutFriends)
    ViewGroup layoutFriends;

    @BindView(R.id.txtStatus)
    TextViewFont txtStatus;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private @StatusType int status;
    private boolean isEnd = true;
    private int nbFriends = 0;

    // RESOURCES
    private int totalTimeSynchro;

    public AccessView(Context context) {
        super(context);
    }

    public AccessView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_access_friends, this);
        unbinder = ButterKnife.bind(this);

        totalTimeSynchro = getContext().getResources().getInteger(R.integer.time_synchro);

        initDependencyInjector();
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDetachedFromWindow();
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

    private void init() {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        status = NONE;

        hideView(layoutFriends, false);

        int circleSize = (int) (screenUtils.getWidthPx() * 0.4f);
        int pulseSize = circleSize + screenUtils.dpToPx(40);

        setLayout(imgCircle, circleSize, circleSize);
        setLayout(progressBar, circleSize, circleSize);
        setLayout(layoutFriends, circleSize - screenUtils.dpToPx(20), circleSize - screenUtils.dpToPx(20));
        setLayout(viewPulse, pulseSize, pulseSize);
        setLayout(layoutPulse, pulseSize + screenUtils.dpToPx(60), pulseSize + screenUtils.dpToPx(60));

        expandAndContract();

        subscriptions.add(Observable.interval(PULSATING_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .onBackpressureDrop()
                .subscribe(aVoid -> {
                    expandAndContract();
                }));

        imgIcon.setScaleX(8);
        imgIcon.setScaleY(8);
        imgIcon.animate()
                .scaleX(1)
                .scaleY(1)
                .setDuration(750)
                .setInterpolator(new OvershootInterpolator(0.45f))
                .start();
    }

    private void setLayout(View view, int width, int height) {
        ViewGroup.LayoutParams viewLayoutParams = view.getLayoutParams();
        viewLayoutParams.height = height;
        viewLayoutParams.width = width;
        view.setLayoutParams(viewLayoutParams);
    }

    private void removePulsingCircleAnimation() {
        Drawable backgrounds[] = new Drawable[2];
        backgrounds[0] = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_circle_black_3, null);
        backgrounds[1] = ResourcesCompat.getDrawable(getResources(), R.drawable.shadow_circle_white, null);

        TransitionDrawable transitionDrawable = new TransitionDrawable(backgrounds);
        viewPulse.setBackground(transitionDrawable);
        viewPulse.animate().scaleX(0).scaleY(0).setDuration(600).start();
        transitionDrawable.startTransition(PULSATING_DURATION);
    }

    private void expandAndContract() {
        if (isEnd) {
            isEnd = false;
            viewPulse.animate()
                    .scaleY(1.1f)
                    .scaleX(1.1f)
                    .setStartDelay(0)
                    .setDuration(PULSATING_DURATION)
                    .start();
        } else {
            isEnd = true;
            viewPulse.animate()
                    .scaleY(1.3f)
                    .scaleX(1.3f)
                    .setStartDelay(0)
                    .setDuration(PULSATING_DURATION)
                    .start();
        }
    }

    public void animateProgress() {
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", progressBar.getMax());
        animation.setDuration(totalTimeSynchro);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    public @StatusType int getStatus() {
        return status;
    }

    public void showLoading(int nbFriends) {
        this.nbFriends = nbFriends;
        txtNumFriends.setText("" + nbFriends);

        if (status != LOADING) {
            status = LOADING;

            progressBar.clearAnimation();
            progressBar.setProgress(0);

            txtStatus.setText(R.string.onboarding_queue_loading_description);

            hideView(imgIcon, true);
            layoutFriends.postDelayed(() -> showView(layoutFriends, true), DURATION >> 1);
        }
    }

    public void showCongrats() {
        if (status != DONE) {
            status = DONE;

            txtStatus.setText(R.string.onboarding_queue_valid_description);

            imgIcon.setImageResource(R.drawable.picto_tick_access);

            hideView(layoutFriends, true);
            imgIcon.postDelayed(() -> showView(imgIcon, true), DURATION >> 1);

            subscriptions.clear();
            removePulsingCircleAnimation();
        }
    }

    private void hideView(View view, boolean animate) {
        view.animate()
                .alpha(0)
                .translationY(screenUtils.dpToPx(20))
                .setDuration(animate ? DURATION : 0)
                .setStartDelay(0)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void showView(View view, boolean animate) {
        view.animate()
                .alpha(1)
                .translationY(0)
                .setDuration(animate ? DURATION : 0)
                .setStartDelay(0)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}
