package com.tribe.app.presentation.view.component.live.game.corona;

import android.content.Context;
import android.support.annotation.NonNull;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.tribelivesdk.game.Game;
import java.util.Hashtable;
import timber.log.Timber;

/**
 * Created by tiago on 11/13/2017.
 */

public class GameBattleRoyaleCoronaView extends GameCoronaView {

  // VARIABLES

  // OBSERVABLES
  public GameBattleRoyaleCoronaView(@NonNull Context context, Game game) {
    super(context, game);
  }

  @Override protected void initGameMasterManagerSubscriptions() {
    super.initGameMasterManagerSubscriptions();
    if (gameMasterManager == null) return;
    subscriptionsRoom.add(gameMasterManager.onMessage().subscribe(s -> {
      if (s.contains("#b")) Timber.d("Sending event to Corona : " + s);
      Hashtable<Object, Object> message = new Hashtable<>();
      message.put("name", "gameMaster");
      message.put("string", s);
      coronaView.sendEvent(message);
    }));
  }

  @Override protected void handleCoronaMessage(String event, Hashtable<Object, Object> hashtable) {
    super.handleCoronaMessage(event, hashtable);
    if (event.equals("gameMaster")) {
      String message = hashtable.get("string").toString();
      gameMasterManager.send(message);
    } else if (event.equals("statusesUpdated")) {
      Hashtable<String, Object> statuses = (Hashtable<String, Object>) hashtable.get("statuses");
      for (String key : liveViewsMap.keySet()) {
        LiveStreamView v = liveViewsMap.get(key);
        if (statuses.get(key) != null) {
          v.updateScoreWithEmoji(0, (String) statuses.get(key));
        } else {
          v.updateScoreWithEmoji(0, "");
        }
      }
    }
  }

  /**
   * JSON PAYLOAD
   */

  /**
   * PUBLIC
   */

  @Override public void stop() {
    super.stop();
  }

  /**
   * OBSERVABLES
   */
}
