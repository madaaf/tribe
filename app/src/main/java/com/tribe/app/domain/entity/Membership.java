package com.tribe.app.domain.entity;

import com.tribe.app.presentation.utils.StringUtils;
import java.util.Date;
import java.util.List;

public class Membership extends Recipient {

  private String id;
  private Group group;

  public Membership(String id) {
    this.id = id;
  }

  @Override public String getDisplayName() {
    String name = group.getName();

    if (StringUtils.isEmpty(name)) {
      return group.getMembersNames();
    } else {
      return group.getName();
    }
  }

  @Override public String getUsername() {
    return null;
  }

  @Override public boolean isFriend() {
    return false;
  }

  @Override public String getUsernameDisplay() {
    return null;
  }

  @Override public String getProfilePicture() {
    return group.getPicture();
  }

  @Override public String getSubId() {
    return group.getId();
  }

  @Override public String getId() {
    return id;
  }

  @Override public Date getUpdatedAt() {
    return updated_at;
  }

  @Override public boolean isLive() {
    return group.isLive();
  }

  @Override public boolean isOnline() {
    return false;
  }

  @Override public Date getLastSeenAt() {
    return group.getLastSeenAt();
  }

  @Override public boolean isGroup() {
    return true;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
  }

  public List<String> getMembersPic() {
    return group.getMembersPics();
  }
}