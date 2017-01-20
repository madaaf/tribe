package com.tribe.app.presentation.view.utils;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.CardView;
import android.view.View;

/**
 * Created by tiago on 14/11/2016.
 */

public class UIUtils {

    public static void setBackgroundGrid(ScreenUtils screenUtils, View v, int position, boolean hasCorners) {
        Drawable background = v.getBackground();
        int color = PaletteGrid.get(position);
        int radiusTopLeft = position == 1 ? screenUtils.dpToPx(5) : 0;
        int radiusTopRight = position == 2 ? screenUtils.dpToPx(5) : 0;
        float [] radiusMatrix = new float[] { radiusTopLeft, radiusTopLeft, radiusTopRight, radiusTopRight, 0, 0, 0, 0 };

        if (background == null) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            gradientDrawable.setColor(color);
            if (hasCorners) gradientDrawable.setCornerRadii(radiusMatrix);
            v.setBackground(gradientDrawable);
        }

        if (background instanceof GradientDrawable) {
            GradientDrawable gradientDrawable = (GradientDrawable) background;
            gradientDrawable.setColor(color);
            if (hasCorners) gradientDrawable.setCornerRadii(radiusMatrix);
        }
    }

    public static void setBackgroundCard(CardView v, int position) {
        int color = PaletteGrid.get(position);
        v.setCardBackgroundColor(color);
    }
}
