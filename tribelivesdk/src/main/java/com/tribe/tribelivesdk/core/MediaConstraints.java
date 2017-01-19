package com.tribe.tribelivesdk.core;

import org.webrtc.CameraEnumerationAndroid;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 13/01/2017.
 */

public class MediaConstraints {

    private Boolean hasVideo;
    private Boolean hasAudio;
    private int maxWidth;
    private int minWidth;
    private int maxHeight;
    private int minHeight;
    private int maxFrameRate;
    private int minFrameRate;
    private List<CameraEnumerationAndroid.CaptureFormat> captureFormatList;

    private MediaConstraints(MediaConstraintsBuilder builder) {
        this.hasVideo = builder.hasVideo;
        this.hasAudio = builder.hasAudio;
        this.maxWidth = builder.maxWidth;
        this.minWidth = builder.minWidth;
        this.maxHeight = builder.maxHeight;
        this.minHeight = builder.minHeight;
        this.maxFrameRate = builder.maxFrameRate;
        this.minFrameRate = builder.minFrameRate;
        this.captureFormatList = new ArrayList<>();
    }

    public static class MediaConstraintsBuilder {

        private Boolean hasVideo = true;
        private Boolean hasAudio = true;
        private int maxWidth = 640;
        private int minWidth = 0;
        private int maxHeight = 480;
        private int minHeight = 0;
        private int maxFrameRate = 30;
        private int minFrameRate = 0;

        public MediaConstraintsBuilder() { }

        public MediaConstraintsBuilder hasVideo(boolean bool) {
            this.hasVideo = bool;
            return this;
        }

        public MediaConstraintsBuilder hasAudio(boolean bool) {
            this.hasAudio = bool;
            return this;
        }

        public MediaConstraintsBuilder maxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        public MediaConstraintsBuilder maxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }

        public MediaConstraintsBuilder minHeight(int minHeight) {
            this.minHeight = minHeight;
            return this;
        }

        public MediaConstraintsBuilder maxFrameRate(int maxFrameRate) {
            this.maxFrameRate = maxFrameRate;
            return this;
        }

        public MediaConstraintsBuilder minFrameRate(int minFrameRate) {
            this.minFrameRate = minFrameRate;
            return this;
        }

        public MediaConstraints build() {
            return new MediaConstraints(this);
        }
    }

    public Boolean getHasVideo() {
        return hasVideo;
    }

    public void setHasVideo(Boolean hasVideo) {
        this.hasVideo = hasVideo;
    }

    public Boolean getHasAudio() {
        return hasAudio;
    }

    public void setHasAudio(Boolean hasAudio) {
        this.hasAudio = hasAudio;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    public int getMaxFrameRate() {
        return maxFrameRate;
    }

    public void setMaxFrameRate(int maxFrameRate) {
        this.maxFrameRate = maxFrameRate;
    }

    public int getMinFrameRate() {
        return minFrameRate;
    }

    public void setMinFrameRate(int minFrameRate) {
        this.minFrameRate = minFrameRate;
    }
}
