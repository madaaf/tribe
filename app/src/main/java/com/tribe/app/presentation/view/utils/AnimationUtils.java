package com.tribe.app.presentation.view.utils;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by tiago on 26/05/2016.
 */
public class AnimationUtils {

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
}
