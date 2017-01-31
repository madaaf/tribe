package com.tribe.app.presentation.view.utils;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by tiago on 14/11/2016.
 */

public class UIUtils {

    public static void setBackgroundGrid(ScreenUtils screenUtils, View v, int position, boolean hasCorners) {
        Drawable background = v.getBackground();
        int color = PaletteGrid.get(position);
        int radiusTopLeft = position == 1 ? screenUtils.dpToPx(5) : 0;
        int radiusTopRight = position == 2 ? screenUtils.dpToPx(5) : 0;
        float[] radiusMatrix = new float[]{radiusTopLeft, radiusTopLeft, radiusTopRight, radiusTopRight, 0, 0, 0, 0};

        if (background == null) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            gradientDrawable.setColor(color);
            if (hasCorners) gradientDrawable.setCornerRadii(radiusMatrix);
            else gradientDrawable.setCornerRadius(0);
            v.setBackground(gradientDrawable);
        } else if (background instanceof GradientDrawable) {
            GradientDrawable gradientDrawable = (GradientDrawable) background;
            gradientDrawable.setColor(color);
            if (hasCorners) gradientDrawable.setCornerRadii(radiusMatrix);
            else gradientDrawable.setCornerRadius(0);
        }
    }

    public static void setBackgroundMultiple(ScreenUtils screenUtils, View v, int position) {
        Drawable background = v.getBackground();
        int color = PaletteGrid.get(position);
        int radius = screenUtils.dpToPx(5);
        float[] radiusMatrix = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        if (background == null) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            gradientDrawable.setColor(color);
            gradientDrawable.setCornerRadii(radiusMatrix);
            v.setBackground(gradientDrawable);
        } else if (background instanceof GradientDrawable) {
            GradientDrawable gradientDrawable = (GradientDrawable) background;
            gradientDrawable.setColor(color);
            gradientDrawable.setCornerRadii(radiusMatrix);
        }
    }

    public static void setBackgroundCard(CardView v, int position) {
        int color = PaletteGrid.get(position);
        v.setCardBackgroundColor(color);
    }

    public static void setBackgroundInd(View v, int position) {
        int color = PaletteGrid.get(position);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setColor(color);
        v.setBackground(gradientDrawable);
    }

    public static void changeSizeOfView(View v, int size) {
        ViewGroup.LayoutParams params = v.getLayoutParams();
        params.width = params.height = size;
        v.setLayoutParams(params);
    }

    public static void changeHeightOfView(View v, int height) {
        ViewGroup.LayoutParams params = v.getLayoutParams();
        params.height = height;
        v.setLayoutParams(params);
    }

    public static void changeWidthOfView(View v, int width) {
        ViewGroup.LayoutParams params = v.getLayoutParams();
        params.width = width;
        v.setLayoutParams(params);
    }
}
