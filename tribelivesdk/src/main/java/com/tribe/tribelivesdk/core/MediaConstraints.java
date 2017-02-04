package com.tribe.tribelivesdk.core;

import com.tribe.tribelivesdk.util.LogUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.webrtc.CameraEnumerationAndroid;

/**
 * Created by tiago on 13/01/2017.
 */

public class MediaConstraints {

  public static final String VIDEO_CODEC_H264 = "H264";
  public static final String VIDEO_CODEC_VP9 = "VP9";

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

    public MediaConstraintsBuilder() {
    }

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

  public static String preferVideoCodec(String sdpDescription, String codec) {
    String[] lines = sdpDescription.split("\r\n");
    int mLineIndex = -1;
    String codecRtpMap = null;
    // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
    String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
    Pattern codecPattern = Pattern.compile(regex);
    String mediaDescription = "m=video ";

    for (int i = 0; (i < lines.length) && (mLineIndex == -1 || codecRtpMap == null); i++) {
      if (lines[i].startsWith(mediaDescription)) {
        mLineIndex = i;
        continue;
      }

      Matcher codecMatcher = codecPattern.matcher(lines[i]);

      if (codecMatcher.matches()) {
        codecRtpMap = codecMatcher.group(1);
      }
    }

    if (mLineIndex == -1) {
      LogUtil.d(MediaConstraints.class, "No " + mediaDescription + " line, so can't prefer " + codec);
      return sdpDescription;
    }

    if (codecRtpMap == null) {
      LogUtil.d(MediaConstraints.class, "No rtpmap for " + codec);
      return sdpDescription;
    }

    LogUtil.d(MediaConstraints.class,
        "Found " + codec + " rtpmap " + codecRtpMap + ", prefer at " + lines[mLineIndex]);

    String[] origMLineParts = lines[mLineIndex].split(" ");

    if (origMLineParts.length > 3) {
      StringBuilder newMLine = new StringBuilder();
      int origPartIndex = 0;
      // Format is: m=<media> <port> <proto> <fmt> ...
      newMLine.append(origMLineParts[origPartIndex++]).append(" ");
      newMLine.append(origMLineParts[origPartIndex++]).append(" ");
      newMLine.append(origMLineParts[origPartIndex++]).append(" ");
      newMLine.append(codecRtpMap);

      for (; origPartIndex < origMLineParts.length; origPartIndex++) {
        if (!origMLineParts[origPartIndex].equals(codecRtpMap)) {
          newMLine.append(" ").append(origMLineParts[origPartIndex]);
        }
      }

      lines[mLineIndex] = newMLine.toString();
      LogUtil.d(MediaConstraints.class, "Change media description: " + lines[mLineIndex]);
    } else {
      LogUtil.e(MediaConstraints.class, "Wrong SDP media description format: " + lines[mLineIndex]);
    }

    StringBuilder newSdpDescription = new StringBuilder();
    for (String line : lines) {
      newSdpDescription.append(line).append("\r\n");
    }

    return newSdpDescription.toString();
  }

  public static String removeVideoCodec(String sdpDescription, String codec) {
    String[] lines = sdpDescription.split("\r\n");
    int mLineIndex = -1;
    int mLineToRemove = -1;
    String codecRtpMap = null;
    // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
    String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
    Pattern codecPattern = Pattern.compile(regex);
    String mediaDescription = "m=video ";

    for (int i = 0; (i < lines.length) && (mLineIndex == -1 || codecRtpMap == null); i++) {
      if (lines[i].startsWith(mediaDescription)) {
        mLineIndex = i;
        continue;
      }

      Matcher codecMatcher = codecPattern.matcher(lines[i]);

      if (codecMatcher.matches()) {
        mLineToRemove = i;
        codecRtpMap = codecMatcher.group(1);
      }
    }

    if (mLineIndex == -1) {
      LogUtil.d(MediaConstraints.class, "No " + mediaDescription + " line, so can't prefer " + codec);
      return sdpDescription;
    }

    if (codecRtpMap == null || mLineToRemove == -1) {
      LogUtil.d(MediaConstraints.class, "No rtpmap for " + codec);
      return sdpDescription;
    }

    LogUtil.d(MediaConstraints.class,
        "Found " + codec + " rtpmap " + codecRtpMap + ", prefer at " + lines[mLineIndex]);

    String[] origMLineParts = lines[mLineIndex].split(" ");

    if (origMLineParts.length > 3) {
      StringBuilder newMLine = new StringBuilder();
      int origPartIndex = 0;
      // Format is: m=<media> <port> <proto> <fmt> ...
      newMLine.append(origMLineParts[origPartIndex++]).append(" ");
      newMLine.append(origMLineParts[origPartIndex++]).append(" ");
      newMLine.append(origMLineParts[origPartIndex++]);

      for (; origPartIndex < origMLineParts.length; origPartIndex++) {
        if (!origMLineParts[origPartIndex].equals(codecRtpMap)) {
          newMLine.append(" ").append(origMLineParts[origPartIndex]);
        }
      }

      lines[mLineIndex] = newMLine.toString();
      LogUtil.d(MediaConstraints.class, "Change media description: " + lines[mLineIndex]);
    } else {
      LogUtil.e(MediaConstraints.class, "Wrong SDP media description format: " + lines[mLineIndex]);
    }

    StringBuilder newSdpDescription = new StringBuilder();
    int count = 0;
    for (String line : lines) {
      if (count != mLineToRemove) {
        newSdpDescription.append(line).append("\r\n");
      }
      count++;
    }

    return sdpDescription;
    //return newSdpDescription.toString(); TODO handle remove of the codec
  }
}
