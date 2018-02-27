package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.User;
import java.util.List;

public interface UserMVPView extends MVPView {

  void onUserInfos(User user);

  void onUserLeaderboard(String userId);

  void onUserInfosList(List<User> users);

}
