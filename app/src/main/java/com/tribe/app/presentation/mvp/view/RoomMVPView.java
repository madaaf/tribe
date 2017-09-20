package com.tribe.app.presentation.mvp.view;

import android.content.Context;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.Shortcut;
import java.util.List;

/**
 * Created by tiago on 11/04/2016.
 */
public interface RoomMVPView extends MVPView {

  Context context();

  void onRoomInfosError(String str);
  void onRoomInfos(Room room);
  void onRoomUpdate(Room room);
  void randomRoomAssignedSubscriber(String roomId);
}
