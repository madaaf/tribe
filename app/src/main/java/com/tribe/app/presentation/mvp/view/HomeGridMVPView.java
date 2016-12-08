package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;

import java.util.List;

public interface HomeGridMVPView extends SendTribeMVPView {

    void onMembershipCreated(Membership membership);
    void onDeepLink(String url);
    void renderRecipientList(List<Recipient> recipientCollection);
    void updateReceivedMessages(List<Message> messageList);
    void updatePendingTribes(List<TribeMessage> pendingTribes);
    void showPendingTribesMenu();
    void scrollToTop();
    int getNbItems();
    void refreshGrid();
    void onFriendshipUpdated(Friendship friendship);
    void successFacebookLogin();
    void errorFacebookLogin();
}
