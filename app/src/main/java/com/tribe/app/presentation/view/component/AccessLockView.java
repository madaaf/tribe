package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tribe.app.R;
import com.tribe.app.presentation.view.drawable.SemiCircleDrawable;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.widget.SemiCircleView;
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

    public AccessLockView(Context context) {
        super(context);
    }

    public AccessLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AccessLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AccessLockView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Globals
     */

    Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();


    @BindView(R.id.pulse)
    View viewPulse;

    @BindView(R.id.imgLockIcon)
    ImageView imgLockIcon;

    @BindView(R.id.semiCircleView)
    View semiCircleView;

    @BindView(R.id.txtNumFriends)
    TextViewFont txtNumFriends;

    @BindView(R.id.txtFriends)
    TextViewFont txtFriends;


    private static final int STATE_GET_ACCESS = 0;
    private static final int STATE_HANG_TIGHT = 1;
    private static final int STATE_SORRY = 2;
    private static final int STATE_CONGRATS = 3;
    private static int viewState;
    private boolean isRed = true;
    int oldSemiCircleBackground;
    int newSemiCircleBackground;

    /**
     * Lifecycle methods
     */


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_access_lock, this);
        unbinder = ButterKnife.bind(this);

        oldSemiCircleBackground = 0;

        newSemiCircleBackground = 0;

        setToAccess();

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
        addPulsingRedCircleAnimation();

        semiCircleView.setVisibility(INVISIBLE);

        // Fade big lock in
        imgLockIcon.setScaleX(5);
        imgLockIcon.setScaleY(5);
        imgLockIcon.animate().alpha(1).setDuration(0).setStartDelay(0).translationY(0).start();
        imgLockIcon.animate().scaleX(1).scaleY(1).setStartDelay(0).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
        AnimationUtils.fadeViewDownOut(txtFriends);
        AnimationUtils.fadeViewDownOut(txtNumFriends);


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
                .translationY(100)
                .setStartDelay(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (viewState == STATE_HANG_TIGHT) fadeTextIn();
                    }
                }).start();

        subscriptions.clear();
        viewPulse.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_circle_grey, null));
        subscriptions.add(Observable.interval(600, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                    if (isRed) {
                        isRed = false;
                        viewPulse.animate().scaleY((float) 1).scaleX((float) 1).setDuration(600).start();
                    } else {
                        isRed = true;
                        viewPulse.animate().scaleY((float) 1.2).scaleX((float) 1.2).setDuration(600).start();
                    }
                }));

        semiCircleView.setVisibility(VISIBLE);
        resetSemiCircle(numFriends);
    }

    public void setToSorry() {
        viewState = STATE_SORRY;

        txtFriends.animate()
                .alpha(0)
                .setDuration(300)
                .translationY(100)
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
                .translationY(100)
                .setStartDelay(0)
                .start();

        subscriptions.clear();
        addPulsingRedCircleAnimation();


        semiCircleView.setVisibility(INVISIBLE);
    }

    public void setToCongrats() {
        viewState = STATE_CONGRATS;
        txtNumFriends.setText("3");

        subscriptions.clear();

        removePulsingCircleAnimation();

        resetSemiCircle(3);
    }

    private void resetSemiCircle(int friends) {

        int circleRadiusFriends;

        oldSemiCircleBackground = newSemiCircleBackground;

        switch (friends) {
            case 0:
                circleRadiusFriends = 0;
                break;
            case 1:
                circleRadiusFriends = 120;
                break;
            case 2:
                circleRadiusFriends = 240;
                break;
            case 3:
                circleRadiusFriends = 360;
                break;
            default:
                circleRadiusFriends = 0;
                break;
        }

        AnimationDrawable animationDrawable = new AnimationDrawable();
        animationDrawable.setOneShot(true);
        newSemiCircleBackground = circleRadiusFriends;
        // TODO: fix this
        for (int i = oldSemiCircleBackground; i <= newSemiCircleBackground; i++) {
            animationDrawable.addFrame(new SemiCircleDrawable(ContextCompat.getColor(getContext(), R.color.blue_text),
                    semiCircleView.getWidth(),
                    semiCircleView.getHeight(),
                    i), 10);
        }
        semiCircleView.setBackground(animationDrawable);
        animationDrawable.start();


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

    private void addPulsingRedCircleAnimation() {

        Drawable backgrounds[] = new Drawable[2];
        backgrounds[0] = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_circle_grey, null);
        backgrounds[1] = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_circle_red, null);

        TransitionDrawable crossfader = new TransitionDrawable(backgrounds);
        viewPulse.setBackground(crossfader);
        crossfader.startTransition(2400);

        subscriptions.add(Observable.interval(1200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                    if (isRed) {
                        isRed = false;
                        crossfader.reverseTransition(1200);
                        viewPulse.animate().scaleY((float) 1).scaleX((float) 1).setDuration(600).start();
                    } else {
                        isRed = true;
                        crossfader.startTransition(2400);
                        viewPulse.animate().scaleY((float) 1.2).scaleX((float) 1.2).setDuration(600).start();
                    }
                }));
    }
}
