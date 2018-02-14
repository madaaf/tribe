package com.tribe.app.presentation.view;

/**
 * Created by madaaflak on 09/02/2018.
 */

public class NotificationModel {
  private String title;
  private String subTitle;
  private String btn1Content;
  private Integer drawableBtn1;
  private String content;
  private String profilePicture;
  private Integer background;

  private NotificationModel(Builder b) {
    this.title = b.title;
    this.subTitle = b.subTitle;
    this.btn1Content = b.btn1Content;
    this.content = b.content;
    this.background = b.background;
    this.profilePicture = b.profilePicture;
    this.drawableBtn1 = b.drawableBtn1;
  }

  public String getTitle() {
    return title;
  }

  public String getSubTitle() {
    return subTitle;
  }

  public String getContent() {
    return content;
  }

  public String getBtn1Content() {
    return btn1Content;
  }

  public Integer getDrawableBtn1() {
    return drawableBtn1;
  }

  public String getProfilePicture() {
    return profilePicture;
  }

  public Integer getBackground() {
    return background;
  }

  public static class Builder {
    private String subTitle;
    private String title;
    private String btn1Content;
    private String content;
    private Integer background;
    private Integer drawableBtn1;
    private String profilePicture;

    public Builder drawableBtn1(Integer f) {
      this.drawableBtn1 = f;
      return this;
    }

    public Builder content(String f) {
      this.content = f;
      return this;
    }

    public Builder profilePicture(String f) {
      this.profilePicture = f;
      return this;
    }

    public Builder background(Integer f) {
      this.background = f;
      return this;
    }

    public Builder btn1Content(String f) {
      this.btn1Content = f;
      return this;
    }

    public Builder subTitle(String f) {
      this.subTitle = f;
      return this;
    }

    public Builder title(String f) {
      this.title = f;
      return this;
    }

    public NotificationModel build() {
      return new NotificationModel(this);
    }
  }
}
