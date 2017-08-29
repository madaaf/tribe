package com.tribe.app.domain.entity;

import com.tribe.app.presentation.view.adapter.model.AvatarModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 02/02/2017.
 */

public class Invite extends Recipient {

  private Room room;

  public void setRoom(Room room) {
    this.room = room;
  }

  public Room getRoom() {
    return room;
  }

  public String getRoomName() {
    return room != null ? room.getName() : "";
  }

  public void setRoomName(String name) {
    room.setName(name);
  }

  @Override public boolean isActionAvailable(User currentUser) {
    return false;
  }

  @Override public boolean isInvisible() {
    return false;
  }

  @Override public String getDisplayName() {
    return room.getName();
  }

  @Override public String getUsername() {
    return null;
  }

  @Override public String getUsernameDisplay() {
    return null;
  }

  @Override public String getProfilePicture() {
    return "";
  }

  @Override public String getSubId() {
    return room.getId();
  }

  @Override public String getId() {
    return room.getId();
  }

  @Override public Date getUpdatedAt() {
    return null;
  }

  @Override public boolean isLive() {
    return true;
  }

  @Override public boolean isOnline() {
    return false;
  }

  @Override public Date getLastSeenAt() {
    return null;
  }

  @Override public boolean isFriend() {
    return false;
  }

  @Override public AvatarModel getAvatar() {
    return null;
  }

  public List<String> getMembersPic() {
    List<String> pics = new ArrayList<>();

    if (room.getLiveUsers() != null) {
      for (User user : room.getLiveUsers()) {
        pics.add(user.getProfilePicture());
      }
    }

    return pics;
  }

  public List<User> getMembers() {
    List<User> userList = new ArrayList<>();

    if (room.getLiveUsers() != null) {
      for (User user : room.getLiveUsers()) {
        userList.add(user);
      }
    }

    return userList;
  }

  public boolean isFriendship(String userId) {
    return room.getLiveUsers().size() == 1 && room.getLiveUsers().get(0).getId().equals(userId);
  }
}
