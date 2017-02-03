package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.RoomConfiguration;
import java.util.List;

/**
 * Created by tiago on 01/18/2017.
 */
public interface LiveMVPView extends MVPView {

  void onRecipientInfos(Recipient recipient);

  void renderFriendshipList(List<Friendship> friendshipList);

  void onJoinedRoom(RoomConfiguration roomConfiguration);
}
