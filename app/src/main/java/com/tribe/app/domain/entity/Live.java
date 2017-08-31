package com.tribe.app.domain.entity;

import android.support.annotation.StringDef;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.activity.LiveActivity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tiago on 23/02/2017.
 */

public class Live implements Serializable {

  @StringDef({ NEW_CALL, WEB, FRIEND_CALL }) public @interface LiveType {
  }

  public static final String NEW_CALL = "NEW_CALL";
  public static final String WEB = "WEB";
  public static final String FRIEND_CALL = "FRIEND_CALL";

  private @LiveType String type;
  private Room room;
  private boolean fromRoom = false;
  private List<User> users;
  private List<String> userIds;
  private List<String> userPics;
  private String url;
  private int color = 0;
  private boolean countdown = true;
  private boolean intent = false;
  private @LiveActivity.Source String source;
  private boolean isDiceDragedInRoom = false;

  private Live(Builder builder) {
    this.room = builder.room;
    this.fromRoom = room != null;
    setUsers(builder.users);
    this.type = builder.type;
    this.color = builder.color;
    this.countdown = builder.countdown;
    this.intent = builder.intent;
    this.url = builder.url;
    this.source = builder.source;
    this.isDiceDragedInRoom = builder.isDiceDragedInRoom;
  }

  public Room getRoom() {
    return room;
  }

  public void setRoom(Room room) {
    this.room = room;
    setUsers(room.getLiveUsers());
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isDiceDragedInRoom() {
    return isDiceDragedInRoom;
  }

  public void setDiceDragedInRoom(boolean diceDragedInRoom) {
    isDiceDragedInRoom = diceDragedInRoom;
  }

  public String getRoomId() {
    return room != null ? room.getId() : null;
  }

  public void setUsers(List<User> users) {
    this.users = users;

    userIds = new ArrayList<>();
    userPics = new ArrayList<>();

    if (users != null) {
      for (User user : users) {
        userIds.add(user.getId());
      }

      userPics = new ArrayList<>();

      List<User> subUsers = users.subList(Math.max(users.size() - 4, 0), users.size());

      if (subUsers != null) {
        for (User user : users) {
          String url = user.getProfilePicture();
          if (!StringUtils.isEmpty(url)) userPics.add(url);
        }
      }
    }
  }

  public List<User> getUsers() {
    return users;
  }

  public List<String> getUserIds() {
    return userIds;
  }

  public boolean hasUsers() {
    return users != null && users.size() > 0;
  }

  public String getName() {
    if (fromRoom() && room.getInitiator() != null) {
      return room.getInitiator().getDisplayName();
    } else if (hasUsers()) {
      return users.get(0).getDisplayName();
    } else {
      return type;
    }
  }

  public boolean hasUser(String userId) {
    return userIds != null && userIds.contains(userId);
  }

  public void setCallRouletteSessionId(String sessionId) {
    setRoom(new Room(sessionId));
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

  public List<String> getUsersPics() {
    return userPics;
  }

  public boolean hasRoom() {
    return room != null;
  }

  public boolean fromRoom() {
    return fromRoom;
  }

  public boolean hasRoomId() {
    return room != null;
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

    private Room room;
    private @LiveType String type;
    private List<User> users;
    private String url;
    private int color;
    private boolean countdown = true;
    private boolean intent = false;
    private boolean isDiceDragedInRoom = false;
    private @LiveActivity.Source String source;

    public Builder(@LiveType String type) {
      this.type = type;
    }

    public Builder room(Room room) {
      this.room = room;
      return this;
    }

    public Builder users(User... users) {
      this.users = new ArrayList<>(Arrays.asList(users));
      return this;
    }

    public Builder roomId(String roomId) {
      this.room = new Room(roomId);
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
