package com.tribe.app.presentation.view.widget.chat.model;

/**
 * Created by madaaflak on 07/09/2017.
 */

public class Image {

  private String url;
  private Integer filesize;
  private String width;
  private String height;
  private float duration;

  public Image() {
  }

  public float getDuration() {
    return duration;
  }

  public String getDurationFormatted() {
    int size = (String.valueOf(duration).length() > 4) ? 4 : String.valueOf(duration).length();
    String value =
        String.valueOf(Float.parseFloat(Float.toString(duration / 100).substring(0, size)));
    return value.replace(".", ":");
  }

  public int getDurationMs() {
    return (int) (duration * 1000);
  }

  public void setDuration(float duration) {
    this.duration = duration;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Integer getFilesize() {
    return filesize;
  }

  public void setFilesize(Integer filesize) {
    this.filesize = filesize;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getHeight() {
    return height;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  @Override public String toString() {
    return "Original{"
        + "url='"
        + url
        + '\''
        + ", filesize='"
        + filesize
        + '\''
        + ", width='"
        + width
        + '\''
        + ", height='"
        + height
        + '\''
        + '}';
  }
}
