package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by tiago on 22/01/2017.
 */

public class LiveRoomView extends ViewGroup {

    @IntDef({GRID, LINEAR})
    public @interface TribeRoomViewType {}

    public static final int GRID = 0;
    public static final int LINEAR = 1;

    private static final int DEFAULT_TYPE = GRID;

    // VARIABLES
    private int fullWidth, fullHeight, partialHeight, partialWidth;
    private @TribeRoomViewType int type;

    public LiveRoomView(Context context) {
        super(context);
        type = DEFAULT_TYPE;
    }

    public LiveRoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        type = DEFAULT_TYPE;
    }

    public void setType(@TribeRoomViewType int type) {
        if (this.type == type) return;

        this.type = type;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            if (child instanceof LiveRowView) {
                LiveRowView liveRowView = (LiveRowView) child;
                liveRowView.setRoomType(type);
            }
        }

        //requestLayout();
    }

    public @TribeRoomViewType int getType() {
        return type;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childState = 0;

        final int maxWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int maxHeight = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        calculateSizes(maxWidth, maxHeight);

        if (type == GRID) {
            switch (getChildCount()) {
                case 0:
                    return;

                case 1:
                case 2:
                    measureOneOrTwoChildren(widthMeasureSpec, heightMeasureSpec);
                    break;

                case 3:
                case 4:
                    measureThreeOrFourChildren(widthMeasureSpec, heightMeasureSpec);
                    break;

                case 5:
                case 6:
                    measureFiveOrSixChildren(widthMeasureSpec, heightMeasureSpec);
                    break;

                case 7:
                case 8:
                    measureSevenOrEightChildren(widthMeasureSpec, heightMeasureSpec);
                    break;
            }
        } else {
            measureTribeChildrenLinear(widthMeasureSpec, heightMeasureSpec);
        }

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    private void calculateSizes(int width, int height) {
        if (type == GRID) {
            fullWidth = width;
            partialWidth = width >> 1;
            fullHeight = height;

            int potentialPartialHeight = 0;

            switch (getChildCount()) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    potentialPartialHeight = height >> 1;
                    break;

                case 5:
                case 6:
                    potentialPartialHeight = height / 3;
                    break;

                case 7:
                case 8:
                    potentialPartialHeight = height / 4;
                    break;
            }

            partialHeight = potentialPartialHeight;
        } else {
            fullWidth = partialWidth = width;
            fullHeight = height;
            partialHeight = height / getChildCount();
        }
    }

    private int measureOneOrTwoChildren(int widthMeasureSpec, int heightMeasureSpec) {
        int childState = 0;

        combineMeasuredStates(childState, measureTribeChild(getChildAt(0),
                widthMeasureSpec, heightMeasureSpec, fullWidth, partialHeight));

        if (getChildCount() == 2) {
            combineMeasuredStates(childState, measureTribeChild(getChildAt(1),
                    widthMeasureSpec, heightMeasureSpec, fullWidth, partialHeight));
        }

        return childState;
    }

    private int measureThreeOrFourChildren(int widthMeasureSpec, int heightMeasureSpec) {
        return measureThreeOrMoreChildren(widthMeasureSpec, heightMeasureSpec, getChildCount() == 3 ? 0 : -1);
    }

    private int measureFiveOrSixChildren(int widthMeasureSpec, int heightMeasureSpec) {
        return measureThreeOrMoreChildren(widthMeasureSpec, heightMeasureSpec, getChildCount() == 5 ? 4 : -1);
    }

    private int measureSevenOrEightChildren(int widthMeasureSpec, int heightMeasureSpec) {
        return measureThreeOrMoreChildren(widthMeasureSpec, heightMeasureSpec, getChildCount() == 7 ? 6 : -1);
    }

    private int measureThreeOrMoreChildren(int widthMeasureSpec, int heightMeasureSpec, int fullIndex) {
        int childState = 0;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (fullIndex != -1 && i == fullIndex) measureTribeChild(child, widthMeasureSpec, heightMeasureSpec, fullWidth, partialHeight);
            else measureTribeChild(child, widthMeasureSpec, heightMeasureSpec, partialWidth, partialHeight);
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }

        return childState;
    }

    private int measureTribeChild(View child, int widthMeasureSpec, int heightMeasureSpec,
                                   int desiredWidth, int desiredHeight) {
        if (child.getVisibility() != GONE) {
            child.measure(
                    getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight(),
                            desiredWidth),
                    getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(),
                            desiredHeight));
        }

        return child.getMeasuredState();
    }

    private int measureTribeChildrenLinear(int widthMeasureSpec, int heightMeasureSpec) {
        int childState = 0;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            if (child.getVisibility() != GONE) {
                child.measure(
                        getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight(),
                                fullWidth),
                        getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(),
                                partialHeight));
            }

            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }

        return childState;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() > 0) {
            if (type == GRID) layoutChildrenGrid();
            else layoutChildrenLinear();
        }
    }

    private void layoutChildrenGrid() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int childLeft, childTop;

            if (i == 2 || i == 3 || i == 5 || i == 7) childLeft = (getPaddingLeft() + partialWidth);
            else childLeft = getPaddingLeft();

            if (i == 0 || i == 3) childTop = (getPaddingTop() + partialHeight);
            else if (i == 4 || i == 5) childTop = (getPaddingTop() + partialHeight * 2);
            else if (i == 6 || i == 7) childTop = (getPaddingTop() + partialHeight * 3);
            else childTop = getPaddingTop();

            child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
        }
    }

    private void layoutChildrenLinear() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int childLeft = getPaddingLeft();
            int childTop;

            if (i == 0) childTop = (getPaddingTop() + (getChildCount() >= 2 ? partialHeight : 0));
            else if (i == 1) childTop = (getPaddingTop());
            else childTop = (getPaddingTop() + partialHeight * i);
            child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
        }
    }
}
