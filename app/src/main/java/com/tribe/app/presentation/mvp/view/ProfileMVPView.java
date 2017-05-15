package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Friendship;
import java.util.List;

public interface ProfileMVPView extends UpdateUserMVPView {

  void goToLauncher();

  void renderBlockedFriendshipList(List<Friendship> friendshipList);

  void renderUnblockedFriendshipList(List<Friendship> friendshipList);
}
