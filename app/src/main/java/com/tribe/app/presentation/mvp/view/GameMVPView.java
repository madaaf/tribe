package com.tribe.app.presentation.mvp.view;

import android.content.Context;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.trivia.TriviaQuestions;
import java.util.List;
import java.util.Map;

/**
 * Created by tiago on 01/18/2017.
 */
public interface GameMVPView extends MVPView {

  Context context();

  void onGameLeaderboard(List<Score> scoreList, boolean cloud, boolean friendsOnly, int offset,
      boolean downwards);

  void onUserLeaderboard(List<Score> scoreList, boolean cloud);

  void onFriendsScore(List<Score> scoreList, boolean cloud);

  void onTriviaData(Map<String, List<TriviaQuestions>> map);
}
