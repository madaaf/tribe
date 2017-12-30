package com.tribe.app.presentation.view.widget.chat.model;

/**
 * Created by madaaflak on 07/09/2017.
 */

public class Media {

  private String url;
  private Integer filesize;
  private String width;
  private String height;
  private float duration;

  public Media() {
  }

  public float getDuration() {
    return duration;
  }

  public String getDurationFormatted() {
    Double time = (double) (duration / 100);
    int size = (String.valueOf(time).length() > 4) ? 4 : String.valueOf(time).length();
    String value = String.valueOf(Double.parseDouble(Double.toString(time).substring(0, size)));
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
