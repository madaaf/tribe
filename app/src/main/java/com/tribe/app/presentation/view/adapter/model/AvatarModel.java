package com.tribe.app.presentation.view.adapter.model;

import com.tribe.app.presentation.view.widget.avatar.AvatarLiveView;

/**
 * Created by tiago on 02/03/2017.
 */

public class AvatarModel {

  private String url;
  private @AvatarLiveView.AvatarLiveType int type;

  public AvatarModel(String url, @AvatarLiveView.AvatarLiveType int type) {
    this.url = url;
    this.type = type;
  }

  public int getType() {
    return type;
  }

  public String getUrl() {
    return url;
  }
}
