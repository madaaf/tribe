package com.tribe.app.domain.entity;

import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.presentation.view.adapter.model.AvatarModel;
import com.tribe.app.presentation.view.widget.avatar.AvatarLiveView;
import java.text.Normalizer;
import java.util.Date;

/**
 * Created by tiago on 05/08/2016.
 */
public class Friendship extends Recipient implements Comparable<Friendship> {

  private String id;
  private String tag;
  private String category;
  private User friend;
  private boolean isSelected;
  private String status;
  private boolean is_live;

  public Friendship(String id) {
    this.id = id;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public User getFriend() {
    return friend;
  }

  public void setFriend(User friend) {
    this.friend = friend;
  }

  public boolean isSelected() {
    return isSelected;
  }

  public void setSelected(boolean selected) {
    isSelected = selected;
  }

  @Override public String getId() {
    return id;
  }

  @Override public boolean isActionAvailable(User currentUser) {
    return friend == null || !currentUser.equals(friend);
  }

  @Override public boolean isInvisible() {
    return friend != null && friend.isInvisibleMode();
  }

  @Override public String getDisplayName() {
    if (friend != null) return friend.getDisplayName();
    return "";
  }

  @Override public String getProfilePicture() {
    if (friend != null) return friend.getProfilePicture();
    return "";
  }

  @Override public String getSubId() {
    if (id.equals(Recipient.ID_EMPTY) || id.equals(Recipient.ID_HEADER)) return id;

    return friend.getId();
  }

  @Override public String getUsername() {
    return friend.getUsername();
  }

  @Override public boolean isFriend() {
    return friend != null;
  }

  @Override public AvatarModel getAvatar() {
    if (avatarModel != null) return avatarModel;
    avatarModel = new AvatarModel(getProfilePicture(), isLive() ? AvatarLiveView.LIVE
        : (isOnline() ? AvatarLiveView.CONNECTED : AvatarLiveView.NONE));
    return avatarModel;
  }

  @Override public String getUsernameDisplay() {
    return friend.getUsernameDisplay();
  }

  @Override public Date getUpdatedAt() {
    return friend.getUpdatedAt();
  }

  @Override public Date getLastSeenAt() {
    if (friend != null) return friend.getLastSeenAt();
    return null;
  }

  @Override public boolean isGroup() {
    return false;
  }

  public @FriendshipRealm.FriendshipStatus String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public boolean isBlockedOrHidden() {
    return status != null && !status.equals(FriendshipRealm.DEFAULT);
  }

  public boolean isBlocked() {
    return status != null && status.equals(FriendshipRealm.BLOCKED);
  }

  public boolean isHidden() {
    return status != null && status.equals(FriendshipRealm.HIDDEN);
  }

  @Override public boolean isLive() {
    return is_live;
  }

  @Override public boolean isOnline() {
    return friend != null && friend.isOnline();
  }

  public void setIsLive(boolean isLive) {
    this.is_live = isLive;
  }

  @Override public int compareTo(Friendship other) {
    if (other == null) return 1;

    String name1 = Normalizer.normalize(this.getDisplayName(), Normalizer.Form.NFD);
    name1 = name1.replaceAll("[^\\p{ASCII}]", "");
    String name2 = Normalizer.normalize(other.getDisplayName(), Normalizer.Form.NFD);
    name2 = name2.replaceAll("[^\\p{ASCII}]", "");

    return name1.compareTo(name2);
  }
}
