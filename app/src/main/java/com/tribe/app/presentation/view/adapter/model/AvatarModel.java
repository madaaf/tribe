package com.tribe.app.presentation.view.adapter.model;

import com.tribe.app.presentation.view.widget.avatar.Avatar;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.io.Serializable;
import java.util.List;

/**
 * Created by tiago on 02/03/2017.
 */

public class AvatarModel implements Serializable {

  private String url;
  private @AvatarView.AvatarType int type;
  private List<String> memberPics;

  public AvatarModel(String url, @AvatarView.AvatarType int type) {
    this.url = url;
    this.type = type;
  }

  public @AvatarView.AvatarType int getType() {
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

  public boolean isLiveOrOnline() {
    return type == AvatarView.ONLINE || type == AvatarView.LIVE;
  }
}
