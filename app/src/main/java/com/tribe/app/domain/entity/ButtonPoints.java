package com.tribe.app.domain.entity;

import com.tribe.app.presentation.view.widget.ButtonPointsView;

import java.io.Serializable;

/**
 * Created by tiago on 02/09/2016.
 */
public class ButtonPoints implements Serializable {

    private int type;
    private int drawable;
    private int label;
    private int subLabel;
    private int points;
    private String urlImg;
    private boolean animate = false;

    public ButtonPoints(@ButtonPointsView.ButtonType int type,
                        int label,
                        int subLabel, int points) {
        this.type = type;
        this.label = label;
        this.subLabel = subLabel;
        this.points = points;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public int getSubLabel() {
        return subLabel;
    }

    public void setSubLabel(int subLabel) {
        this.subLabel = subLabel;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setUrlImg(String urlImg) {
        this.urlImg = urlImg;
    }

    public String getUrlImg() {
        return urlImg;
    }

    public boolean isAnimate() {
        return animate;
    }

    public void setAnimate(boolean animate) {
        this.animate = animate;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + type;
        return result;
    }
}
