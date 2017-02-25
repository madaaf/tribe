package com.tribe.app.domain.entity;

import com.tribe.app.presentation.utils.StringUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 23/02/2017.
 */

public class Live implements Serializable {

  private String id;
  private String subId;
  private String displayName;
  private String picture;
  private List<User> memberList;
  private boolean isGroup;
  private String sessionId;
  private int color = 0;
  private boolean countdown = true;

  private Live(Builder builder) {
    this.id = builder.id;
    this.displayName = builder.displayName;
    this.picture = builder.picture;
    this.memberList = builder.memberList;
    this.isGroup = builder.isGroup;
    this.sessionId = builder.sessionId;
    this.color = builder.color;
    this.subId = builder.subId;
    this.countdown = builder.countdown;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getPicture() {
    return picture;
  }

  public void setPicture(String picture) {
    this.picture = picture;
  }

  public List<User> getMembers() {
    return memberList;
  }

  public void setMembers(List<User> members) {
    this.memberList = members;
  }

  public boolean isGroup() {
    return isGroup;
  }

  public void setGroup(boolean group) {
    isGroup = group;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public int getColor() {
    return color;
  }

  public void setColor(int color) {
    this.color = color;
  }

  public String getSubId() {
    return subId;
  }

  public void setSubId(String subId) {
    this.subId = subId;
  }

  public boolean isCountdown() {
    return countdown;
  }

  public void setCountdown(boolean countdown) {
    this.countdown = countdown;
  }

  public boolean isGroupMember(String userId) {
    if (memberList != null) {
      for (User member : memberList) {
        if (member.getId().equals(userId)) {
          return true;
        }
      }
    }

    return false;
  }

  public List<String> getMembersPics() {
    List<String> pics = new ArrayList<>();

    if (memberList != null) {
      List<User> subMembers =
          memberList.subList(Math.max(memberList.size() - 4, 0), memberList.size());

      if (subMembers != null) {
        for (User user : subMembers) {
          String url = user.getProfilePicture();
          if (!StringUtils.isEmpty(url)) pics.add(url);
        }
      }
    }

    return pics;
  }

  public static class Builder {

    private String id;
    private String subId;
    private String displayName;
    private String picture;
    private List<User> memberList;
    private boolean isGroup;
    private String sessionId;
    private int color;
    private boolean countdown = true;

    public Builder(String id, String subId) {
      this.id = id;
      this.subId = subId;
    }

    public Builder displayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public Builder picture(String picture) {
      this.picture = picture;
      return this;
    }

    public Builder memberList(List<User> memberList) {
      this.memberList = memberList;
      return this;
    }

    public Builder isGroup(boolean isGroup) {
      this.isGroup = isGroup;
      return this;
    }

    public Builder sessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Builder color(int color) {
      this.color = color;
      return this;
    }

    public Builder countdown(boolean countdown) {
      this.countdown = countdown;
      return this;
    }

    public Live build() {
      return new Live(this);
    }
  }
}
