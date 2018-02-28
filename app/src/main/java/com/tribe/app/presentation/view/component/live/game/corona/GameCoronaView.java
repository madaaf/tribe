package com.tribe.app.presentation.view.component.live.game.corona;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.ansca.corona.CoronaView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameView;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeSession;
import java.util.Hashtable;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;

/**
 * Created by tiago on 11/13/2017.
 */

public class GameCoronaView extends GameView {

  @BindView(R.id.coronaView) CoronaView coronaView;
  @BindView(R.id.layoutProgress) FrameLayout layoutProgress;
  @BindView(R.id.viewProgress) View viewProgress;
  @BindView(R.id.cardViewProgress) CardView cardViewProgress;

  // VARIABLES

  public GameCoronaView(@NonNull Context context) {
    super(context);
  }

  public GameCoronaView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void initView(Context context) {
    super.initView(context);

    inflater.inflate(R.layout.view_game_corona, this, true);
    unbinder = ButterKnife.bind(this);
  }

  @Override protected void initWebRTCRoomSubscriptions() {

  }

  @Override protected void takeOverGame() {

  }

  private void startGame() {

  }

  private void runGame() {
    coronaView.init("coronatest/aliens-attack");
    coronaView.setZOrderMediaOverlay(true);
    coronaView.setCoronaEventListener((coronaView, hashtable) -> {
      String event = (String) hashtable.get("event");

      if (event.equals("gameLoaded")) {
        startGame();
      }

      return null;
    });
    coronaView.resume();
  }

  @Override protected void receiveMessage(TribeSession tribeSession, JSONObject jsonObject) {
    super.receiveMessage(tribeSession, jsonObject);

    if (jsonObject.has(game.getId())) {
      try {
        JSONObject message = jsonObject.getJSONObject(game.getId());
        Hashtable<Object, Object> table = new Hashtable();
        table.put("name", "receiveMessage");
        table.put("message", JsonHelper.toHashtable(message));
        table.put("fromUserId", tribeSession.getUserId());
        coronaView.sendEvent(table);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * JSON PAYLOAD
   */

  /**
   * PUBLIC
   */

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, TribeGuest>> mapInvitedObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    super.start(game, mapObservable, mapInvitedObservable, liveViewsObservable, userId);
    runGame();
  }

  @Override public void stop() {
    super.stop();
  }

  @Override public void dispose() {
    super.dispose();
    coronaView.destroy();
  }

  @Override public void setNextGame() {

  }

  /**
   * OBSERVABLES
   */
}
