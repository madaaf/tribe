package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.presentation.view.component.games.LeaderboardPage;
import com.tribe.app.presentation.view.component.games.LeaderboardPagerEnum;
import com.tribe.tribelivesdk.game.Game;
import rx.Observable;

public class LeaderboardPagerAdapter extends PagerAdapter {

  private Context context;
  private Game selectedGame;
  private Observable<Score> onViewCardClickObs;

  public LeaderboardPagerAdapter(Context context, Game selectedGame, Observable<Score> onViewCardClickObs) {
    this.context = context;
    this.selectedGame = selectedGame;
    this.onViewCardClickObs = onViewCardClickObs;
  }

  @Override public Object instantiateItem(ViewGroup collection, int position) {
    LeaderboardPagerEnum leaderboardPagerEnum = LeaderboardPagerEnum.values()[position];
    LeaderboardPage leaderboardPage =
        new LeaderboardPage(context, leaderboardPagerEnum.isFriends(), selectedGame);
    leaderboardPage.initViewCardClickObservable(onViewCardClickObs);
    collection.addView(leaderboardPage);
    return leaderboardPage;
  }

  @Override public void destroyItem(ViewGroup collection, int position, Object view) {
    collection.removeView((View) view);
  }

  @Override public int getCount() {
    return LeaderboardPagerEnum.values().length;
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public CharSequence getPageTitle(int position) {
    LeaderboardPagerEnum leaderboardPagerEnum = LeaderboardPagerEnum.values()[position];
    return context.getString(leaderboardPagerEnum.getTitleResId());
  }
}