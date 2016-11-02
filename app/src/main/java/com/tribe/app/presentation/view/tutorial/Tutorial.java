package com.tribe.app.presentation.view.tutorial;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import butterknife.ButterKnife;

/**
 * Created by tanjunrong on 2/10/15.
 */
public class Tutorial {

    @IntDef({CLICK, HORIZONTAL_LEFT, HORIZONTAL_RIGHT, VERTICAL_UPWARD, VERTICAL_DOWNWARD})
    public @interface Technique {
    }

    public static final int CLICK = 0;
    public static final int HORIZONTAL_LEFT = 1;
    public static final int HORIZONTAL_RIGHT = 2;
    public static final int VERTICAL_UPWARD = 3;
    public static final int VERTICAL_DOWNWARD = 4;

    @IntDef({ALLOW_ALL, CLICK_ONLY, SWIPE_ONLY})
    public @interface MotionType {
    }

    public static final int ALLOW_ALL = 0;
    public static final int CLICK_ONLY = 1;
    public static final int SWIPE_ONLY = 2;

    protected @Technique int technique;
    protected View highlightedView;
    private Activity activity;
    private ScreenUtils screenUtils;
    protected @MotionType int motionType;
    protected FrameLayoutWithHole frameLayout;
    private View toolTipViewGroup;
    public ToolTip toolTip;
    public Overlay overlay;
    public Pointer pointer;

    public static Tutorial init(Activity activity, ScreenUtils screenUtils) {
        return new Tutorial(activity, screenUtils);
    }

    public Tutorial(Activity activity, ScreenUtils screenUtils) {
        this.activity = activity;
        this.screenUtils = screenUtils;
    }

    public Tutorial with(@Technique int technique) {
        this.technique = technique;
        return this;
    }

    public Tutorial motionType(@MotionType int motionType) {
        this.motionType = motionType;
        return this;
    }

    public Tutorial playOn(View targetView) {
        highlightedView = targetView;
        setupView();
        return this;
    }

    public Tutorial setOverlay(Overlay overlay) {
        this.overlay = overlay;
        return this;
    }

    public Tutorial setPointer(Pointer pointer) {
        this.pointer = pointer;
        return this;
    }

    public Tutorial setToolTip(ToolTip toolTip) {
        this.toolTip = toolTip;
        return this;
    }

    public void cleanUp() {
        frameLayout.cleanUp();

        if (toolTipViewGroup != null) {
            ((ViewGroup) activity.getWindow().getDecorView()).removeView(toolTipViewGroup);
        }
    }

    public FrameLayoutWithHole getOverlay() {
        return frameLayout;
    }

    public View getToolTip() {
        return toolTipViewGroup;
    }

    private int getXBasedOnGravity(int width) {
        int[] pos = new int[2];
        highlightedView.getLocationOnScreen(pos);
        int x = pos[0];
        if ((pointer.gravity & Gravity.RIGHT) == Gravity.RIGHT) {
            return x + highlightedView.getWidth() - width;
        } else if ((pointer.gravity & Gravity.LEFT) == Gravity.LEFT) {
            return x;
        } else { // this is center
            return x + highlightedView.getWidth() / 2 - width / 2;
        }
    }

