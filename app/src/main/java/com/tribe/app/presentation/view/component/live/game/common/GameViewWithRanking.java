package com.tribe.app.presentation.view.component.live.game.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.tribelivesdk.core.WebRTCRoom;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/06/2017.
 */

public abstract class GameViewWithRanking extends GameView {

  private static final String SCORES_KEY = "scores";
  private static final String CONTEXT_KEY = "context";

  // VARIABLES
  protected Map<TribeGuest, RankingStatus> mapStatuses;
  protected Map<String, RankingStatus> mapStatusesById;
  protected Map<TribeGuest, Integer> mapRanking;
  protected Map<String, Integer> mapRankingById;
  protected String previousSortedHash;

  // OBSERVABLES
  protected CompositeSubscription subscriptionsSession = new CompositeSubscription();

  public enum RankingStatus {
    LOST(":skull:"), PENDING(":timer:");

    private final String emoji;

    RankingStatus(String emojiCode) {
      this.emoji = EmojiParser.getEmoji(emojiCode);
    }

    public String getEmoji() {
      return emoji;
    }
  }

  public GameViewWithRanking(@NonNull Context context) {
    super(context);
  }

  public GameViewWithRanking(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  protected void initView(Context context) {
    super.initView(context);
    mapStatuses = new HashMap<>();
    mapStatusesById = new HashMap<>();
    mapRanking = new HashMap<>();
    mapRankingById = new HashMap<>();
  }

  /**
   * PRIVATE
   */

  @Override protected void initWebRTCRoomSubscriptions() {
    subscriptionsRoom.add(webRTCRoom.onGameMessage()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonObject -> receiveMessage(jsonObject)));
  }

