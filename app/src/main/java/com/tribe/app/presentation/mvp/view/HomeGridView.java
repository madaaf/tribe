package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Tribe;

import java.util.List;
import java.util.Map;

public interface HomeGridView extends SendTribeView {

    void renderFriendshipList(List<Friendship> friendCollection);
    void updateTribes(Map<Friendship, List<Tribe>> tribes);
    void scrollToTop();
    void setCurrentTribe(Tribe tribe);
    int getNbItems();
}
