package com.tribe.app.presentation.mvp.view;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;

public interface IntroView extends LoadDataView {

    void goToCode(Pin pin);
    void goToHome();
    void goToConnected();
}

