package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.ErrorLogin;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;

public interface AuthMVPView extends LoadDataMVPView {

  void goToCode(Pin pin);

  void goToHome();

  void goToConnected(User user);

  void loginError(ErrorLogin errorLogin);

  void pinError(ErrorLogin errorLogin);
}

