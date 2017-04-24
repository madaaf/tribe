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
      LEAVING_ROOM_POPUP, DRAG_FRIEND_POPUP, BUZZ_FRIEND_POPUP, NEVER_ASK_AGAIN_MICRO_PERMISSION,
      NEVER_ASK_AGAIN_CAMERA_PERMISSION
  }) public @interface StateKey {
  }

  public static final String LEAVING_ROOM_POPUP = "LEAVING_ROOM_POPUP";
  public static final String DRAG_FRIEND_POPUP = "DRAG_FRIEND_POPUP";
  public static final String BUZZ_FRIEND_POPUP = "BUZZ_FRIEND_POPUP";

  public static final String NEVER_ASK_AGAIN_MICRO_PERMISSION = "NEVER_ASK_AGAIN_MICRO_PERMISSION";
  public static final String NEVER_ASK_AGAIN_CAMERA_PERMISSION = "NEVER_ASK_AGAIN_MICRO_PERMISSION";

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
