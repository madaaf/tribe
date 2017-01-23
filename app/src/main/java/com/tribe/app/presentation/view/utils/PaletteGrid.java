package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.preferences.Theme;

import java.util.Random;

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
    private static Integer[] paletteFive;
    private static Integer[] paletteSix;

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
                ContextCompat.getColor(context, R.color.classic_8)
        };

        paletteTwo = new Integer[] {
                ContextCompat.getColor(context, R.color.blue_1),
                ContextCompat.getColor(context, R.color.blue_2),
                ContextCompat.getColor(context, R.color.blue_3),
                ContextCompat.getColor(context, R.color.blue_4),
                ContextCompat.getColor(context, R.color.blue_5),
                ContextCompat.getColor(context, R.color.blue_6),
                ContextCompat.getColor(context, R.color.blue_7),
                ContextCompat.getColor(context, R.color.blue_8),
                ContextCompat.getColor(context, R.color.blue_9),
                ContextCompat.getColor(context, R.color.blue_10),
                ContextCompat.getColor(context, R.color.blue_11),
                ContextCompat.getColor(context, R.color.blue_12)
        };

        paletteThree = new Integer[] {
                ContextCompat.getColor(context, R.color.red_1),
                ContextCompat.getColor(context, R.color.red_2),
                ContextCompat.getColor(context, R.color.red_3),
                ContextCompat.getColor(context, R.color.red_4),
                ContextCompat.getColor(context, R.color.red_5),
                ContextCompat.getColor(context, R.color.red_6),
                ContextCompat.getColor(context, R.color.red_7),
                ContextCompat.getColor(context, R.color.red_8),
                ContextCompat.getColor(context, R.color.red_9),
                ContextCompat.getColor(context, R.color.red_10),
                ContextCompat.getColor(context, R.color.red_11),
                ContextCompat.getColor(context, R.color.red_12)
        };

        paletteFour = new Integer[] {
                ContextCompat.getColor(context, R.color.violet_1),
                ContextCompat.getColor(context, R.color.violet_2),
                ContextCompat.getColor(context, R.color.violet_3),
                ContextCompat.getColor(context, R.color.violet_4),
                ContextCompat.getColor(context, R.color.violet_5),
                ContextCompat.getColor(context, R.color.violet_6),
                ContextCompat.getColor(context, R.color.violet_7),
                ContextCompat.getColor(context, R.color.violet_8),
                ContextCompat.getColor(context, R.color.violet_9),
                ContextCompat.getColor(context, R.color.violet_10),
                ContextCompat.getColor(context, R.color.violet_11),
                ContextCompat.getColor(context, R.color.violet_12)
        };

        paletteFive = new Integer[] {
                ContextCompat.getColor(context, R.color.orange_1),
                ContextCompat.getColor(context, R.color.orange_2),
                ContextCompat.getColor(context, R.color.orange_3),
                ContextCompat.getColor(context, R.color.orange_4),
                ContextCompat.getColor(context, R.color.orange_5),
                ContextCompat.getColor(context, R.color.orange_6),
                ContextCompat.getColor(context, R.color.orange_7),
                ContextCompat.getColor(context, R.color.orange_8),
                ContextCompat.getColor(context, R.color.orange_9),
                ContextCompat.getColor(context, R.color.orange_10),
                ContextCompat.getColor(context, R.color.orange_11),
                ContextCompat.getColor(context, R.color.orange_12)
        };

        paletteSix = new Integer[] {
                ContextCompat.getColor(context, R.color.black_1),
                ContextCompat.getColor(context, R.color.black_2),
                ContextCompat.getColor(context, R.color.black_3),
                ContextCompat.getColor(context, R.color.black_4),
                ContextCompat.getColor(context, R.color.black_5),
                ContextCompat.getColor(context, R.color.black_6),
                ContextCompat.getColor(context, R.color.black_7),
                ContextCompat.getColor(context, R.color.black_8),
                ContextCompat.getColor(context, R.color.black_9),
                ContextCompat.getColor(context, R.color.black_10),
                ContextCompat.getColor(context, R.color.black_11),
                ContextCompat.getColor(context, R.color.black_12)
        };
    }

    public static int get(int position) {
        if (theme.get() == 0)
            return paletteOne[position % paletteOne.length];
        else if (theme.get() == 1)
            return paletteTwo[position % paletteTwo.length];
        else if (theme.get() == 2)
            return paletteThree[position % paletteThree.length];
        else if (theme.get() == 3)
            return paletteFour[position % paletteFour.length];
        else if (theme.get() == 4)
            return paletteFive[position % paletteFive.length];
        else if (theme.get() == 5)
            return paletteSix[position % paletteSix.length];

        return Color.BLACK;
    }

    public static int getLength() {
        if (theme.get() == 0)
            return paletteOne.length;
        else if (theme.get() == 1)
            return paletteTwo.length;
        else if (theme.get() == 2)
            return paletteThree.length;
        else if (theme.get() == 3)
            return paletteFour.length;
        else if (theme.get() == 4)
            return paletteFive.length;
        else if (theme.get() == 5)
            return paletteSix.length;

        return Color.BLACK;
    }

    public static int getRandomColorExcluding(int excludeColor) {
        Random r = new Random();
        int randomPosition = 1;

        int color = excludeColor;
        while (color == excludeColor) {
            color = get(randomPosition);
            randomPosition = r.nextInt(getLength() - 0) + 0;
        }

        return color;
    }
}
