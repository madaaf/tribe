package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Friendship;

import java.util.List;

public interface HomeGridView extends LoadDataView {

    void renderFriendshipList(List<Friendship> friendCollection);

    void scrollToTop();
}
