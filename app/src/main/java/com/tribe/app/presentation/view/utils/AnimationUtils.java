package com.tribe.app.presentation.view.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tribe.app.R;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tiago on 26/05/2016.
 */
public class AnimationUtils {

    private static final float OVERSHOOT_REPLACE = 1f;
    private static final int DURATION_REPLACE = 300;

    public static final float SCALE_RESET = 1f;
    public static final float SCALE_INVISIBLE = 0f;
    public static final int TRANSLATION_RESET = 0;
    public static final int NO_START_DELAY = 0;
    public static final float ALPHA_FULL = 1;
    public static final float ALPHA_NONE = 0;
    public static final int ANIMATION_DURATION_EXTRA_SHORT = 100;
    public static final int ANIMATION_DURATION_SHORT = 300;
    public static final int ANIMATION_DURATION_MID = 500;

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

    public static void scaleUp(final View view, long duration) {
        scaleUp(view, duration, 0, new LinearInterpolator());
    }

    public static void scaleDown(final View view, long duration, long delay, Interpolator interpolator) {
        view.setScaleX(1);
        view.setScaleY(1);
        view.animate().scaleX(0).scaleY(0).setInterpolator(interpolator).setDuration(duration).setStartDelay(delay).start();
    }

    public static void scaleDown(final View view, long duration, Interpolator interpolator) {
        scaleDown(view, duration, 0, interpolator);
    }

    public static void scaleDown(final View view, long duration) {
        scaleDown(view, duration, 0, new LinearInterpolator());
    }

    public static void fadeOut(View v, long duration) {
        v.animate().alpha(0).setStartDelay(NO_START_DELAY).setInterpolator(new DecelerateInterpolator()).setDuration(duration).start();
    }

    public static void fadeIn(View v, long duration) {
        v.animate().alpha(1).setStartDelay(NO_START_DELAY).setInterpolator(new DecelerateInterpolator()).setDuration(duration).start();
    }

    public static void fadeOutAccelerate(View v, long duration) {
        v.animate().alpha(0).setStartDelay(NO_START_DELAY).setInterpolator(new AccelerateInterpolator(2)).setDuration(duration).start();
    }

    public static void fadeInAccelerate(View v, long duration) {
        v.animate().alpha(1).setStartDelay(NO_START_DELAY).setInterpolator(new AccelerateInterpolator(2)).setDuration(duration).start();
    }

    public static void fadeViewDownOut(View view, Animator.AnimatorListener listener) {
        view.animate()
                .translationY(25)
                .alpha(0)
                .setDuration(DURATION_REPLACE)
                .setListener(listener)
                .start();
    }

    public static void fadeViewUpIn(View view) {
        view.animate()
                .translationY(0)
                .alpha(1).
                setDuration(DURATION_REPLACE)
                .start();
    }

    public static void fadeOutFast(View view) {
        view.animate()
                .setStartDelay(0)
                .alpha(0)
                .setDuration(50)
                .start();
    }

    public static void fadeViewInOut(View view, int distance) {
        view.animate()
                .alpha(0)
                .setDuration(DURATION_REPLACE)
                .translationY(distance)
                .setStartDelay(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.animate().alpha(1)
                                .setDuration(DURATION_REPLACE)
                                .translationY(0)
                                .setStartDelay(0)
                                .setListener(null)
                                .start();
                    }
                }).start();
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

    public static void scaleIn(View view, int duration) {
        view.animate()
                .scaleY((float) 1.2).scaleX((float) 1.2)
                .setDuration(duration/2)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.animate()
                                .scaleX(1).scaleY(1)
                                .setDuration(duration/2)
                                .start();
                    }
                }).start();
    }

    public static void collapseScale(View view, int duration) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 1f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f);
        scaleAnimation.setDuration(duration);
        scaleAnimation.setFillAfter(true);
        view.startAnimation(scaleAnimation);
    }

    public static void expandScale(View view, int duration) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f);
        scaleAnimation.setDuration(duration);
        scaleAnimation.setFillAfter(true);
        view.startAnimation(scaleAnimation);
    }

    public static void animateBottomMargin(View view, int margin, int duration) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(lp.bottomMargin, margin);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            lp.bottomMargin = (Integer) animation.getAnimatedValue();
            view.setLayoutParams(lp);
        });
        animator.start();
    }

    public static void animateLeftMargin(View view, int margin, int duration) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(lp.leftMargin, margin);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            lp.leftMargin = (Integer) animation.getAnimatedValue();
            view.setLayoutParams(lp);
        });
        animator.start();
    }

    public static void animateTopMargin(View view, int margin, int duration, Interpolator interpolator) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(lp.topMargin, margin);
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);
        animator.addUpdateListener(animation -> {
            lp.topMargin = (Integer) animation.getAnimatedValue();
            view.setLayoutParams(lp);
        });
        animator.start();
    }

    public static void animateHeightFrameLayout(View view, int startHeight, int endHeight, int duration) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            lp.height = (Integer) animation.getAnimatedValue();
            view.setLayoutParams(lp);
        });
        animator.start();
    }

    public static void animateHeightCoordinatorLayout(View view, int startHeight, int endHeight, int duration) {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            lp.height = (Integer) animation.getAnimatedValue();
            view.getLayoutParams().height = lp.height;
            view.requestLayout();
        });
        animator.start();
    }

    public static void animateHeightLinearLayout(View view, int startHeight, int endHeight, int duration) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            lp.height = (Integer) animation.getAnimatedValue();
            view.getLayoutParams().height = lp.height;
            view.requestLayout();
        });
        animator.start();
    }

    public static void animateSizeFrameLayout(View view, int endHeight, int duration) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(view.getWidth(), endHeight);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            lp.height = (Integer) animation.getAnimatedValue();
            lp.width = (Integer) animation.getAnimatedValue();
            view.getLayoutParams().height = lp.height;
            view.getLayoutParams().width = lp.width;
            view.requestLayout();
        });
        animator.start();
    }

    public static void scaleOldImageOutNewImageIn(ImageView imageView, Drawable oldDrawable, Drawable newDrawable) {
        imageView.setImageDrawable(oldDrawable);
        AnimationUtils.scaleDown(imageView, AnimationUtils.ANIMATION_DURATION_SHORT);
        Observable.timer(AnimationUtils.ANIMATION_DURATION_SHORT, TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    imageView.setImageDrawable(newDrawable);
                    AnimationUtils.scaleUp(imageView, ANIMATION_DURATION_SHORT);
                });
    }

    public static void animateTextColor(TextView txtView, int colorFrom, int colorTo, int duration) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(animator -> txtView.setTextColor((Integer) animator.getAnimatedValue()));
        colorAnimation.setDuration(duration);
        colorAnimation.setInterpolator(new DecelerateInterpolator());
        colorAnimation.start();
    }
}
