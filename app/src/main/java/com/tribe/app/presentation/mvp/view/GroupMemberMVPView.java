package com.tribe.app.presentation.mvp.view;

/**
 * Created by horatiothomas on 9/19/16.
 */
public interface GroupMemberMVPView extends LoadDataMVPView {
  void createFriendship();

  void removeFriend();

  void setAdmin();

  void removeAdmin();

  void removeMember();
}
