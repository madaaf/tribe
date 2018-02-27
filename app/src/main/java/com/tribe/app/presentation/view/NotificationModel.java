package com.tribe.app.presentation.view;

import android.os.Build;
import android.support.annotation.StringDef;
import android.view.View;
import com.tribe.app.presentation.view.popup.view.PopupParentView;
import com.tribe.app.presentation.view.popup.view.PopupView;

/**
 * Created by madaaflak on 09/02/2018.
 */

public class NotificationModel {

  public static final String POPUP_CHALLENGER = "POPUP_CHALLENGER";
  public static final String POPUP_FACEBOOK = "POPUP_FACEBOOK";
  public static final String POPUP_UPLOAD_PICTURE = "POPUP_UPLOAD_PICTURE";

  @StringDef({
      POPUP_CHALLENGER, POPUP_FACEBOOK, POPUP_UPLOAD_PICTURE
  }) public @interface NotificationType {
  }

  private String title;
  private String subTitle;
  private String content;
  private String profilePicture;
  private Integer logoPicture;
  private Integer background;
  private String userId;
  private @NotificationType String type;
  private View view;

  private String btn1Content;
  private Integer btn1DrawableStart;
  private Integer btn1DrawableEnd;
  private Integer btn1Background;

  private NotificationModel(Builder b) {
    this.title = b.title;
    this.subTitle = b.subTitle;
    this.content = b.content;
    this.background = b.background;
    this.profilePicture = b.profilePicture;
    this.userId = b.userId;
    this.logoPicture = b.logoPicture;
    this.type = b.type;
    this.btn1Content = b.btn1Content;
    this.btn1DrawableEnd = b.btn1DrawableEnd;
    this.btn1DrawableStart = b.btn1DrawableStart;
    this.btn1Background = b.btn1Background;
    this.view = b.view;
  }

  public Integer getBtn1Background() {
    return btn1Background;
  }

  public String getType() {
    return type;
  }

  public Integer getLogoPicture() {
    return logoPicture;
  }

  public String getUserId() {
    return userId;
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

  public Integer getBtn1DrawableStart() {
    return btn1DrawableStart;
  }

  public String getProfilePicture() {
    return profilePicture;
  }

  public Integer getBackground() {
    return background;
  }

  public Integer getBtn1DrawableEnd() {
    return btn1DrawableEnd;
  }

  public View getView() {
    return view;
  }

  public static class Builder {
    private String subTitle;
    private String title;
    private String content;
    private Integer background;
    private Integer logoPicture;
    private String profilePicture;
    private String userId;
    private Integer btn1DrawableStart;
    private Integer btn1DrawableEnd;
    private String btn1Content;
    private Integer btn1Background;
    private View view;

    private @NotificationType String type;

    public Builder type(@NotificationType String f) {
      this.type = f;
      return this;
    }

    public Builder drawableBtnEnd(Integer f) {
      this.btn1DrawableEnd = f;
      return this;
    }

    public Builder logoPicture(Integer f) {
      this.logoPicture = f;
      return this;
    }

    public Builder userId(String f) {
      this.userId = f;
      return this;
    }

    public Builder btn1Background(Integer f) {
      this.btn1Background = f;
      return this;
    }

    public Builder drawableBtn1(Integer f) {
      this.btn1DrawableStart = f;
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

    public Builder view(View view) {
      this.view = view;
      return this;
    }

    public NotificationModel build() {
      return new NotificationModel(this);
    }
  }
}
