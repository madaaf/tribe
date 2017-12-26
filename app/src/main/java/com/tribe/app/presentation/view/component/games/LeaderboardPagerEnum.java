package com.tribe.app.presentation.view.component.games;

import com.tribe.app.R;

/**
 * Created by tiago on 12/12/2017.
 */

public enum LeaderboardPagerEnum {

  FRIENDS(R.layout.item_page_leaderboard, R.string.leaderboards_friends, true), OVERALL(
      R.layout.item_page_leaderboard, R.string.leaderboards_overall, false);

  private int layoutResId, titleResId;
  private boolean friends = false;

  LeaderboardPagerEnum(int layoutResId, int titleResId, boolean friends) {
    this.layoutResId = layoutResId;
    this.friends = friends;
    this.titleResId = titleResId;
  }

  public int getLayoutResId() {
    return layoutResId;
  }

  public boolean isFriends() {
    return friends;
  }

  public int getTitleResId() {
    return titleResId;
  }
}
