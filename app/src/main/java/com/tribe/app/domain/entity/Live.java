package com.tribe.app.domain.entity;

import android.support.annotation.StringDef;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.activity.LiveActivity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 23/02/2017.
 */

public class Live implements Serializable {

  @StringDef({ NEW_CALL, WEB }) public @interface LiveType {
  }

  public static final String NEW_CALL = "NEW_CALL";
  public static final String WEB = "WEB";

  private @LiveType String type;
  private String[] userIds;
  private String displayName;
  private String userName;
  private String picture;
  private List<User> memberList;
  private boolean isInvite;
  private String sessionId;
  private String linkId;
  private String url;
  private int color = 0;
  private boolean countdown = true;
  private boolean intent = false;
  private @LiveActivity.Source String source;
  private boolean isDiceDragedInRoom = false;
  private String fbId;

  private Live(Builder builder) {
    this.userIds = builder.userIds;
    this.type = builder.type;
    this.displayName = builder.displayName;
    this.picture = builder.picture;
    this.memberList = builder.memberList;
    this.isInvite = builder.isInvite;
    this.sessionId = builder.sessionId;
    this.color = builder.color;
    this.countdown = builder.countdown;
    this.intent = builder.intent;
    this.userName = builder.userName;
    this.linkId = builder.linkId;
    this.url = builder.url;
    this.source = builder.source;
    this.isDiceDragedInRoom = builder.isDiceDragedInRoom;
    this.fbId = builder.fbId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String[] getUserIds() {
    return userIds;
  }

  public boolean hasUserIds() {
    return userIds != null && userIds.length > 0;
  }

  public void setUserIds(String[] userIds) {
    this.userIds = userIds;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getUserName() {
    return userName;
  }

  public String getFbId() {
    return fbId;
  }

  public void setUserName(String userName) {
    this.userName = userName;
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

  public boolean isDiceDragedInRoom() {
    return isDiceDragedInRoom;
  }

  public void setDiceDragedInRoom(boolean diceDragedInRoom) {
    isDiceDragedInRoom = diceDragedInRoom;
  }

  public List<User> getMembers() {
    return memberList;
  }

  public void setMembers(List<User> members) {
    this.memberList = members;
  }

  public void setInvite(boolean invite) {
    isInvite = invite;
  }

  public boolean isInvite() {
    return isInvite;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public void setCallRouletteSessionId(String sessionId) {
    setSessionId(sessionId);
    setLinkId(null);
    setUrl(null);
  }

  public int getColor() {
    return color;
  }

  public void setColor(int color) {
    this.color = color;
  }

  public boolean isCountdown() {
    return countdown;
  }

  public void setCountdown(boolean countdown) {
    this.countdown = countdown;
  }

  public boolean isIntent() {
    return intent;
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

  public void setLinkId(String linkId) {
    this.linkId = linkId;
  }

  public String getLinkId() {
    return linkId;
  }

  public boolean isSessionOrLink() {
    return !StringUtils.isEmpty(sessionId) || !StringUtils.isEmpty(linkId);
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @LiveActivity.Source public String getSource() {
    return source;
  }

  public void setSource(@LiveActivity.Source String source) {
    this.source = source;
  }

  public static class Builder {

    private @LiveType String type;
    private String[] userIds;
    private String displayName;
    private String picture;
    private List<User> memberList;
    private boolean isGroup;
    private boolean isInvite = false;
    private String sessionId;
    private String linkId;
    private String url;
    private int color;
    private boolean countdown = true;
    private boolean intent = false;
    private boolean isDiceDragedInRoom = false;
    private String userName;
    private @LiveActivity.Source String source;
    private String fbId;

    public Builder() {
    }

    public Builder type(@LiveType String type) {
      this.type = type;
      return this;
    }

    public Builder userIds(String... userIds) {
      this.userIds = userIds;
      return this;
    }

    public Builder displayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public Builder fbId(String fbId) {
      this.fbId = fbId;
      return this;
    }

    public Builder userName(String userName) {
      this.userName = userName;
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

    public Builder isInvite(boolean isInvite) {
      this.isInvite = isInvite;
      return this;
    }

    public Builder sessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Builder linkId(String linkId) {
      this.linkId = linkId;
      return this;
    }

    public Builder isDiceDragedInRoom(boolean isDiceDragedInRoom) {
      this.isDiceDragedInRoom = isDiceDragedInRoom;
      return this;
    }

    public Builder url(String url) {
      this.url = url;
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

    public Builder intent(boolean fromIntent) {
      this.intent = fromIntent;
      return this;
    }

    public Builder source(@LiveActivity.Source String source) {
      this.source = source;
      return this;
    }

    public Live build() {
      return new Live(this);
    }
  }
}
