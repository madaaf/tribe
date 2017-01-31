package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.User;

/**
 * Created by horatiothomas on 8/31/16.
 */
public interface UpdateUserMVPView extends LoadDataMVPView {

  void successUpdateUser(User user);

  void successFacebookLogin();

  void errorFacebookLogin();

  void usernameResult(Boolean available);
}
