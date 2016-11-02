package com.tribe.app.presentation.view.tutorial;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;

public class ToolTip {

    @IntDef({RIGHT_UPWARD, RIGHT_DOWNWARD, CENTER_DOWNWARD, NO_IND})
    public @interface Style{}

    public static final int RIGHT_UPWARD = 0;
    public static final int RIGHT_DOWNWARD = 1;
    public static final int CENTER_DOWNWARD = 2;
    public static final int NO_IND = 3;

    public String title;
    public int backgroundRes;
    public View.OnClickListener onClickListener;
    public Animation enterAnimation, exitAnimation;
    public int gravity;
    public int textColor;

    public ToolTip(Context context) {
        title = "";
        backgroundRes = Color.WHITE;
        textColor = Color.BLACK;

        enterAnimation = new AlphaAnimation(0f, 1f);
        enterAnimation.setDuration(1000);
        enterAnimation.setFillAfter(true);
        enterAnimation.setInterpolator(new BounceInterpolator());

        gravity = Gravity.CENTER;
    }

    public ToolTip setTitle(String title) {
        this.title = title;
        return this;
    }

    public ToolTip setBackgroundRes(int backgroundRes) {
        this.backgroundRes = backgroundRes;
        return this;
    }

    public ToolTip setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public ToolTip setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public ToolTip setEnterAnimation(Animation enterAnimation) {
        this.enterAnimation = enterAnimation;
        return this;
    }

    public ToolTip setGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }
}
