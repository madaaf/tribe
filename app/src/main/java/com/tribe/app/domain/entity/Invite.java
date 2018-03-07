package com.tribe.app.domain.entity;

import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.model.AvatarModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 02/02/2017.
 */

public class Invite extends Recipient {

  private Room room;
  private User inviter;
  private Shortcut shortcut;

  public void setInviter(User inviter) {
    this.inviter = inviter;
  }

  public User getInviter() {
    return inviter;
  }

  public void setRoom(Room room) {
    this.room = room;
  }

  public Room getRoom() {
    return room;
  }

  public String getRoomName() {
    if (room != null) {
      return room.getName();
    } else if (room.getLiveUsers().size() <= 1) {
      return "";
    } else {
      return "PLACEHOLDER";
    }
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
    if (room.getLiveUsers().size() <= 1 && room.getInvitedUsers().size() == 1) {
      return room.getInitiator().getDisplayName();
    } else if (shortcut != null && !StringUtils.isEmpty(shortcut.getName())) {
      return shortcut.getName();
    } else {
      return room.getUserNames();
    }
  }

  @Override public String getUsername() {
    return null;
  }

  @Override public String getProfilePicture() {
    return room.getInitiator().getProfilePicture();
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

  @Override public UserPlaying isPlaying() {
    return null;
  }

  @Override public boolean isOnline() {
    return false;
  }

  @Override public Date getLastSeenAt() {
    return null;
  }

  @Override public boolean isRead() {
    return shortcut != null ? shortcut.isRead() : true;
  }

  @Override public boolean isFriend() {
    return false;
  }

  @Override public AvatarModel getAvatar() {
    return null;
  }

  @Override public String getTrophy() {
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

  public List<User> getAllUsers() {
    return room.getAllUsers();
  }

  public List<String> getRoomUserIds() {
    return room.getUserIds();
  }

  public boolean isSingle() {
    return room.getLiveUsers().size() <= 1;
  }

  public Shortcut getShortcut() {
    return shortcut;
  }

  public void setShortcut(Shortcut shortcut) {
    this.shortcut = shortcut;
    if (this.room != null) room.setShortcut(shortcut);
  }
}
