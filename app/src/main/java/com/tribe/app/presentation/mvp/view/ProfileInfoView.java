package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.User;

public interface ProfileInfoView extends UpdateUserView {

    void loadFacebookInfos(FacebookEntity facebookEntity);
    void userRegistered(User user);
}

