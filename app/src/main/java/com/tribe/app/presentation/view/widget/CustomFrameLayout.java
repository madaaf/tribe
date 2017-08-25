package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by remy on 09/08/2017.
 */

public class CustomFrameLayout extends FrameLayout {

    private boolean isFirstLayout = true;

    public CustomFrameLayout(Context context) {
        super(context);
    }

    public CustomFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (isFirstLayout) {
            isFirstLayout = false;
            onFirstLayout();
        }
    }

    protected void onFirstLayout() {

    }
}
