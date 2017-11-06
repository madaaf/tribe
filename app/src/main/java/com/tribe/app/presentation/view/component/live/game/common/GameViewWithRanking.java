package com.tribe.app.presentation.view.component.live.game.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.HashMap;
import java.util.Map;
import rx.Observable;

/**
 * Created by tiago on 11/06/2017.
 */

public abstract class GameViewWithRanking extends GameView {

  private static final String SCORES_KEY = "scores";
  private static final String CONTEXT_KEY = "context";

  // VARIABLES
  private Map<TribeGuest, RankingStatus> mapStatuses;

  public enum RankingStatus {
    LOST(EmojiParser.getEmoji(":skull:")), PENDING(":timer:");

    private final String emoji;

    RankingStatus(String emoji) {
      this.emoji = emoji;
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
  }

  /**
   * PRIVATE
   */

  protected void setStatus(RankingStatus ranking, String userId) {
    TribeGuest tribeGuest = peerList.get(userId);
    mapStatuses.put(tribeGuest, ranking);
    updateLiveScores();
  }

  protected void updateLiveScores() {

  }

  /**
   * PUBLIC
   */

  @Override
  public void start(Observable<Map<String, TribeGuest>> map) {
    super.start(map);
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
