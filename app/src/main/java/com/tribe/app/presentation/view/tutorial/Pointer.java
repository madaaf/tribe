package com.tribe.app.presentation.view.tutorial;

import android.graphics.Color;
import android.view.Gravity;

/**
 * Created by tanjunrong on 6/20/15.
 */
public class Pointer {

    public int gravity = Gravity.CENTER;
    public int color = Color.WHITE;

    public Pointer() {
        this(Gravity.CENTER, Color.WHITE);
    }

    public Pointer(int gravity, int color) {
        this.gravity = gravity;
        this.color = color;
    }

    public Pointer setColor(int color) {
        this.color = color;
        return this;
    }

    public Pointer setGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }
}
