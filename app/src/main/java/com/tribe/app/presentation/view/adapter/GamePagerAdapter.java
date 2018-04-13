package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.BuildConfig;
import com.tribe.app.presentation.view.activity.GameDetailsView;
import com.tribe.tribelivesdk.game.Game;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GamePagerAdapter extends PagerAdapter {

  private Context context;
  private List<Game> gamesList = new ArrayList<>();
  private Map<String, GameDetailsView> viewList = new HashMap<>();

  public GamePagerAdapter(Context context) {
    this.context = context;
    LayoutInflater mLayoutInflater =
        (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public int getCount() {
    return gamesList.size();
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public int getItemPosition(@NonNull Object object) {
    return super.getItemPosition(object);
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {
    Game game = (Game) gamesList.get(position);
    GameDetailsView view = new GameDetailsView(context, game);

    if (container != null) container.addView(view);
    this.viewList.put(game.getId(), view);
    return view;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object view) {
    container.removeView((View) view);
    // this.viewList.remove(view);
  }

  public void setItems(List<Game> gamesList) {
    if (gamesList == null) return;
    List<Game> filtred = new ArrayList<>();

    for (Game g : gamesList) {
      boolean enable = (BuildConfig.VERSION_CODE >= g.getMin_android_version());
      if (g.isIn_home() && enable) filtred.add(g);
    }

    this.gamesList.clear();
    this.gamesList.addAll(filtred);
    notifyDataSetChanged();
  }

  public GameDetailsView getItemAtPosition(int position) {
    if (viewList != null
        && !viewList.isEmpty()
        && gamesList != null
        && !gamesList.isEmpty()
        && gamesList.get(position) != null) {
      return viewList.get(gamesList.get(position).getId());
    }
    return null;
  }

  public List<Game> getGamesList() {
    return gamesList;
  }
}
