package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;

import java.util.List;

public interface HomeGridView extends SendTribeView {

    void renderRecipientList(List<Recipient> recipientCollection);
    void updateTribes(List<TribeMessage> tribes);
    void futureUpdateTribes(List<TribeMessage> tribes);
    void updatePendingTribes(List<TribeMessage> pendingTribes);
    void showPendingTribesMenu();
    void scrollToTop();
    void setCurrentTribe(TribeMessage tribe);
    int getNbItems();
}
