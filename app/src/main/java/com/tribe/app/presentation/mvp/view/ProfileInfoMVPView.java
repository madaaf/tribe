package com.tribe.app.presentation.mvp.view;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.User;

public interface ProfileInfoMVPView extends UpdateUserMVPView {

  void onRegisterSuccess(AccessToken accessToken);

  void onRegisterFail();

  void userInfos(User user);
}

