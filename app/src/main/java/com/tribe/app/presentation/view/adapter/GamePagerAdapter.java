package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.presentation.view.activity.GameDetailsView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.List;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GamePagerAdapter extends PagerAdapter {

  private Context context;

  private GameManager gameManager;
  private int currentPosition;
  private View currentView;
  private List<Game> gamesList;
  private ScreenUtils screenUtils;

  private CompositeSubscription subscriptions = new CompositeSubscription();

  public GamePagerAdapter(Context context, List<Game> gamesList, ScreenUtils screenUtils) {
    this.context = context;
    this.gamesList = gamesList;
    this.screenUtils = screenUtils;
    LayoutInflater mLayoutInflater =
        (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    gameManager = GameManager.getInstance(context);
  }

  @Override public int getCount() {
    return gamesList.size();
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {
    Game game = (Game) gamesList.get(position);
    GameDetailsView test = new GameDetailsView(context, game);
    container.addView(test);
    return test;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object view) {
    container.removeView((View) view);
  }
}
