package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.User;

public interface UserMVPView extends MVPView {

  void onUserInfos(User user);

  void onUserLeaderboard(String userId);
}
