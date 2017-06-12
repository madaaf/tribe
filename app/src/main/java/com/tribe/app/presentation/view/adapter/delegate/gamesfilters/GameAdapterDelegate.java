package com.tribe.app.presentation.view.adapter.delegate.gamesfilters;

import android.content.Context;
import android.support.annotation.NonNull;
import com.tribe.app.R;
import com.tribe.tribelivesdk.entity.GameFilter;
import com.tribe.tribelivesdk.game.Game;
import java.util.List;

/**
 * Created by tiago on 06/02/17.
 */
public class GameAdapterDelegate extends GamesFiltersAdapterDelegate {

  public GameAdapterDelegate(Context context) {
    super(context);
  }

  @Override public boolean isForViewType(@NonNull List<GameFilter> items, int position) {
    return items.get(position) instanceof Game;
  }

  protected int getLayoutId() {
    return R.layout.item_game;
  }
}
