package com.tribe.app.presentation.view.adapter.model;

import com.tribe.app.presentation.view.widget.avatar.AvatarLiveView;
import java.util.List;

/**
 * Created by tiago on 02/03/2017.
 */

public class AvatarModel {

  private String url;
  private @AvatarLiveView.AvatarLiveType int type;
  private List<String> memberPics;

  public AvatarModel(String url, @AvatarLiveView.AvatarLiveType int type) {
    this.url = url;
    this.type = type;
  }

  public @AvatarLiveView.AvatarLiveType int getType() {
    return type;
  }

  public String getUrl() {
    return url;
  }

  public void setMemberPics(List<String> memberPics) {
    this.memberPics = memberPics;
  }

  public List<String> getMemberPics() {
    return memberPics;
  }
}
