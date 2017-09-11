package com.tribe.app.domain.entity;

import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.model.AvatarModel;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 09/10/2017.
 */
public class Shortcut implements Serializable, BaseListInterface {

  private String id;
  private String name;
  private String picture;
  private boolean pinned;
  private boolean blocked;
  private boolean read;
  private Date created_at;
  private Date last_activity_at;
  private List<User> members;

  public Shortcut(String id) {
    this.id = id;
  }

  @Override public String getId() {
    return id;
  }

  @Override public void setAnimateAdd(boolean animateAdd) {

  }

  @Override public boolean isAnimateAdd() {
    return false;
  }

  @Override public boolean isActionAvailable(User currentUser) {
    return false;
  }

  @Override public boolean isInvisible() {
    return false;
  }

  @Override public String getDisplayName() {
    return null;
  }

  @Override public String getUsername() {
    return null;
  }

  @Override public boolean isFriend() {
    return false;
  }

  @Override public AvatarModel getAvatar() {
    return null;
  }

  @Override public boolean isReverse() {
    return false;
  }

  public void setId(String id) {
    this.id = id;
  }
}
