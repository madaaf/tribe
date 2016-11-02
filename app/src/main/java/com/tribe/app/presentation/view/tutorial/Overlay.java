package com.tribe.app.presentation.view.tutorial;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.Animation;

import com.tribe.app.R;

public class Overlay {

    public final static int NOT_SET = -1;

    @IntDef({CIRCLE, RECTANGLE})
    public @interface Style{}

    public static final int CIRCLE = 0;
    public static final int RECTANGLE = 1;

    public int backgroundColor;
    public boolean disableClick;
    public boolean disableClickThroughHole;
    public View.OnClickListener onClickListener;
    public @Style int style;
    public Animation enterAnimation, exitAnimation;
    public int holeOffsetLeft = 0;
    public int holeOffsetTop = 0;
    public int holeCornerRadius = 0;
    public int holePadding = 0;
    public int holeRadius = NOT_SET;
    public Bitmap imageOverlay;

    public Overlay(Context context) {
        this(true, ContextCompat.getColor(context, R.color.black_opacity_50), CIRCLE);
    }

    public Overlay(boolean disableClick, int backgroundColor, @Style int style) {
        this.disableClick = disableClick;
        this.backgroundColor = backgroundColor;
        this.style = style;
    }

    public Overlay setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public Overlay disableClick(boolean yesNo) {
        this.disableClick = yesNo;
        return this;
    }

    public Overlay disableClickThroughHole(boolean yesNo) {
        disableClickThroughHole = yesNo;
        return this;
    }

    public Overlay setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public Overlay setStyle(@Style int style) {
        this.style = style;
        return this;
    }

    public Overlay setEnterAnimation(Animation enterAnimation) {
        this.enterAnimation = enterAnimation;
        return this;
    }

    public Overlay setExitAnimation(Animation exitAnimation) {
        this.exitAnimation = exitAnimation;
        return this;
    }

    public Overlay setImageOverlay(Bitmap bitmap) {
        this.imageOverlay = bitmap;
        return this;
    }

    public Overlay setHoleRadius(int holeRadius) {
        this.holeRadius = holeRadius;
        return this;
    }

    public Overlay setHoleOffsets(int offsetLeft, int offsetTop) {
        holeOffsetLeft = offsetLeft;
        holeOffsetTop = offsetTop;
        return this;
    }

    public Overlay setHoleCornerRadius(int cornerRadius) {
        holeCornerRadius = cornerRadius;
        return this;
    }

    public Overlay setHolePadding(int padding) {
        holePadding = padding;
        return this;
    }
}
