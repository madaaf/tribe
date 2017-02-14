package com.tribe.app.presentation.view.utils;

import android.support.annotation.StringDef;
import android.util.Log;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.presentation.utils.preferences.TribeState;
import java.util.Set;
import javax.inject.Inject;

/**
 * Created by madaaflak on 14/02/2017.
 */

public class StateManager {

  private Preference<Set<String>> tutorialState;

  @StringDef({ LEAVING_ROOM, DRAGGING_GUEST }) public @interface StateKey {
  }

  public static final String LEAVING_ROOM = "LEAVING_ROOM";
  public static final String DRAGGING_GUEST = "DRAGGING_GUEST";
  public static final String ENTER_FIRST_LIVE = "ENTER_FIRST_LIVE";
  public static final String START_FIRST_LIVE = "START_FIRST_LIVE";
  public static final String WAINTING_FRIENDS_JOIN_LIVE = "WAINTING_FRIENDS_JOIN_LIVE";

  @Inject public StateManager(@TribeState Preference<Set<String>> tutorialState) {
    this.tutorialState = tutorialState;
  }

  public void addTutorialKey(@StateManager.StateKey String key) {
    Set<String> tut = tutorialState.get();
    tut.add(key);
    tutorialState.set(tut);
    Log.d("ok", "ok");
  }

  public boolean shouldDisplay(@StateManager.StateKey String key) {
    boolean contain = false;

    if (tutorialState.get().contains(key)) {
      contain = true;
    }

    return !contain;
  }
}
