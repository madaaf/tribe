package com.tribe.app.presentation.view.utils;

import android.support.annotation.StringDef;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.preferences.TribeState;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by madaaflak on 14/02/2017.
 */

@Singleton public class StateManager {

  private Preference<Set<String>> tutorialState;

  @StringDef({
      LEAVING_ROOM, DRAGGING_GUEST, DROPPING_GUEST, ENTER_FIRST_LIVE, START_FIRST_LIVE,
      WAINTING_FRIENDS_LIVE, JOIN_FRIEND_LIVE
  }) public @interface StateKey {
  }

  public static final String LEAVING_ROOM = "LEAVING_ROOM";
  public static final String DRAGGING_GUEST = "DRAGGING_GUEST";
  public static final String DROPPING_GUEST = "DROPPING_GUEST";
  public static final String ENTER_FIRST_LIVE = "ENTER_FIRST_LIVE";
  public static final String START_FIRST_LIVE = "START_FIRST_LIVE";
  public static final String WAINTING_FRIENDS_LIVE = "WAINTING_FRIENDS_LIVE";
  public static final String JOIN_FRIEND_LIVE = "JOIN_FRIEND_LIVE";

  @Inject public StateManager(@TribeState Preference<Set<String>> tutorialState) {
    this.tutorialState = tutorialState;
  }

  public void addTutorialKey(@StateKey String key) {
    if (!StringUtils.isEmpty(key)) {
      //make a copy, update it and save it
      Set<String> newStrSet = new HashSet<String>();
      newStrSet.add(key);
      newStrSet.addAll(tutorialState.get());
      tutorialState.set(newStrSet);
    }
  }

  public boolean shouldDisplay(@StateKey String key) {
    boolean contain = false;

    if (tutorialState.get().contains(key)) {
      contain = true;
    }

    return !contain;
  }
}
