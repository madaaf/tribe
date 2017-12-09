package com.tribe.app.presentation.mvp.view;

import android.content.Context;
import com.tribe.app.domain.entity.Score;
import java.util.List;

/**
 * Created by tiago on 01/18/2017.
 */
public interface GameMVPView extends MVPView {

  Context context();

  void onGameLeaderboard(List<Score> scoreList);
}