  private void receiveMessage(JSONObject jsonObject) {
    if (jsonObject.has(game.getId())) {
      try {
        JSONObject message = jsonObject.getJSONObject(game.getId());
        if (message.has(CONTEXT_KEY)) {
          String contextKey = message.getString(CONTEXT_KEY);
          if (contextKey.equals(SCORES_KEY)) {
            Map<String, Integer> scores =
                new Gson().fromJson(String.valueOf(message.getJSONObject(SCORES_KEY)),
                    new TypeToken<HashMap<String, Integer>>() {
                    }.getType());
            updateRanking(scores);
          }
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  protected void setStatus(RankingStatus ranking, String userId) {
    TribeGuest tribeGuest = peerMap.get(userId);
    mapStatuses.put(tribeGuest, ranking);
    mapStatusesById.put(userId, ranking);
    updateLiveScores();
  }

  protected void resetStatuses() {
    mapStatuses.clear();
    mapStatusesById.clear();
    updateLiveScores();
  }

  protected void updateRanking(Map<String, Integer> scores) {
    if (scores == null) return;

    for (String tribeGuestId : scores.keySet()) {
      if (scores.containsKey(tribeGuestId) && peerMap.containsKey(tribeGuestId)) {
        mapRanking.put(peerMap.get(tribeGuestId), scores.get(tribeGuestId));
        mapRankingById.put(tribeGuestId, scores.get(tribeGuestId));
      }
    }

    game.getContextMap().put(SCORES_KEY, scores);

    updateLiveScores();
  }

  protected void resetLiveScores() {
    for (LiveStreamView view : liveViewsMap.values()) {
      view.updateScoreWithEmoji(0, null);
    }
  }

  protected void updateLiveScores() {
    Collection<Integer> rankings = mapRanking.values();
    int maxRanking = rankings != null && rankings.size() > 0 ? Collections.max(rankings) : 0;
    int minRanking = rankings != null && rankings.size() > 0 ? Collections.min(rankings) : 0;

    for (String userId : liveViewsMap.keySet()) {
      LiveStreamView liveStreamView = liveViewsMap.get(userId);
      if (mapRankingById.get(userId) != null) {
        int newScore = mapRankingById.get(userId);
        int oldScore = liveStreamView.getScore();
        String statusText =
            mapStatusesById.containsKey(userId) ? mapStatusesById.get(userId).getEmoji() : "";
        String emojiText =
            (newScore > 0 && newScore == maxRanking ? EmojiParser.demojizedText(":crown:")
                : ((newScore == minRanking ? EmojiParser.demojizedText(":poop:") : "")));
        liveStreamView.updateScoreWithEmoji(newScore, statusText + emojiText);
        if (newScore != oldScore) liveStreamView.bounceView();
      } else {
        liveStreamView.updateScoreWithEmoji(0, null);
      }
    }
  }

  protected void setScore(String userId, int score, boolean shouldBroadcast) {
    if (shouldBroadcast) {
      Map<String, Integer> scores = (Map<String, Integer>) game.getContextMap().get(SCORES_KEY);
      scores.put(userId, score);
      sendScore(SCORES_KEY, game.getContextMap());
      return;
    }

    TribeGuest tribeGuest = null;
    for (TribeGuest trg : mapRanking.keySet()) {
      if (trg.getId().equals(userId)) {
        tribeGuest = trg;
      }
    }

    if (tribeGuest != null) {
      mapRanking.put(tribeGuest, score);
      mapRankingById.put(tribeGuest.getId(), score);
    }
  }

  protected void addPoints(int points, String userId, boolean shouldBroadcast) {
    if (game == null) return;

    if (shouldBroadcast) {
      Map<String, Integer> scores = (Map<String, Integer>) game.getContextMap().get(SCORES_KEY);
      int score = scores.get(userId) != null ? scores.get(userId) : 0;
      score++;
      scores.put(userId, score);
      sendScore(SCORES_KEY, game.getContextMap());
      return;
    }

    TribeGuest tribeGuest = null;
    for (TribeGuest trg : mapRanking.keySet()) {
      if (trg.getId().equals(userId)) {
        tribeGuest = trg;
      }
    }

    if (tribeGuest != null) {
      int newValue = 1;
      if (mapRanking.get(tribeGuest) != null) {
        newValue = mapRanking.get(tribeGuest) + points;
      }

      mapRanking.put(tribeGuest, newValue);
      mapRankingById.put(tribeGuest.getId(), newValue);
    }
  }

  protected void sendScore(String key, Map<String, Object> contextMap) {
    webRTCRoom.sendToPeers(getContextPayload((Map<String, Integer>) contextMap.get(key)), false);

    JSONObject jsonObject = getCompleteContextPayload(key, contextMap);
    webRTCRoom.sendToPeers(jsonObject, true);
    receiveMessage(jsonObject);
  }

  /**
   * JSON PAYLOADS
   */

  private JSONObject getContextPayload(Map<String, Integer> context) {
    JSONObject obj = new JSONObject();
    JSONObject gameContext = new JSONObject();
    JsonUtils.jsonPut(gameContext, Game.CONTEXT, computeScoreMap(context, true));
    JsonUtils.jsonPut(gameContext, Game.ID, game.getId());
    JsonUtils.jsonPut(obj, WebRTCRoom.MESSAGE_GAME, gameContext);
    return obj;
  }

  private JSONObject getCompleteContextPayload(String key, Map<String, Object> context) {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    Map<String, Integer> scoreMap = (Map<String, Integer>) context.get(key);
    JsonUtils.jsonPut(game, key, computeScoreMap(scoreMap, false));
    JsonUtils.jsonPut(game, CONTEXT_KEY, key);
    JsonUtils.jsonPut(obj, this.game.getId(), game);
    return obj;
  }

  private JSONObject computeScoreMap(Map<String, Integer> scoreMap, boolean includeId) {
    JSONObject json = new JSONObject();
    JSONObject scoresJson = new JSONObject();

    try {
      for (String scoreKeyId : scoreMap.keySet()) {
        scoresJson.put(scoreKeyId, scoreMap.get(scoreKeyId));
      }

      json.put(SCORES_KEY, scoresJson);
    } catch (JSONException ex) {
      ex.printStackTrace();
    }

    if (includeId) {
      return json;
    } else {
      return scoresJson;
    }
  }

  /**
   * PUBLIC
   */

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> map,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    super.start(game, map, liveViewsObservable, userId);

    game.getContextMap().put(SCORES_KEY, new HashMap<String, Integer>());

    subscriptionsSession.add(peerMapObservable.debounce(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(mapGuest -> {
          Set<TribeGuest> rankedPlayers = mapRanking.keySet();
          List<TribeGuest> playerList = new ArrayList<>();

          for (TribeGuest guest : peerMap.values()) {
            playerList.add(guest);
          }

          playerList.add(currentUser.asTribeGuest());

          List<TribeGuest> newPlayerList = new ArrayList<>();
          for (TribeGuest tribeGuest : playerList) {
            if (!rankedPlayers.contains(tribeGuest)) newPlayerList.add(tribeGuest);
          }

          List<TribeGuest> leftPlayerList = new ArrayList<>();
          for (TribeGuest tribeGuest : rankedPlayers) {
            if (!playerList.contains(tribeGuest)) leftPlayerList.add(tribeGuest);
          }

          for (TribeGuest tribeGuest : leftPlayerList) {
            mapRanking.remove(tribeGuest);
            mapRankingById.remove(tribeGuest.getId());
            mapStatuses.remove(tribeGuest);
            mapStatusesById.remove(tribeGuest.getId());
          }

          if (game.getContextMap().get(SCORES_KEY) != null) {
            sendScore(SCORES_KEY, game.getContextMap());
          }
        }));

    resetLiveScores();
    updateRanking(null);
  }

  public void resetScores(boolean shouldSendGameOver) {
    Map<String, Object> map = new HashMap<>();
    Map<String, Integer> newScoresMap = new HashMap<>();

    for (String id : mapRankingById.keySet()) {
      newScoresMap.put(id, 0);
    }

    map.put(SCORES_KEY, newScoresMap);
    sendScore(SCORES_KEY, map);
    updateLiveScores();
  }

  public void stop() {
    super.stop();
  }

  public void dispose() {
    super.dispose();
    subscriptionsSession.unsubscribe();
  }

  /**
   * OBSERVABLE
   */
}
