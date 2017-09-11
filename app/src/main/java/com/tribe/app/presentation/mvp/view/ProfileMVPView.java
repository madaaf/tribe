package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Room;

public interface ProfileMVPView extends UpdateUserMVPView {

  void goToLauncher();

  void onCreateRoom(Room room);
}
