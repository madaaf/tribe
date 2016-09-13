package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.ErrorLogin;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;

public interface IntroView extends LoadDataView {

    void goToCode(Pin pin);
    void goToHome();
    void goToProfileInfo();
    void goToConnected(User user);
    void loginError(ErrorLogin errorLogin);
}