    private int getYBasedOnGravity(int height) {
        int[] pos = new int[2];
        highlightedView.getLocationInWindow(pos);
        int y = pos[1];

        if ((pointer.gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
            return y + highlightedView.getHeight() - height;
        } else if ((pointer.gravity & Gravity.TOP) == Gravity.TOP) {
            return y;
        } else { // this is center
            return y + highlightedView.getHeight() / 2 - height / 2;
        }
    }

    protected void setupView() {
        if (ViewCompat.isAttachedToWindow(highlightedView)) {
            startView();
        } else {
            final ViewTreeObserver viewTreeObserver = highlightedView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        highlightedView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        highlightedView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    startView();
                }
            });
        }
    }

    private void startView() {
        frameLayout = new FrameLayoutWithHole(activity, highlightedView, motionType, overlay);
        handleDisableClicking(frameLayout);
        setupFrameLayout();
        setupToolTip();
    }

    private void handleDisableClicking(FrameLayoutWithHole frameLayoutWithHole) {
        if (overlay != null && overlay.onClickListener != null) {
            frameLayoutWithHole.setClickable(true);
            frameLayoutWithHole.setOnClickListener(overlay.onClickListener);
        } else if (overlay != null && overlay.disableClick) {
            frameLayoutWithHole.setViewHole(highlightedView);
            frameLayoutWithHole.setSoundEffectsEnabled(false);
            frameLayoutWithHole.setOnClickListener(v -> {
            });
        }
    }

    private void setupToolTip() {
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        if (toolTip != null) {
            /* inflate and get views */
            ViewGroup parent = (ViewGroup) activity.getWindow().getDecorView();
            LayoutInflater layoutInflater = activity.getLayoutInflater();

            toolTipViewGroup = layoutInflater.inflate(R.layout.view_tutorial_tooltip, null);
            View toolTipContainer = ButterKnife.findById(toolTipViewGroup, R.id.containerTooltip);
            TextViewFont toolTipTitleTV = ButterKnife.findById(toolTipViewGroup, R.id.title);

            toolTipContainer.setBackgroundResource(toolTip.backgroundRes);

            if (toolTip.title == null || toolTip.title.isEmpty()) {
                toolTipTitleTV.setVisibility(View.GONE);
            } else {
                toolTipTitleTV.setVisibility(View.VISIBLE);
                toolTipTitleTV.setText(toolTip.title);
            }

            toolTipViewGroup.startAnimation(toolTip.enterAnimation);

            /* position and size calculation */
            int[] pos = new int[2];
            highlightedView.getLocationOnScreen(pos);
            int targetViewX = pos[0];
            final int targetViewY = pos[1];

            // get measured size of tooltip
            toolTipViewGroup.measure(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            int toolTipMeasuredWidth = toolTipViewGroup.getMeasuredWidth();
            int toolTipMeasuredHeight = toolTipViewGroup.getMeasuredHeight();

            Point resultPoint = new Point(); // this holds the final position of tooltip
            float density = activity.getResources().getDisplayMetrics().density;
            final float adjustment = 10 * density; //adjustment is that little overlapping area of tooltip and targeted button

            // calculate x position, based on gravity, tooltipMeasuredWidth, parent max width, x position of target view, adjustment
            if (toolTipMeasuredWidth > parent.getWidth()) {
                resultPoint.x = getXForTooTip(toolTip.gravity, parent.getWidth(), targetViewX, adjustment);
            } else {
                resultPoint.x = getXForTooTip(toolTip.gravity, toolTipMeasuredWidth, targetViewX, adjustment);
            }

            resultPoint.y = getYForTooTip(toolTip.gravity, toolTipMeasuredHeight, targetViewY, adjustment);

            // add view to parent
//            ((ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content)).addView(toolTipViewGroup, layoutParams);
            parent.addView(toolTipViewGroup, layoutParams);

            // 1. width < screen check
            if (toolTipMeasuredWidth > parent.getWidth()) {
                toolTipViewGroup.getLayoutParams().width = parent.getWidth();
                toolTipMeasuredWidth = parent.getWidth();
            }
            // 2. x left boundary check
            if (resultPoint.x < 0) {
                toolTipViewGroup.getLayoutParams().width = toolTipMeasuredWidth + resultPoint.x; //since point.x is negative, use plus
                resultPoint.x = 0;
            }
            // 3. x right boundary check
            int tempRightX = resultPoint.x + toolTipMeasuredWidth;
            if (tempRightX > parent.getWidth()) {
                toolTipViewGroup.getLayoutParams().width = parent.getWidth() - resultPoint.x; //since point.x is negative, use plus
            }

            // pass toolTip onClickListener into toolTipViewGroup
            if (toolTip.onClickListener != null) {
                toolTipViewGroup.setOnClickListener(toolTip.onClickListener);
            }

            // TODO: no boundary check for height yet, this is a unlikely case though
            // height boundary can be fixed by user changing the gravity to the other size, since there are plenty of space vertically compared to horizontally

            // this needs an viewTreeObserver, that's because TextView measurement of it's vertical height is not accurate (didn't take into account of multiple lines yet) before it's rendered
            // re-calculate height again once it's rendered
            toolTipViewGroup.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // make sure this only run once
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        //noinspection deprecation
                        toolTipViewGroup.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        toolTipViewGroup.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }

                    int fixedY;
                    int toolTipHeightAfterLayouted = toolTipViewGroup.getHeight();
                    fixedY = getYForTooTip(toolTip.gravity, toolTipHeightAfterLayouted, targetViewY, adjustment);
                    layoutParams.setMargins((int) toolTipViewGroup.getX(), fixedY, 0, 0);
                }
            });

            // set the position using setMargins on the left and top
            layoutParams.setMargins(resultPoint.x, resultPoint.y, 0, 0);
        }
    }

    private int getXForTooTip(int gravity, int toolTipMeasuredWidth, int targetViewX, float adjustment) {
        int x;

        if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
            x = targetViewX - toolTipMeasuredWidth + (int) adjustment;
        } else if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) {
            x = targetViewX + highlightedView.getWidth() - (int) adjustment;
        } else {
            x = targetViewX + highlightedView.getWidth() / 2 - toolTipMeasuredWidth / 2;
        }

        return x;
    }

    private int getYForTooTip(int gravity, int toolTipMeasuredHeight, int targetViewY, float adjustment) {
        int y;

        if ((gravity & Gravity.TOP) == Gravity.TOP) {

            if (((gravity & Gravity.LEFT) == Gravity.LEFT) || ((gravity & Gravity.RIGHT) == Gravity.RIGHT)) {
                y = targetViewY - toolTipMeasuredHeight + (int) adjustment;
            } else {
                y = targetViewY - toolTipMeasuredHeight - (int) adjustment;
            }
        } else { // this is center
            if (((gravity & Gravity.LEFT) == Gravity.LEFT) || ((gravity & Gravity.RIGHT) == Gravity.RIGHT)) {
                y = targetViewY + highlightedView.getHeight() - (int) adjustment;
            } else {
                y = targetViewY + highlightedView.getHeight() + (int) adjustment;
            }
        }

        return y;
    }

    private void setupFrameLayout() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        ViewGroup contentArea = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        int[] pos = new int[2];
        contentArea.getLocationOnScreen(pos);
        // frameLayoutWithHole's coordinates are calculated taking full screen height into account
        // but we're adding it to the content area only, so we need to offset it to the same Y value of contentArea

        layoutParams.setMargins(0, -pos[1], 0, 0);
        contentArea.addView(frameLayout, layoutParams);
    }

    private void performAnimationOn(final View view) {
        if (technique == Tutorial.HORIZONTAL_LEFT) {

            final AnimatorSet animatorSet = new AnimatorSet();
            final AnimatorSet animatorSet2 = new AnimatorSet();
            Animator.AnimatorListener lis1 = new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                    view.setTranslationX(0);
                    animatorSet2.start();
                }
            };
            Animator.AnimatorListener lis2 = new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                    view.setTranslationX(0);
                    animatorSet.start();
                }
            };

            long fadeInDuration = 800;
            long scaleDownDuration = 800;
            long goLeftXDuration = 2000;
            long fadeOutDuration = goLeftXDuration;
            float translationX = screenUtils.getWidthPx() >> 1;

            final ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeInAnim.setDuration(fadeInDuration);
            final ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f);
            scaleDownX.setDuration(scaleDownDuration);
            final ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f);
            scaleDownY.setDuration(scaleDownDuration);
            final ObjectAnimator goLeftX = ObjectAnimator.ofFloat(view, "translationX", -translationX);
            goLeftX.setDuration(goLeftXDuration);
            final ValueAnimator fadeOutAnim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOutAnim.setDuration(fadeOutDuration);

            final ValueAnimator fadeInAnim2 = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeInAnim2.setDuration(fadeInDuration);
            final ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f);
            scaleDownX2.setDuration(scaleDownDuration);
            final ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f);
            scaleDownY2.setDuration(scaleDownDuration);
            final ObjectAnimator goLeftX2 = ObjectAnimator.ofFloat(view, "translationX", -translationX);
            goLeftX2.setDuration(goLeftXDuration);
            final ValueAnimator fadeOutAnim2 = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOutAnim2.setDuration(fadeOutDuration);

            animatorSet.play(fadeInAnim);
            animatorSet.play(scaleDownX).with(scaleDownY).after(fadeInAnim);
            animatorSet.play(goLeftX).with(fadeOutAnim).after(scaleDownY);

            animatorSet2.play(fadeInAnim2);
            animatorSet2.play(scaleDownX2).with(scaleDownY2).after(fadeInAnim2);
            animatorSet2.play(goLeftX2).with(fadeOutAnim2).after(scaleDownY2);

            animatorSet.addListener(lis1);
            animatorSet2.addListener(lis2);
            animatorSet.start();

            /* these animatorSets are kept track in FrameLayout, so that they can be cleaned up when FrameLayout is detached from window */
            frameLayout.addAnimatorSet(animatorSet);
            frameLayout.addAnimatorSet(animatorSet2);
        } else {
            final AnimatorSet animatorSet = new AnimatorSet();
            final AnimatorSet animatorSet2 = new AnimatorSet();
            Animator.AnimatorListener lis1 = new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                    view.setTranslationX(0);
                    animatorSet2.start();
                }
            };
            Animator.AnimatorListener lis2 = new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                    view.setTranslationX(0);
                    animatorSet.start();
                }
            };

            long fadeInDuration = 800;
            long scaleDownDuration = 800;
            long fadeOutDuration = 800;
            long delay = 1000;

            final ValueAnimator delayAnim = ObjectAnimator.ofFloat(view, "translationX", 0);
            delayAnim.setDuration(delay);
            final ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeInAnim.setDuration(fadeInDuration);
            final ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f);
            scaleDownX.setDuration(scaleDownDuration);
            final ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f);
            scaleDownY.setDuration(scaleDownDuration);
            final ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.85f, 1f);
            scaleUpX.setDuration(scaleDownDuration);
            final ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.85f, 1f);
            scaleUpY.setDuration(scaleDownDuration);
            final ValueAnimator fadeOutAnim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOutAnim.setDuration(fadeOutDuration);

            final ValueAnimator delayAnim2 = ObjectAnimator.ofFloat(view, "translationX", 0);
            delayAnim2.setDuration(delay);
            final ValueAnimator fadeInAnim2 = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeInAnim2.setDuration(fadeInDuration);
            final ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f);
            scaleDownX2.setDuration(scaleDownDuration);
            final ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f);
            scaleDownY2.setDuration(scaleDownDuration);
            final ObjectAnimator scaleUpX2 = ObjectAnimator.ofFloat(view, "scaleX", 0.85f, 1f);
            scaleUpX2.setDuration(scaleDownDuration);
            final ObjectAnimator scaleUpY2 = ObjectAnimator.ofFloat(view, "scaleY", 0.85f, 1f);
            scaleUpY2.setDuration(scaleDownDuration);
            final ValueAnimator fadeOutAnim2 = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOutAnim2.setDuration(fadeOutDuration);
            view.setAlpha(0);
            animatorSet.setStartDelay(toolTip != null ? toolTip.enterAnimation.getDuration() : 0);
            animatorSet.play(fadeInAnim);
            animatorSet.play(scaleDownX).with(scaleDownY).after(fadeInAnim);
            animatorSet.play(scaleUpX).with(scaleUpY).with(fadeOutAnim).after(scaleDownY);
            animatorSet.play(delayAnim).after(scaleUpY);

            animatorSet2.play(fadeInAnim2);
            animatorSet2.play(scaleDownX2).with(scaleDownY2).after(fadeInAnim2);
            animatorSet2.play(scaleUpX2).with(scaleUpY2).with(fadeOutAnim2).after(scaleDownY2);
            animatorSet2.play(delayAnim2).after(scaleUpY2);

            animatorSet.addListener(lis1);
            animatorSet2.addListener(lis2);
            animatorSet.start();

            /* these animatorSets are kept track in FrameLayout, so that they can be cleaned up when FrameLayout is detached from window */
            frameLayout.addAnimatorSet(animatorSet);
            frameLayout.addAnimatorSet(animatorSet2);
        }
    }
}
