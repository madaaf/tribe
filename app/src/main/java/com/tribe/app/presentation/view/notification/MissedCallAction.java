package com.tribe.app.presentation.view.notification;

import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.model.AvatarModel;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.io.Serializable;

/**
 * Created by madaaflak on 27/04/2017.
 */

public class MissedCallAction implements Serializable, BaseListInterface {
  private String userId;
  private int nbrMissedCall;
  private NotificationPayload notificationPayload;
  protected AvatarModel avatarModel = null;

  public MissedCallAction(String userId, NotificationPayload notificationPayload,
      int nbrMissedCall) {
    this.userId = userId;
    this.nbrMissedCall = nbrMissedCall;
    this.notificationPayload = notificationPayload;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public int getNbrMissedCall() {
    return nbrMissedCall;
  }

  public void setNbrMissedCall(int nbrMissedCall) {
    this.nbrMissedCall = nbrMissedCall;
  }

  public NotificationPayload getNotificationPayload() {
    return notificationPayload;
  }

  public void setNotificationPayload(NotificationPayload notificationPayload) {
    this.notificationPayload = notificationPayload;
  }

  @Override public String getId() {
    return notificationPayload.getUserId();
  }

  @Override public void setAnimateAdd(boolean animateAdd) {

  }

  @Override public boolean isAnimateAdd() {
    return true;
  }

  @Override public boolean isActionAvailable(User currentUser) {
    return true;
  }

  @Override public boolean isInvisible() {
    return false;
  }

  @Override public String getDisplayName() {
    return notificationPayload.getUserDisplayName();
  }

  @Override public String getUsername() {
    return notificationPayload.getUserDisplayName();
  }

  @Override public boolean isFriend() {
    return true;
  }

  @Override public AvatarModel getAvatar() {
    if (avatarModel != null) return avatarModel;
    avatarModel = new AvatarModel(notificationPayload.getUserPicture(), AvatarView.REGULAR);
    return avatarModel;
  }

  @Override public boolean isReverse() {
    return false;
  }
}