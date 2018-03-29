package com.tribe.app.presentation.view.component.live.game.corona;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.tribelivesdk.game.Game;
import java.util.Hashtable;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by tiago on 11/13/2017.
 */

public class GameBattleRoyaleCoronaView extends GameCoronaView {

  // VARIABLES
  private double ratioWidth = 0.0D, ratioHeight = 0.0D, nativeMapRatio = 0.0D, coronaDisplayWidth =
      0.0D, coronaDisplayHeight = 0.0D, nativeDisplayWidth = 0.0D, nativeDisplayHeight = 0.0D,
      coronaMapWidth = 0.0D, coronaMapHeight = 0.0D, nativeMapWidth = 0.0D, nativeMapHeight = 0.0D;
  private Rect miniMapRect, playerRect;

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
      if (message.contains("#b")) Timber.d("Sending to gameMaster : " + message);
      gameMasterManager.send(message);
    } else if (event.equals("usersPositions")) {
      handleUserPositions(hashtable);
    } else if (event.equals("mapSize")) {
      handleMap(hashtable);
    }
  }

  private void handleUserPositions(Hashtable<Object, Object> hashtable) {
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
        onXYOffsetChanged.onNext(Pair.create((int) (-nativeUserX + (nativeDisplayWidth / 2)),
            (int) (-nativeUserY + (nativeDisplayHeight / 2))));
      }

      if (!userId.equals(currentUser.getId()) && v != null) {
        int location[] = new int[2];
        v.getLocationOnScreen(location);
        playerRect = new Rect(location[0], location[1], location[0] + v.getMeasuredWidth(),
            location[1] + v.getMeasuredHeight());
        boolean isIn = miniMapRect.intersect(playerRect);
        if (isIn) {
          v.setVisibility(View.INVISIBLE);
        } else {
          v.setVisibility(View.VISIBLE);
        }
      }
    }
  }

  private void handleMap(Hashtable<Object, Object> hashtable) {
    coronaDisplayWidth = (Double) hashtable.get("displayWidth");
    coronaDisplayHeight = (Double) hashtable.get("displayHeight");
    coronaMapWidth = (Double) hashtable.get("mapWidth");
    coronaMapHeight = (Double) hashtable.get("mapHeight");

    nativeDisplayWidth = coronaView.getWidth();
    nativeDisplayHeight = coronaView.getHeight();

    ratioWidth = nativeDisplayWidth / coronaDisplayWidth;
    ratioHeight = nativeDisplayHeight / coronaDisplayHeight;
    nativeMapRatio = Math.max(1.0, Math.max(ratioWidth, ratioHeight));

    nativeMapWidth = coronaMapWidth * nativeMapRatio;
    nativeMapHeight = coronaMapHeight * nativeMapRatio;

    onMapSizeChanged.onNext(Pair.create((int) nativeMapWidth, (int) nativeMapHeight));

    double coronaMiniMapWidth = (Double) hashtable.get("miniMapWidth");
    double coronaMiniMapHeight = (Double) hashtable.get("miniMapHeight");
    double nativeMiniMapWidth = coronaMiniMapWidth * nativeMapRatio;
    double nativeMiniMapHeight = coronaMiniMapHeight * nativeMapRatio;
    double miniMapX = (Double) hashtable.get("miniMapX");
    double miniMapY = (Double) hashtable.get("miniMapY");

    int left = (int) ((nativeDisplayWidth * miniMapX) - (nativeMiniMapWidth / 2));
    int top = (int) (nativeDisplayHeight * miniMapY);
    miniMapRect =
        new Rect(left, top, (int) (left + nativeMiniMapWidth), (int) (top + nativeMiniMapHeight));
    Timber.d("miniMapRect : " + miniMapRect);
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
