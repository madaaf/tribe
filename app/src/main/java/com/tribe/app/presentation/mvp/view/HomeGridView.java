package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Tribe;

import java.util.List;

public interface HomeGridView extends SendTribeView {

    void renderRecipientList(List<Recipient> recipientCollection);
    void updateTribes(List<Tribe> tribes);
    void futureUpdateTribes(List<Tribe> tribes);
    void updatePendingTribes(List<Tribe> pendingTribes);
    void showPendingTribesMenu();
    void scrollToTop();
    void setCurrentTribe(Tribe tribe);
    int getNbItems();
}
