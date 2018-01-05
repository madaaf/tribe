package com.tribe.app.domain.entity;

import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.view.adapter.decorator.BaseSectionItemDecoration;
import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.interfaces.HomeAdapterInterface;
import com.tribe.app.presentation.view.adapter.model.AvatarModel;
import com.tribe.app.presentation.view.utils.ObjectUtils;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by tiago on 05/08/2016.
 */
public abstract class Recipient implements Serializable, BaseListInterface, HomeAdapterInterface {

  public static final String ID_EMPTY = "EMPTY";
  public static final String ID_HEADER = "HEADER";
  public static final String ID_MORE = "MORE";
  public static final String ID_VIDEO = "VIDEO";
  public static final String ID_CALL_ROULETTE = "ID_CALL_ROULETTE";

  public static final String DISPLAY_NAME = "DISPLAY_NAME";
  public static final String PROFILE_PICTURE = "PROFILE_PICTURE";
  public static final String IS_LIVE = "IS_LIVE";
  public static final String IS_ONLINE = "IS_ONLINE";
  public static final String LAST_ONLINE = "LAST_ONLINE";

  protected Date created_at;
  protected Date updated_at;
  protected int position;
  protected boolean animateAdd = false;
  protected AvatarModel avatarModel = null;

  public Date getCreatedAt() {
    return created_at;
  }

  public void setCreatedAt(Date createdAt) {
    this.created_at = createdAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updated_at = updatedAt;
  }

  public Date getUpdatedAt() {
    return this.updated_at;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public int getPosition() {
    return position;
  }

  public boolean isSupport() {
    return getId().equals(Shortcut.SUPPORT);
  }

  public static int nullSafeComparator(final Recipient one, final Recipient two) {
    int res = ((Boolean) two.isLive()).compareTo(one.isLive());
    if (res != 0) return res;

    res = ((Boolean) !two.isRead()).compareTo(!one.isRead());
    if (res != 0) return res;

    res = ((Boolean) two.isOnline()).compareTo(one.isOnline());
    if (res != 0) return res;

    return DateUtils.compareDateNullSafe(two.getLastSeenAt(), one.getLastSeenAt());
  }

  @Override public boolean equals(Object obj) {
    if (this == obj) return true;

    if (obj == null) return false;

    if (getClass() != obj.getClass()) return false;

    Recipient recipient = (Recipient) obj;

    return ObjectUtils.nullSafeEquals(getDisplayName(), recipient.getDisplayName())
        && ObjectUtils.nullSafeEquals(getLastSeenAt(), recipient.getLastSeenAt())
        && ObjectUtils.nullSafeEquals(getProfilePicture(), recipient.getProfilePicture())
        && ObjectUtils.nullSafeEquals(isLive(), recipient.isLive())
        && ObjectUtils.nullSafeEquals(isOnline(), recipient.isOnline())
        && ObjectUtils.nullSafeEquals(getLastSeenAt(), recipient.getLastSeenAt());
  }

  public abstract String getDisplayName();

  public abstract String getUsername();

  public abstract String getProfilePicture();

  public abstract String getId();

  public abstract boolean isLive();

  public abstract boolean isOnline();

  public abstract Date getLastSeenAt();

  public abstract boolean isRead();

  public boolean isFake() {
    return (getId().equals(Recipient.ID_EMPTY)
        || getId().equals(Recipient.ID_HEADER)
        || getId().equals(Recipient.ID_MORE)
        || getId().equals(Recipient.ID_VIDEO)
        || getId().equals(Recipient.ID_CALL_ROULETTE));
  }

  public @BaseSectionItemDecoration.HeaderType int getHomeSectionType() {
    if (isLive() || !isRead()) {
      return BaseSectionItemDecoration.HOME_ONGOING;
    } else if (isOnline()) {
      return BaseSectionItemDecoration.HOME_ONLINE;
    } else if (!getId().equals(ID_EMPTY) && !getId().equals(ID_HEADER)) {
      return BaseSectionItemDecoration.HOME_RECENT;
    } else {
      return BaseSectionItemDecoration.NONE;
    }
  }

  public String getSectionTag() {
    String section = null;

    switch (getHomeSectionType()) {
      case BaseSectionItemDecoration.HOME_ONGOING:
        section = TagManagerUtils.SECTION_ONGOING;
        break;

      case BaseSectionItemDecoration.HOME_ONLINE:
        section = TagManagerUtils.SECTION_ONLINE;
        break;

      case BaseSectionItemDecoration.HOME_RECENT:
        section = TagManagerUtils.SECTION_RECENT;
        break;
    }

    return section;
  }

  @Override public void setAnimateAdd(boolean animateAdd) {
    this.animateAdd = animateAdd;
  }

  @Override public boolean isAnimateAdd() {
    return animateAdd;
  }

  @Override public boolean isReverse() {
    return false;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getId() != null ? getId().hashCode() : 0);
    return result;
  }
}
