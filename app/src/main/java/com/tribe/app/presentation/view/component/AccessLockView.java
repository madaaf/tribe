package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.SemiCircleView;
import com.tribe.app.presentation.view.widget.TextViewFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

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

    @BindView(R.id.pulse)
    View viewPulse;

    @BindView(R.id.imgLockIcon)
    ImageView imgLockIcon;

    @BindView(R.id.semiCircleView)
    SemiCircleView semiCircleView;

    @BindView(R.id.txtNumFriends)
    TextViewFont txtNumFriends;

    @BindView(R.id.txtFriends)
    TextViewFont txtFriends;

    ObjectAnimator pulseAnimation;

    /**
     * Lifecycle methods
     */

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_access_lock, this);
        unbinder = ButterKnife.bind(this);

        // TODO: change to red for sorry

        pulseAnimation = ObjectAnimator.ofPropertyValuesHolder(viewPulse,
                PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1.2f));

        pulseAnimation.setDuration(600);
        pulseAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        pulseAnimation.setRepeatMode(ObjectAnimator.REVERSE);

        pulseAnimation.start();

    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();
        super.onDetachedFromWindow();
    }

    /**
     * Modify view methods
     */

    public void setToAccess() {
//        viewState = STATE_GET_ACCESS;
        imgLockIcon.setVisibility(VISIBLE);
        txtNumFriends.setVisibility(INVISIBLE);
        txtFriends.setVisibility(INVISIBLE);

        resetSemiCircle(0);
        setViewPulseGrey();
    }


    public void setToHangTight(int numFriends) {
//        viewState = STATE_HANG_TIGHT;
        imgLockIcon.setVisibility(INVISIBLE);
        txtNumFriends.setVisibility(VISIBLE);
        txtNumFriends.setText(String.valueOf(numFriends));
        txtFriends.setVisibility(VISIBLE);

        resetSemiCircle(numFriends);
        setViewPulseGrey();
    }

    public void setToSorry() {
//        viewState = STATE_SORRY;
        imgLockIcon.setVisibility(VISIBLE);
        txtNumFriends.setVisibility(INVISIBLE);
        txtFriends.setVisibility(INVISIBLE);

        resetSemiCircle(0);
        setViewPulseRed();
    }

    public void setToCongrats() {
//        viewState = STATE_CONGRATS;
        imgLockIcon.setVisibility(INVISIBLE);
        txtNumFriends.setVisibility(VISIBLE);
        txtNumFriends.setText("3");
        txtFriends.setVisibility(VISIBLE);

        resetSemiCircle(3);
        setViewPulseStopped();
    }

    private void resetSemiCircle(int friends) {

        int circleRadiusFriends;

        switch (friends) {
            case 0:
                circleRadiusFriends = SemiCircleView.NO_FRIENDS;
                break;
            case 1:
                circleRadiusFriends = SemiCircleView.ONE_FRIEND;
                break;
            case 2:
                circleRadiusFriends = SemiCircleView.TWO_FRIENDS;
                break;
            case 3:
                circleRadiusFriends = SemiCircleView.THREE_FRIENDS;
                break;
            default:
                circleRadiusFriends = SemiCircleView.NO_FRIENDS;
                break;
        }

        semiCircleView.setCurrentFriends(circleRadiusFriends);
        semiCircleView.invalidate();
    }

    private void setViewPulseRed() {

    }

    private void setViewPulseGrey() {

    }

    private void setViewPulseStopped() {
        pulseAnimation.end();
        viewPulse.setVisibility(INVISIBLE);
    }

}
