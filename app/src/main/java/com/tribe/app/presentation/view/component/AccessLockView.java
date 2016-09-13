package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

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

    @BindView(R.id.semiCircleView)
    View semiCircleView;

    @BindView(R.id.whiteCircle)
    View whiteCircle;

    @BindView(R.id.imgLockIcon)
    ImageView imgLockIcon;

    @BindView(R.id.txtNumFriends)
    TextViewFont txtNumFriends;

    @BindView(R.id.txtFriends)
    TextViewFont txtFriends;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private ScreenUtils screenUtils;
    private boolean isEnd = true;
    private int pulsingDuration = 1200;

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
        viewState = STATE_GET_ACCESS;

        subscriptions.clear();
        greyPulse();

        semiCircleView.setVisibility(INVISIBLE);
        imgLockIcon.setAlpha(1f);
        imgLockIcon.setTranslationY(0);

        AnimationUtils.fadeViewDownOut(txtFriends);
        AnimationUtils.fadeViewDownOut(txtNumFriends);
    }

    public void setToAccessFirstTime() {
        viewState = STATE_GET_ACCESS;

        subscriptions.clear();
        greyPulse();

        semiCircleView.setVisibility(INVISIBLE);
        imgLockIcon.setAlpha(1f);
        imgLockIcon.setTranslationY(0);

        AnimationUtils.fadeViewDownOut(txtFriends);
        AnimationUtils.fadeViewDownOut(txtNumFriends);
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
        AnimationUtils.fadeViewUpIn(txtFriends);
        AnimationUtils.fadeViewUpIn(txtNumFriends);
    }

    private void fadeLockIn() {
        AnimationUtils.fadeViewUpIn(imgLockIcon);
    }

    public void setToHangTight(int numFriends) {
        viewState = STATE_HANG_TIGHT;

        txtNumFriends.setText(String.valueOf(numFriends));

        imgLockIcon.animate()
                .alpha(0)
                .setDuration(300)
                .translationY(screenUtils.dpToPx(50))
                .setStartDelay(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (viewState == STATE_HANG_TIGHT) fadeTextIn();
                    }
                }).start();

        subscriptions.clear();
        greyPulse();

        semiCircleView.setVisibility(VISIBLE);
    }

    public void setToSorry() {
        viewState = STATE_SORRY;

        txtFriends.animate()
                .alpha(0)
                .setDuration(300)
                .translationY(screenUtils.dpToPx(25))
                .setStartDelay(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (viewState == STATE_SORRY) fadeLockIn();
                    }
                }).start();

        txtNumFriends.animate()
                .alpha(0)
                .setDuration(300)
                .translationY(screenUtils.dpToPx(25))
                .setStartDelay(0)
                .start();

        subscriptions.clear();
        redPulse();

        semiCircleView.setVisibility(INVISIBLE);
    }

    public void setToCongrats() {
        viewState = STATE_CONGRATS;
        txtNumFriends.setText("3");

        subscriptions.clear();
        bluePulse();
        subscriptions.add(Observable.interval(600, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                    removePulsingCircleAnimation();
                }));
    }

    public void setViewWidthHeight(int whiteCircleWidth, int pulseWidth) {
        whiteCircle.setLayoutParams(new LayoutParams(whiteCircleWidth, whiteCircleWidth));
        setNewWidthAndHeight(whiteCircle, whiteCircleWidth, whiteCircleWidth);
        setNewWidthAndHeight(semiCircleView, whiteCircleWidth, whiteCircleWidth);
        setNewWidthAndHeight(viewPulse, pulseWidth, pulseWidth);
    }

    private void removePulsingCircleAnimation() {
        Drawable backgrounds[] = new Drawable[2];
        backgrounds[0] = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_circle_grey, null);
        backgrounds[1] = ResourcesCompat.getDrawable(getResources(), R.drawable.shadow_circle_white, null);

        TransitionDrawable crossfader = new TransitionDrawable(backgrounds);
        viewPulse.setBackground(crossfader);
        viewPulse.animate().scaleX(0).scaleY(0).setDuration(600).start();
        crossfader.startTransition(1200);
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
        crossfader.startTransition(pulsingDuration * 2);

        expandAndContract(crossfader);

        subscriptions.add(Observable.interval(pulsingDuration, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
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
            crossfader.reverseTransition(pulsingDuration);
            viewPulse.animate()
                    .scaleY((float) 1)
                    .scaleX((float) 1)
                    .setStartDelay(0)
                    .setDuration(pulsingDuration)
                    .start();
        } else {
            isEnd = true;
            crossfader.startTransition(pulsingDuration * 2);
            viewPulse.animate()
                    .scaleY((float) 1.2)
                    .scaleX((float) 1.2)
                    .setStartDelay(0)
                    .setDuration(pulsingDuration)
                    .start();
        }
    }

}
