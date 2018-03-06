package com.tribe.app.presentation.utils;

import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.tribelivesdk.game.GameManager;
import javax.inject.Inject;

/**
 * Created by tiago on 05/03/2018.
 */

public class TrophiesManager {

  @Inject GameManager gameManager;
  @Inject User currentUser;

  private static TrophiesManager instance = null;

  public static TrophiesManager getInstance(AndroidApplication application) {
    if (instance == null) {
      instance = new TrophiesManager(application);
    }

    return instance;
  }

  public TrophiesManager(AndroidApplication application) {
    application.getApplicationComponent().inject(this);
  }

  public int friendsCount() {
    int count = 0;

    for (Shortcut shortcut : currentUser.getShortcutList()) {
      if (shortcut.isSingle()) count++;
    }

    return count;
  }
}
