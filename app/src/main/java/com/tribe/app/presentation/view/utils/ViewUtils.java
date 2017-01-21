package com.tribe.app.presentation.view.utils;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by tiago on 20/01/2017.
 */

public class ViewUtils {

    public static View findViewAt(ViewGroup viewGroup, Class clazz, int x, int y) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                if (clazz.isAssignableFrom(child.getClass())) {
                    if (isIn(child, x, y)) return child;
                } else {
                    View foundView = findViewAt((ViewGroup) child, clazz, x, y);
                    if (foundView != null && foundView.isShown()) {
                        return foundView;
                    }
                }
            } else {
                if (isIn(child, x, y)) return child;
            }
        }

        return null;
    }

    private static boolean isIn(View child, int x, int y) {
        int[] location = new int[2];
        child.getLocationOnScreen(location);
        Rect rect = new Rect(location[0], location[1], location[0] + child.getWidth(), location[1] + child.getHeight());
        return rect.contains(x, y);
    }
}
