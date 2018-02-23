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
      LEAVING_ROOM_POPUP, NEVER_ASK_AGAIN_MICRO_PERMISSION, NEVER_ASK_AGAIN_CAMERA_PERMISSION,
      NEVER_ASK_AGAIN_CONTACT_PERMISSION, PERMISSION_CONTACT, NEW_GAME_START, GAME_POST_IT_POPUP,
      FACEBOOK_CONTACT_PERMISSION, REPORT_USER, FIRST_CHALLENGE_POPUP,FIRST_LEAVE_ROOM
  }) public @interface StateKey {
  }

  public static final String LEAVING_ROOM_POPUP = "LEAVING_ROOM_POPUP";
  public static final String FACEBOOK_CONTACT_PERMISSION = "FACEBOOK_CONTACT_PERMISSION";
  public static final String PERMISSION_CONTACT = "PERMISSION_CONTACT";
  public static final String NEW_GAME_START = "NEW_GAME_START";
  public static final String GAME_POST_IT_POPUP = "GAME_POST_IT_POPUP";
  public static final String REPORT_USER = "REPORT_USER";
  public static final String FIRST_CHALLENGE_POPUP = "FIRST_CHALLENGE_POPUP";
  public static final String FIRST_LEAVE_ROOM = "FIRST_LEAVE_ROOM";

  public static final String NEVER_ASK_AGAIN_MICRO_PERMISSION = "NEVER_ASK_AGAIN_MICRO_PERMISSION";
  public static final String NEVER_ASK_AGAIN_CAMERA_PERMISSION = "NEVER_ASK_AGAIN_MICRO_PERMISSION";
  public static final String NEVER_ASK_AGAIN_CONTACT_PERMISSION =
      "NEVER_ASK_AGAIN_CONTACT_PERMISSION";

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

  public void deleteKey(@StateKey String key) {
    if (!StringUtils.isEmpty(key)) {
      //make a copy, update it and save it
      Set<String> newStrSet = new HashSet<String>();
      newStrSet.remove(key);
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
