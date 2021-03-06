package com.tribe.app.domain.entity;

import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.model.AvatarModel;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.io.Serializable;

/**
 * Created by tiago on 08/09/2016.
 */
public class SearchResult implements Serializable, BaseListInterface {

  private String id;
  private String display_name;
  private String username;
  private String picture;
  private Shortcut shortcut;
  private boolean invisible_mode = false;
  private boolean searchDone = false;
  private boolean animateAdd = false;
  private boolean isMyself = false;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override public void setAnimateAdd(boolean animateAdd) {
    this.animateAdd = animateAdd;
  }

  @Override public boolean isAnimateAdd() {
    return animateAdd;
  }

  @Override public boolean isActionAvailable(User currentUser) {
    return !StringUtils.isEmpty(getDisplayName()) && !isMyself();
  }

  @Override public boolean isInvisible() {
    return invisible_mode;
  }

  @Override public String getDisplayName() {
    return display_name;
  }

  public void setDisplayName(String displayName) {
    this.display_name = displayName;
  }

  @Override public String getUsername() {
    return username;
  }

  @Override public boolean isFriend() {
    if (shortcut == null) return false;
    return shortcut.isFriend();
  }

  @Override public AvatarModel getAvatar() {
    return new AvatarModel(picture,
        shortcut != null && shortcut.isOnline() ? AvatarView.ONLINE : AvatarView.REGULAR);
  }

  @Override public boolean isReverse() {
    return false;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPicture() {
    return picture;
  }

  public void setPicture(String picture) {
    this.picture = picture;
  }

  public boolean isSearchDone() {
    return searchDone;
  }

  public void setSearchDone(boolean searchDone) {
    this.searchDone = searchDone;
  }

  public void setInvisibleMode(boolean invisibleMode) {
    this.invisible_mode = invisibleMode;
  }

  public boolean isMyself() {
    return isMyself;
  }

  public void setMyself(boolean myself) {
    isMyself = myself;
  }

  public Shortcut getShortcut() {
    return shortcut;
  }

  public void setShortcut(Shortcut shortcut) {
    this.shortcut = shortcut;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + "search".hashCode();
    return result;
  }
}
