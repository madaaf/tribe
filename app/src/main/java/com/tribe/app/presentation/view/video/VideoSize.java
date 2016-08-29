package com.tribe.app.presentation.view.video;

/**
 * Created by tiago on 28/08/2016.
 */
public class VideoSize {

    private int width;
    private int height;

    public VideoSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
