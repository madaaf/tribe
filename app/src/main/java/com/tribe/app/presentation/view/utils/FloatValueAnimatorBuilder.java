package com.tribe.app.presentation.view.utils;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;

public class FloatValueAnimatorBuilder {

    final ValueAnimator animator;

    EndListener endListener;

    public interface UpdateListener {
        void onUpdate(float linearTime);
    }

    public interface EndListener {
        void onEnd();
    }

    public FloatValueAnimatorBuilder() {
        this(false);
    }

    public FloatValueAnimatorBuilder(boolean reverse) {
        if (reverse) {
            this.animator = ValueAnimator.ofFloat(1.0f, 0.0f);
        } else {
            this.animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        }
    }

    public FloatValueAnimatorBuilder delayBy(long millis) {
        animator.setStartDelay(millis);
        return this;
    }

    public FloatValueAnimatorBuilder duration(long millis) {
        animator.setDuration(millis);
        return this;
    }

    public FloatValueAnimatorBuilder interpolator(TimeInterpolator interpolator) {
        animator.setInterpolator(interpolator);
        return this;
    }

    public FloatValueAnimatorBuilder repeat(int times) {
        animator.setRepeatCount(times);
        return this;
    }

    public FloatValueAnimatorBuilder onUpdate(final UpdateListener listener) {
        animator.addUpdateListener(animation -> listener.onUpdate((float) animation.getAnimatedValue()));
        return this;
    }

    public FloatValueAnimatorBuilder onEnd(final EndListener listener) {
        this.endListener = listener;
        return this;
    }

    public ValueAnimator build() {
        if (endListener != null) {
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    endListener.onEnd();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        }

        return animator;
    }
}