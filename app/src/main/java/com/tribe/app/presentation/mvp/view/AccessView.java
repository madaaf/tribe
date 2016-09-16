package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.User;

import java.util.List;

/**
 * Created by tiago on 12/09/2016.
 */
public interface AccessView extends View {

    void renderFriendList(List<User> userRealm);
}
