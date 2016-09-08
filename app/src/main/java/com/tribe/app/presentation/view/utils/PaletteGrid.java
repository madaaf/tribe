package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;

import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.scope.Theme;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 18/05/2016.
 */
@Singleton
public class PaletteGrid {

    private static Integer[] paletteOne;
    private static Integer[] paletteTwo;
    private static Integer[] paletteThree;
    private static Integer[] paletteFour;

    private static Preference<Integer> theme;

    @Inject
    public PaletteGrid(Context context, @Theme Preference<Integer> theme) {

        this.theme = theme;

        paletteOne = new Integer[] {
                ContextCompat.getColor(context, R.color.classic_1),
                ContextCompat.getColor(context, R.color.classic_2),
                ContextCompat.getColor(context, R.color.classic_3),
                ContextCompat.getColor(context, R.color.classic_4),
                ContextCompat.getColor(context, R.color.classic_5),
                ContextCompat.getColor(context, R.color.classic_6),
                ContextCompat.getColor(context, R.color.classic_7),
                ContextCompat.getColor(context, R.color.classic_8),
                ContextCompat.getColor(context, R.color.classic_9),
                ContextCompat.getColor(context, R.color.classic_10),
                ContextCompat.getColor(context, R.color.classic_11),
                ContextCompat.getColor(context, R.color.classic_12)
        };

        paletteTwo = new Integer[] {
                ContextCompat.getColor(context, R.color.heighties_1),
                ContextCompat.getColor(context, R.color.heighties_2),
                ContextCompat.getColor(context, R.color.heighties_3),
                ContextCompat.getColor(context, R.color.heighties_4)
        };
    }

    public static int get(int position) {
        if (theme.get() == 0)
            return paletteOne[position % paletteOne.length];
        if (theme.get() == 1)
            return paletteTwo[position % paletteTwo.length];

        return Color.BLACK;
    }
}
