package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import java.util.List;

public interface HomeGridMVPView extends LoadDataMVPView {

  void onMembershipCreated(Membership membership);

  void onDeepLink(String url);

  void renderRecipientList(List<Recipient> recipientCollection);

  void refreshGrid();

  void onFriendshipUpdated(Friendship friendship);

  void successFacebookLogin();

  void errorFacebookLogin();

  void onSyncDone();
}
