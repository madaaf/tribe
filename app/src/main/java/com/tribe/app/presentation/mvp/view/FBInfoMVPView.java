package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.FacebookEntity;

public interface FBInfoMVPView extends MVPView {

  void loadFacebookInfos(FacebookEntity facebookEntity);

  void successFacebookLogin();

  void errorFacebookLogin();
}

