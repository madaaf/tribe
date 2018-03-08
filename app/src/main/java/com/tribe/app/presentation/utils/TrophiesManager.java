package com.tribe.app.presentation.utils;

import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.preferences.DaysOfUsage;
import com.tribe.app.presentation.utils.preferences.GamesPlayed;
import com.tribe.app.presentation.utils.preferences.MultiplayerSessions;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.Set;
import javax.inject.Inject;

/**
 * Created by tiago on 05/03/2018.
 */

public class TrophiesManager {

  @Inject GameManager gameManager;
  @Inject User currentUser;
  @Inject @MultiplayerSessions Preference<Integer> multiplayerSessions;
  @Inject @DaysOfUsage Preference<Integer> daysOfUsage;
  @Inject @GamesPlayed Preference<Set<String>> gamesPlayed;

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

  public int gamesPlayed() {
    return gamesPlayed.get().size();
  }

  public int daysOfUsage() {
    return daysOfUsage.get();
  }

  public int multiplayerSessions() {
    return multiplayerSessions.get();
  }

  public int bestScoresCount() {
    int count = 0;

    for (Game game : gameManager.getGames()) {
      if (game.getFriendLeader() != null &&
          game.getFriendLeader().getId().equals(currentUser.getId())) {
        count++;
      }
    }

    return count;
  }

  public User getCurrentUser() {
    return currentUser;
  }
}
