package com.tribe.app.presentation.view.utils;

import android.animation.Animator;
import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import com.tribe.app.R;

/**
 * Created by tiago on 26/05/2016.
 */
public class AnimationUtils {

    private static final float OVERSHOOT_REPLACE = 1f;
    private static final int DURATION_REPLACE = 300;

    public static Animation fadeInAnimation(final View view, long duration, long delay) {
        Animation animation = new AlphaAnimation(0, 1);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setDuration(duration);
        animation.setStartOffset(delay);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.GONE);
            }
        });

        return animation;
    }

    public static Animation fadeOutAnimation(final View view, long duration, long delay) {
        Animation animation = new AlphaAnimation(1, 0);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setDuration(duration);
        animation.setStartOffset(delay);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }
        });

        return animation;
    }

    public static void scaleUp(final View view, long duration, long delay, Interpolator interpolator) {
        view.setScaleX(0);
        view.setScaleY(0);
        view.animate().scaleX(1).scaleY(1).setInterpolator(interpolator).setDuration(duration).setStartDelay(delay).start();
    }

    public static void scaleUp(final View view, long duration, Interpolator interpolator) {
        scaleUp(view, duration, 0, interpolator);
    }

    public static void scaleDown(final View view, long duration, long delay) {
        view.setScaleX(1);
        view.setScaleY(1);
        view.animate().scaleX(0).scaleY(0).setInterpolator(new DecelerateInterpolator()).setDuration(duration).setStartDelay(delay).start();
    }

    public static void scaleDown(final View view, long duration) {
        scaleDown(view, duration, 0);
    }

    public static void fadeOut(View v, long duration) {
        v.animate().alpha(0).setInterpolator(new DecelerateInterpolator()).setDuration(duration).start();
    }

    public static void fadeIn(View v, long duration) {
        v.animate().alpha(1).setInterpolator(new DecelerateInterpolator()).setDuration(duration).start();
    }

    public static void fadeViewDownOut(View view) {
        view.animate().translationY(100).alpha(0).setDuration(300).start();
    }

    public static void fadeViewUpIn(View view) {
        view.animate().translationY(0).alpha(1).setDuration(300).start();
    }

    /**
     *
     * @param context
     * @param v1 the view that will be animated out of the screen
     * @param v2 the view coming in to the screen
     */
    public static void replaceView(Context context, View v1, View v2, Animator.AnimatorListener listener) {
        int translateOut = context.getResources().getDimensionPixelSize(R.dimen.transition_replace);
        v1.animate().alpha(0).translationY(translateOut).setDuration(DURATION_REPLACE).setInterpolator(new OvershootInterpolator(OVERSHOOT_REPLACE)).start();
        v2.animate().alpha(1).translationY(0).setDuration(DURATION_REPLACE).setInterpolator(new OvershootInterpolator(OVERSHOOT_REPLACE)).setListener(listener).start();
    }
}
