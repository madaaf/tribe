package com.tribe.app.presentation.mvp.view.adapter;

import android.content.Context;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.presentation.mvp.view.GameMVPView;
import java.util.List;

/**
 * Created by tiago on 11/12/2017.
 */

public class GameMVPViewAdapter implements GameMVPView {

  @Override public void onUserLeaderboard(List<Score> scoreList, boolean cloud) {

  }

  @Override public void onGameLeaderboard(List<Score> scoreList, boolean cloud, boolean friendsOnly,
      int offset, boolean downwards) {

  }

  @Override public Context context() {
    return null;
  }
}
