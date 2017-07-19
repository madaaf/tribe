package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GameChallengeViewPagerAdapter extends PagerAdapter {

  private Context mContext;
  LayoutInflater mLayoutInflater;

  public GameChallengeViewPagerAdapter(Context context) {
    mContext = context;
    mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public int getCount() {
    return 3;
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {
    View itemView = mLayoutInflater.inflate(R.layout.item_game_challenges, container, false);
    container.addView(itemView);
    return itemView;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object view) {
    container.removeView((View) view);
  }
}
