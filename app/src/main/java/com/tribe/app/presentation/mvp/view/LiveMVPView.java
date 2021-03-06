package com.tribe.app.presentation.mvp.view;

import android.content.Context;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import java.util.List;

/**
 * Created by tiago on 01/18/2017.
 */
public interface LiveMVPView extends MVPView {

  Context context();

  void onRoomInfos(Room room);

  void onRoomInfosError(String message);

  void onRoomFull(String message);

  void onReceivedAnonymousMemberInRoom(List<User> users);

  void randomRoomAssignedSubscriber(String roomId);

  void fbIdUpdatedSubscriber(User userUpdated);

  void onRoomUpdate(Room room);

  void onInvites(List<Invite> invites);

  void onRandomBannedUntil(String date);

  void onDisplayNotificationForNewHighScore();
}
