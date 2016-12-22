package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.User;

import java.util.List;

public interface FriendsMVPView extends LoadDataMVPView {

    void renderContactList(List<User> contactList);
    void successFacebookLogin();
    void errorFacebookLogin();
    void syncDone();
}

