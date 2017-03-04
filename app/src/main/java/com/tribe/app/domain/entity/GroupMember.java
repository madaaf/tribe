package com.tribe.app.domain.entity;

import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.model.AvatarModel;
import com.tribe.app.presentation.view.widget.avatar.AvatarLiveView;
import java.io.Serializable;

/**
 * Created by tiago on 23/11/2016.
 */
public class GroupMember implements Serializable, BaseListInterface {

  private boolean ogMember = false;
  private boolean member = false;
  private boolean friend = false;
  private boolean animateAdd = false;
  private User user;
  private Friendship friendship;
  private AvatarModel avatarModel = null;

  public GroupMember(User user) {
    this.user = user;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;

    if (getClass() != o.getClass()) return false;

    GroupMember that = (GroupMember) o;

    return user.getId() != null ? user.getId().equals(that.user.getId())
        : that.user.getId() == null;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (user.getId() != null ? user.getId().hashCode() : 0);
    return result;
  }

  public static int nullSafeComparator(final GroupMember one, final GroupMember two) {
    if (one.getUser().getCreatedAt() == null ^ two.getUser().getCreatedAt() == null) {
      return (one.getUser().getCreatedAt() == null) ? 1 : -1;
    }

    if (one.getUser().getUpdatedAt() == null && two.getUser().getUpdatedAt() == null) {
      return 0;
    }

    return two.getUser().getUpdatedAt().compareTo(one.getUser().getUpdatedAt());
  }

  public boolean isMember() {
    return member;
  }

  public boolean isOgMember() {
    return ogMember;
  }

  public void setOgMember(boolean ogMember) {
    this.ogMember = ogMember;
  }

  public void setMember(boolean member) {
    this.member = member;
  }

  @Override public boolean isAnimateAdd() {
    return animateAdd;
  }

  @Override public boolean isActionAvailable(User currentUser) {
    return !user.equals(currentUser);
  }

  @Override public boolean isInvisible() {
    return user.isInvisible();
  }

  @Override public String getDisplayName() {
    return user.getDisplayName();
  }

  @Override public String getUsername() {
    return user.getUsername();
  }

  @Override public String getId() {
    return user.getId();
  }

  @Override public void setAnimateAdd(boolean animateAdd) {
    this.animateAdd = animateAdd;
  }

  @Override public boolean isFriend() {
    return friend;
  }

  @Override public AvatarModel getAvatar() {
    if (avatarModel != null) return avatarModel;
    avatarModel = new AvatarModel(user.getProfilePicture(),
        user.isOnline() ? AvatarLiveView.CONNECTED : AvatarLiveView.NONE);
    return avatarModel;
  }

  @Override public boolean isReverse() {
    return isMember();
  }

  public void setFriend(boolean friend) {
    this.friend = friend;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public void setFriendship(Friendship friendship) {
    this.friendship = friendship;
  }

  public Friendship getFriendship() {
    return friendship;
  }
}
