package com.tribe.app.presentation.mvp.view.adapter;

import android.content.Context;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.presentation.mvp.view.RoomMVPView;

/**
 * Created by tiago on 03/08/2018.
 */

public class RoomMVPViewAdapter implements RoomMVPView {

  @Override public Context context() {
    return null;
  }

  @Override public void onRoomInfosError(String str) {

  }

  @Override public void onRoomInfos(Room room) {

  }

  @Override public void onRoomUpdate(Room room) {

  }

  @Override public void randomRoomAssignedSubscriber(String roomId) {

  }
}
