package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;

import com.tribe.app.R;

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

    private static int selectedPalette = 1;

    @Inject
    public PaletteGrid(Context context) {
        paletteOne = new Integer[] {
                ContextCompat.getColor(context, R.color.palette_one_one),
                ContextCompat.getColor(context, R.color.palette_one_two),
                ContextCompat.getColor(context, R.color.palette_one_three),
                ContextCompat.getColor(context, R.color.palette_one_four),
                ContextCompat.getColor(context, R.color.palette_one_five),
                ContextCompat.getColor(context, R.color.palette_one_six),
                ContextCompat.getColor(context, R.color.palette_one_seven)
        };

        paletteTwo = new Integer[] {
                ContextCompat.getColor(context, R.color.palette_two_one),
                ContextCompat.getColor(context, R.color.palette_two_two),
                ContextCompat.getColor(context, R.color.palette_two_three),
                ContextCompat.getColor(context, R.color.palette_two_four)
        };
    }

    public static int get(int position) {
        if (selectedPalette == 0)
            return paletteOne[position % paletteOne.length];
        if (selectedPalette == 1)
            return paletteTwo[position % paletteTwo.length];

        return Color.BLACK;
    }
}
