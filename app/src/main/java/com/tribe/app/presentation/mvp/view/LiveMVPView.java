package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Friendship;

import java.util.List;

/**
 * Created by tiago on 01/18/2017.
 */
public interface LiveMVPView extends MVPView {

    void renderFriendshipList(List<Friendship> friendshipList);
}
