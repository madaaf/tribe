package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * AccessLockView.java
 * Created by horatiothomas on 8/18/16.
 * Component used in AccessFragment.java to create the lock view with animations
 */
public class AccessLockView extends FrameLayout {

    private final static int DURATION = 300;
    private final static int DURATION_SHORT = 100;
    private final static int PULSATING_DURATION = 1200;

    private static final int STATE_GET_ACCESS = 0;
    private static final int STATE_HANG_TIGHT = 1;
    private static final int STATE_SORRY = 2;
    private static final int STATE_CONGRATS = 3;
    private static int viewState;

    /**
     * Globals
     */
    @BindView(R.id.pulse)
    View viewPulse;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.whiteCircle)
    View whiteCircle;

    @BindView(R.id.imgLockIcon)
    ImageView imgLockIcon;

    @BindView(R.id.txtNumFriends)
    TextSwitcher txtNumFriends;

    @BindView(R.id.layoutFriends)
    ViewGroup layoutFriends;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private ScreenUtils screenUtils;
    private boolean isEnd = true;

    // RESOURCES
    private int totalTimeSynchro;

    public AccessLockView(Context context) {
        super(context);
    }

    public AccessLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Lifecycle methods
     */

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_access_lock, this);
        unbinder = ButterKnife.bind(this);

        screenUtils = ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().screenUtils();
        totalTimeSynchro = getContext().getResources().getInteger(R.integer.time_synchro);

        setToAccessFirstTime();
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

    /**
     * Modify view methods
     */

    public void setToAccess() {
        setAccessState();

        AnimationUtils.fadeViewDownOut(layoutFriends, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                imgLockIcon.animate()
                        .alpha(1)
                        .setDuration(DURATION)
                        .translationY(0)
                        .setListener(null)
                        .start();

                layoutFriends.animate().setListener(null).start();
            }
        });
    }

    public void setToAccessFirstTime() {
        setAccessState();
        imgLockIcon.setAlpha(1f);
        imgLockIcon.setTranslationY(0);

        AnimationUtils.fadeViewDownOut(layoutFriends, null);
    }

    private void setAccessState() {
        viewState = STATE_GET_ACCESS;

        subscriptions.clear();
        greyPulse();

        txtNumFriends.setText("0");
        progressBar.setProgress(0);
        progressBar.setVisibility(INVISIBLE);
    }

    public void fadeBigLockIn() {
        imgLockIcon.setScaleX(10);
        imgLockIcon.setScaleY(10);
        imgLockIcon.setAlpha(1f);
        imgLockIcon.animate()
                .scaleX(1).scaleY(1)
                .setStartDelay(0)
                .setDuration(600)
                .start();
    }

    private void fadeTextIn() {
        AnimationUtils.fadeViewUpIn(layoutFriends);
    }

    private void fadeLockIn() {
        AnimationUtils.fadeViewUpIn(imgLockIcon);
    }

    public void setToHangTight(int numFriends) {
        txtNumFriends.setText(String.valueOf(numFriends));

        if (viewState != STATE_HANG_TIGHT) {
            viewState = STATE_HANG_TIGHT;

            imgLockIcon.animate()
                    .alpha(0)
                    .setDuration(DURATION)
                    .translationY(screenUtils.dpToPx(25))
                    .setStartDelay(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (viewState == STATE_HANG_TIGHT) fadeTextIn();
                            imgLockIcon.animate().setListener(null).start();
                        }
                    }).start();

            subscriptions.clear();
            greyPulse();

            progressBar.setVisibility(VISIBLE);
        }
    }

    public void setToSorry() {
        viewState = STATE_SORRY;

        if (viewState == STATE_SORRY) fadeLockIn();

        layoutFriends.animate()
                .alpha(0)
                .setDuration(DURATION)
                .translationY(screenUtils.dpToPx(25))
                .setStartDelay(0)
                .start();

        subscriptions.clear();
        redPulse();

        progressBar.setVisibility(INVISIBLE);
    }

    public void setToCongrats() {
        viewState = STATE_CONGRATS;
        subscriptions.clear();
        removePulsingCircleAnimation();
    }

    public void setViewWidthHeight(int whiteCircleWidth, int pulseWidth) {
        setNewWidthAndHeight(whiteCircle, whiteCircleWidth, whiteCircleWidth);
        setNewWidthAndHeight(progressBar, whiteCircleWidth, whiteCircleWidth);
        setNewWidthAndHeight(layoutFriends, whiteCircleWidth - screenUtils.dpToPx(20), whiteCircleWidth - screenUtils.dpToPx(20));
        setNewWidthAndHeight(viewPulse, pulseWidth, pulseWidth);
    }

    private void removePulsingCircleAnimation() {
        Drawable backgrounds[] = new Drawable[2];
        backgrounds[0] = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_circle_grey, null);
        backgrounds[1] = ResourcesCompat.getDrawable(getResources(), R.drawable.shadow_circle_white, null);

        TransitionDrawable crossfader = new TransitionDrawable(backgrounds);
        viewPulse.setBackground(crossfader);
        viewPulse.animate().scaleX(0).scaleY(0).setDuration(600).start();
        crossfader.startTransition(PULSATING_DURATION);
    }

    private void bluePulse() {
        Drawable backgrounds[] = new Drawable[2];
        backgrounds[0] = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_circle_grey, null);
        backgrounds[1] = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_circle_blue, null);
        changePulse(backgrounds);
    }

    private void greyPulse() {
        Drawable backgrounds[] = new Drawable[2];
        backgrounds[0] = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_circle_grey_light, null);
        backgrounds[1] = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_circle_grey, null);
        changePulse(backgrounds);
    }

    private void redPulse() {
        Drawable backgrounds[] = new Drawable[2];
        backgrounds[0] = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_circle_grey, null);
        backgrounds[1] = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_circle_red, null);
        changePulse(backgrounds);
    }

    private void changePulse(Drawable[] backgrounds) {
        TransitionDrawable crossfader = new TransitionDrawable(backgrounds);
        viewPulse.setBackground(crossfader);
        crossfader.startTransition(PULSATING_DURATION * 2);

        expandAndContract(crossfader);

        subscriptions.add(Observable.interval(PULSATING_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                    expandAndContract(crossfader);
                }));
    }

    private void setNewWidthAndHeight(View view, int width, int height) {
        LayoutParams viewLayoutParams = (LayoutParams) view.getLayoutParams();
        viewLayoutParams.height = height;
        viewLayoutParams.width = width;
        viewLayoutParams.gravity = Gravity.CENTER;
        view.setLayoutParams(viewLayoutParams);
    }

    private void expandAndContract(TransitionDrawable crossfader) {
        if (isEnd) {
            isEnd = false;
            crossfader.reverseTransition(PULSATING_DURATION);
            viewPulse.animate()
                    .scaleY((float) 1)
                    .scaleX((float) 1)
                    .setStartDelay(0)
                    .setDuration(PULSATING_DURATION)
                    .start();
        } else {
            isEnd = true;
            crossfader.startTransition(PULSATING_DURATION * 2);
            viewPulse.animate()
                    .scaleY((float) 1.2)
                    .scaleX((float) 1.2)
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
}
