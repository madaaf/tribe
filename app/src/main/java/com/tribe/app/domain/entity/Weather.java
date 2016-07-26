package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by tiago on 06/05/2016.
 */
public class Weather implements Serializable {

    private int temp_f;
    private int temp_c;
    private String icon;

    public Weather() {

    }

    public int getTempF() {
        return temp_f;
    }

    public void setTempF(int temp_f) {
        this.temp_f = temp_f;
    }

    public int getTempC() {
        return temp_c;
    }

    public void setTempC(int temp_c) {
        this.temp_c = temp_c;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
