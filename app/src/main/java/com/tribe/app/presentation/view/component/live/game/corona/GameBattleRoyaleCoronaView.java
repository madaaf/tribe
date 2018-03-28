package com.tribe.app.presentation.view.component.live.game.corona;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.tribelivesdk.game.Game;
import java.util.Hashtable;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 11/13/2017.
 */

public class GameBattleRoyaleCoronaView extends GameCoronaView {

  // VARIABLES
  private double ratioWidth = 0.0D, ratioHeight = 0.0D, nativeMapRatio = 0.0D;
  private double coronaDisplayWidth = 0.0D;
  private double coronaDisplayHeight = 0.0D;
  private double nativeDisplayWidth = 0.0D;
  private double nativeDisplayHeight = 0.0D;
  private double coronaMapWidth = 0.0D;
  private double coronaMapHeight = 0.0D;
  private double nativeMapWidth = 0.0D;
  private double nativeMapHeight = 0.0D;

  // OBSERVABLES
  private PublishSubject<Pair<Integer, Integer>> onMapSizeChanged = PublishSubject.create();
  private PublishSubject<Pair<Integer, Integer>> onXYOffsetChanged = PublishSubject.create();

  public GameBattleRoyaleCoronaView(@NonNull Context context, Game game) {
    super(context, game);
  }

  @Override protected void initGameMasterManagerSubscriptions() {
    super.initGameMasterManagerSubscriptions();
    if (gameMasterManager == null) return;
    subscriptionsRoom.add(gameMasterManager.onMessage().subscribe(s -> {
      //Timber.d("Sending event to Corona : " + s);
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
      //Timber.d("Sending to gameMaster : " + message);
      gameMasterManager.send(message);
    } else if (event.equals("usersPositions")) {
      Double x = null;
      Double y = null;
      String userId = null;
      Hashtable<String, Hashtable<String, Object>> data =
          ((Hashtable<String, Hashtable<String, Object>>) hashtable.get("data"));

      for (Hashtable<String, Object> userPositionTable : data.values()) {
        x = (Double) userPositionTable.get("x");
        y = (Double) userPositionTable.get("y");
        userId = userPositionTable.get("userId").toString();

        Double nativeUserX = x * nativeMapWidth;
        Double nativeUserY = y * nativeMapHeight;

        LiveStreamView v = liveViewsMap.get(userId);
        if (v != null) {
          v.updateXYOffset((nativeUserX.intValue() - (v.getMeasuredWidth() >> 1)),
              (nativeUserY.intValue() - (v.getMeasuredHeight() >> 1)));
        }

        if (userId.equals(currentUser.getId())) {
          //Timber.d("nativeUserX : " + nativeUserX + ", nativeUserY : " + nativeUserY);

          onXYOffsetChanged.onNext(Pair.create((int) (-nativeUserX + (nativeDisplayWidth / 2)),
              (int) (-nativeUserY + (nativeDisplayHeight / 2))));
        }
      }
    } else if (event.equals("mapSize")) {
      coronaDisplayWidth = (Double) hashtable.get("displayWidth");
      coronaDisplayHeight = (Double) hashtable.get("displayHeight");
      coronaMapWidth = (Double) hashtable.get("mapWidth");
      coronaMapHeight = (Double) hashtable.get("mapHeight");
      //Timber.d("coronaDisplayWidth : " +
      //    coronaDisplayWidth +
      //    ", coronaDisplayHeight : " +
      //    coronaDisplayHeight +
      //    ", coronaMapWidth : " +
      //    coronaMapWidth +
      //    ", coronaMapHeight : " +
      //    coronaMapHeight);

      nativeDisplayWidth = coronaView.getWidth();
      nativeDisplayHeight = coronaView.getHeight();

      //Timber.d("nativeDisplayWidth : " +
      //    nativeDisplayWidth +
      //    ", nativeDisplayHeight : " +
      //    nativeDisplayHeight);

      ratioWidth = nativeDisplayWidth / coronaDisplayWidth;
      ratioHeight = nativeDisplayHeight / coronaDisplayHeight;
      nativeMapRatio = Math.max(1.0, Math.max(ratioWidth, ratioHeight));

      nativeMapWidth = coronaMapWidth * nativeMapRatio;
      nativeMapHeight = coronaMapHeight * nativeMapRatio;

      //Timber.d("ratioWidth : " +
      //    ratioWidth +
      //    ", ratioHeight : " +
      //    ratioHeight +
      //    ", nativeMapRatio : " +
      //    nativeMapRatio);
      //Timber.d(
      //    "nativeMapWidth : " + nativeMapWidth + ", nativeMapHeight : " + nativeMapHeight);

      onMapSizeChanged.onNext(Pair.create((int) nativeMapWidth, (int) nativeMapHeight));
    }
  }

  /**
   * JSON PAYLOAD
   */

  /**
   * PUBLIC
   */

  @Override public void stop() {
    onMapSizeChanged.onNext(null);
    for (LiveStreamView v : liveViewsMap.values()) v.updateXYOffset(0, 0);
    super.stop();
  }

  /**
   * OBSERVABLES
   */

  public Observable<Pair<Integer, Integer>> onMapSizeChanged() {
    return onMapSizeChanged;
  }

  public Observable<Pair<Integer, Integer>> onXYOffsetChanged() {
    return onXYOffsetChanged;
  }
}
