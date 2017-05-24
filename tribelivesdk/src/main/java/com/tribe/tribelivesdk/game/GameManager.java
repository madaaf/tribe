package com.tribe.tribelivesdk.game;

import android.content.Context;
import com.tribe.tribelivesdk.R;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by tiago on 23/05/2017.
 */

public class GameManager {

  private List<Game> gameList;

  @Inject public GameManager(Context context) {
    gameList = new ArrayList<>();
    gameList.add(new Game(Game.GAME_POST_IT, "Post-It", R.drawable.bg_game_post_it));
  }
}
