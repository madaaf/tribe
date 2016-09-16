package com.tribe.app.domain.entity;

import android.support.annotation.StringDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by tiago on 15/09/2016.
 */
public class PTSEntity implements Serializable {

    @StringDef({ICON, ALPHABET})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PTSType {
    }

    public static final String ICON = "icon";
    public static final String ALPHABET = "alphabet";

    private @PTSType String type;
    private boolean activated;
    private String letter;
    private int drawable;

    public PTSEntity(@PTSType String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }
}
