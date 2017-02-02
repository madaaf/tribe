package com.tribe.app.domain.entity;

import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.view.utils.ObjectUtils;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by tiago on 05/08/2016.
 */
public abstract class Recipient implements Serializable {

  public static final String ID_EMPTY = "EMPTY";
  public static final String ID_HEADER = "HEADER";

  public static final String DISPLAY_NAME = "DISPLAY_NAME";
  public static final String PROFILE_PICTURE = "PROFILE_PICTURE";
  public static final String IS_LIVE = "IS_LIVE";
  public static final String IS_ONLINE = "IS_ONLINE";
  public static final String LAST_ONLINE = "LAST_ONLINE";

  protected Date created_at;
  protected Date updated_at;

  protected boolean mute;

  protected int position;

  public Date getCreatedAt() {
    return created_at;
  }

  public void setCreatedAt(Date createdAt) {
    this.created_at = createdAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updated_at = updatedAt;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public int getPosition() {
    return position;
  }

  public static int nullSafeComparator(final Recipient one, final Recipient two) {
    int res = ((Boolean) two.isLive()).compareTo(one.isLive());
    if (res != 0) return res;

    res = ((Boolean) two.isOnline()).compareTo(one.isOnline());
    if (res != 0) return res;

    return DateUtils.compareDateNullSafe(two.getLastOnline(), one.getLastOnline());
  }

  @Override public boolean equals(Object obj) {
    if (this == obj) return true;

    if (obj == null) return false;

    if (getClass() != obj.getClass()) return false;

    Recipient recipient = (Recipient) obj;

    return ObjectUtils.nullSafeEquals(getDisplayName(), recipient.getDisplayName())
        && ObjectUtils.nullSafeEquals(getLastOnline(), recipient.getLastOnline())
        && ObjectUtils.nullSafeEquals(getProfilePicture(), recipient.getProfilePicture())
        && ObjectUtils.nullSafeEquals(isLive(), recipient.isLive())
        && ObjectUtils.nullSafeEquals(isOnline(), recipient.isOnline());
  }

  public abstract String getDisplayName();

  public abstract String getUsername();

  public abstract String getUsernameDisplay();

  public abstract String getProfilePicture();

  public abstract String getSubId();

  public abstract String getId();

  public abstract Date getUpdatedAt();

  public abstract boolean isLive();

  public abstract boolean isOnline();

  public abstract Date getLastOnline();

  public boolean isMute() {
    return mute;
  }

  public void setMute(boolean mute) {
    this.mute = mute;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getId() != null ? getId().hashCode() : 0);
    return result;
  }
}
