package com.tribe.app.presentation.view.tutorial;

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
import android.view.animation.Animation;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.ButterKnife;


/**
 * created by tiago on 11/01/2016 based on : https://github.com/worker8/TourGuide
 */
public class Tutorial {

    @Inject
    TutorialManager tutorialManager;

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

    private
    @TutorialManager.TutorialKey
    String key;
    private
    @Technique
    int technique;
    private View highlightedView;
    private Activity activity;
    private ScreenUtils screenUtils;
    private
    @MotionType
    int motionType;
    private FrameLayoutWithHole frameLayout;
    private View toolTipViewGroup;
    private ToolTip toolTip;
    private Overlay overlay;

    public static Tutorial init(Activity activity, ScreenUtils screenUtils, @TutorialManager.TutorialKey String key) {
        return new Tutorial(activity, screenUtils, key);
    }

    public Tutorial(Activity activity, ScreenUtils screenUtils, @TutorialManager.TutorialKey String key) {
        this.activity = activity;
        this.screenUtils = screenUtils;
        this.key = key;

        ((AndroidApplication) activity.getApplicationContext()).getApplicationComponent().inject(this);
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

    public Tutorial setToolTip(ToolTip toolTip) {
        this.toolTip = toolTip;
        return this;
    }

    public void cleanUp() {
        tutorialManager.addTutorialKey(key);

        if (toolTip.exitAnimation != null) {
            Animation animation = toolTip.exitAnimation;
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    cleanUpRest();
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            toolTipViewGroup.startAnimation(toolTip.exitAnimation);
        } else {
            cleanUpRest();
        }
    }

    private void cleanUpRest() {
        if (toolTipViewGroup != null) {
            ((ViewGroup) activity.getWindow().getDecorView()).removeView(toolTipViewGroup);
        }

        frameLayout.cleanUp();
    }

    public FrameLayoutWithHole getOverlay() {
        return frameLayout;
    }

    public View getToolTip() {
        return toolTipViewGroup;
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
                resultPoint.x = getXForTooTip(toolTip.gravity, parent.getWidth(), targetViewX, adjustment + toolTip.offsetX);
            } else {
                resultPoint.x = getXForTooTip(toolTip.gravity, toolTipMeasuredWidth, targetViewX, adjustment + toolTip.offsetX);
            }

            resultPoint.y = getYForTooTip(toolTip.gravity, toolTipMeasuredHeight, targetViewY, adjustment + toolTip.offsetY);

            // add view to parent
            // ((ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content)).addView(toolTipViewGroup, layoutParams);
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
}
