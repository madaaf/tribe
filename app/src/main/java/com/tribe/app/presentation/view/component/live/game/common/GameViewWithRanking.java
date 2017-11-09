package com.tribe.app.presentation.view.component.live.game.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import rx.Observable;
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

    mapRanking.keySet().forEach(tribeGuest -> {
      if (scores.containsKey(tribeGuest.getId())) {
        mapRanking.put(tribeGuest, scores.get(tribeGuest.getId()));
        mapRankingById.put(tribeGuest.getId(), scores.get(tribeGuest.getId()));
      }
    });

    updateLiveScores();
  }

  private void resetLiveScores() {
    // EUH
  }

  protected void updateLiveScores() {

  }

  protected void addPoint(String userId) {

  }

  /**
   * PUBLIC
   */

  @Override public void start(Observable<Map<String, TribeGuest>> map, String userId) {
    super.start(map, userId);

    subscriptionsSession.add(
        peerMapObservable.debounce(500, TimeUnit.MILLISECONDS).subscribe(mapGuest -> {
          Set<TribeGuest> rankedPlayers = mapRanking.keySet();
          List<TribeGuest> playerList = new ArrayList<>();

          for (TribeGuest guest : peerMap.values()) {
            playerList.add(guest);
          }

          playerList.add(currentUser.asTribeGuest());

          List<TribeGuest> newPlayerList = new ArrayList<>();
          playerList.forEach(tribeGuest -> {
            if (!rankedPlayers.contains(tribeGuest)) newPlayerList.add(tribeGuest);
          });

          List<TribeGuest> leftPlayerList = new ArrayList<>();
          rankedPlayers.forEach(tribeGuest -> {
            if (!playerList.contains(tribeGuest)) leftPlayerList.add(tribeGuest);
          });

          leftPlayerList.forEach(tribeGuest -> {
            mapRanking.remove(tribeGuest);
            mapRankingById.remove(tribeGuest.getId());
            mapStatuses.remove(tribeGuest);
            mapStatusesById.remove(tribeGuest.getId());
          });
        }));

    resetLiveScores();
    updateRanking(null);
  }

  public void stop() {
    super.stop();
  }

  public void dispose() {
    super.dispose();
  }

  /**
   * OBSERVABLE
   */
}
