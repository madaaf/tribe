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

  private String room_id;
  private Group group;
  private List<Friendship> friendships;

  public void setFriendships(List<Friendship> friendships) {
    this.friendships = friendships;
  }

  public List<Friendship> getFriendships() {
    return friendships;
  }

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
  }

  public String getRoomId() {
    return room_id;
  }

  public void setRoomId(String roomId) {
    this.room_id = roomId;
  }

  @Override public boolean isActionAvailable(User currentUser) {
    return false;
  }

  @Override public boolean isInvisible() {
    return false;
  }

  @Override public String getDisplayName() {
    return isGroup() ? group.getName() : getFriendshipsName();
  }

  @Override public String getUsername() {
    return null;
  }

  @Override public String getUsernameDisplay() {
    return null;
  }

  @Override public String getProfilePicture() {
    return isGroup() ? group.getPicture() : "";
  }

  @Override public String getSubId() {
    return room_id;
  }

  @Override public String getId() {
    return room_id;
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

  @Override public boolean isGroup() {
    return group != null;
  }

  @Override public boolean isFriend() {
    return false;
  }

  @Override public AvatarModel getAvatar() {
    return null;
  }

  private String getFriendshipsName() {
    String name = "";

    int count = 0;
    for (Friendship friendship : friendships) {
      if (count > 0) name += ", ";
      name += friendship.getDisplayName();
      count++;
    }

    return name;
  }

  public List<String> getMembersPic() {
    List<String> pics = new ArrayList<>();

    if (isGroup()) {
      return group.getMembersPics();
    } else if (friendships != null) {
      for (Friendship friendship : friendships) {
        String url = friendship.getProfilePicture();
        if (!StringUtils.isEmpty(url)) pics.add(url);
      }
    }

    return pics;
  }

  public List<User> getMembers() {
    List<User> userList = new ArrayList<>();

    if (friendships != null) {
      for (Friendship fr : friendships) {
        userList.add(fr.getFriend());
      }
    } else if (group != null) {
      userList.addAll(group.getMembers());
    }

    return userList;
  }
}
