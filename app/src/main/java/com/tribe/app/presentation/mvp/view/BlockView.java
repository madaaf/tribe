package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Friendship;

import java.util.List;

/**
 * Created by horatiothomas on 8/31/16.
 */
public interface BlockView extends View {

    void friendshipUpdated(Friendship friendship);
    void renderBlockedFriendshipList(List<Friendship>friendshipList);
}
