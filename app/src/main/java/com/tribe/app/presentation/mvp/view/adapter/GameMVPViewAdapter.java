package com.tribe.app.presentation.mvp.view.adapter;

import android.content.Context;
import com.tribe.app.domain.entity.GameFile;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import com.tribe.app.presentation.mvp.view.GameMVPView;
import com.tribe.tribelivesdk.game.Game;
import java.util.List;
import java.util.Map;

/**
 * Created by tiago on 11/12/2017.
 */

public class GameMVPViewAdapter implements GameMVPView {

  @Override public void onUserLeaderboard(List<Score> scoreList, boolean cloud) {

  }

  @Override public void onGameLeaderboard(List<Score> scoreList, boolean cloud) {

  }

  @Override public Context context() {
    return null;
  }

  @Override public void onFriendsScore(List<Score> scoreList, boolean cloud) {

  }

  @Override public void onTriviaData(Map<String, List<TriviaQuestion>> map) {

  }

  @Override public void onBattleMusicData(Map<String, BattleMusicPlaylist> map) {

  }

  @Override public void onGameList(List<Game> gameList) {

  }

  @Override public void onGameFile(GameFile gameFile) {
  
  }
  
  @Override public void onGameData(List<String> data) {

  }

  @Override public void onUserBestScore(Score score) {

  }
}
