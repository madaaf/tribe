package com.tribe.app.domain.entity;

import android.widget.ImageView;

import java.io.Serializable;

/**
 * Created by tiago on 15/08/2016.
 */
public class Section implements Serializable {

    private int begin;
    private int end;
    private ImageView imageView;

    public Section(int begin, int end, ImageView imageView) {
        this.begin = begin;
        this.end = end;
        this.imageView = imageView;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }
}
