package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.User;

public interface ProfileInfoView extends LoadDataView {

    void successFacebookLogin();
    void errorFacebookLogin();
    void loadFacebookInfos(FacebookEntity facebookEntity);
    void usernameResult(SearchResult searchResult);
    void userRegistered();
    void goToAccess(User user);
}

