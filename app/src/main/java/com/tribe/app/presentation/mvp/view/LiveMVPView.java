package com.tribe.app.presentation.mvp.view;

import android.content.Context;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.domain.entity.User;
import java.util.List;

/**
 * Created by tiago on 01/18/2017.
 */
public interface LiveMVPView extends MVPView {

  Context context();

  void onRecipientInfos(Recipient recipient);

  void renderFriendshipList(List<Friendship> friendshipList);

  void onJoinedRoom(RoomConfiguration roomConfiguration);

  void onJoinRoomError(String message);

  void onRoomFull(String message);

  void onReceivedAnonymousMemberInRoom(List<User> users);

  void onRoomLink(String roomLink);

  void onAddError();

  void onAddSuccess(Friendship friendship);

  void onNamesPostItGame(List<String> nameList);

  void randomRoomAssignedSubscriber(String roomId);

  void fbIdUpdatedSubscriber(User userUpdated);
}
