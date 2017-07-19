package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import java.util.List;
import timber.log.Timber;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GameChallengeViewPagerAdapter extends PagerAdapter {

  // @BindView(R.id.txtChallenge) TextViewFont txtChallenge;

  private Context mContext;
  LayoutInflater mLayoutInflater;
  private List<String> items;

  public GameChallengeViewPagerAdapter(Context context, List<String> items) {
    mContext = context;
    this.items = items;
    mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public int getCount() {
    return 3;
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {
    String ok = "";
    if (items != null) {
      ok = items.get(position);
    }
    Timber.e("ok sjdke");
    View itemView = mLayoutInflater.inflate(R.layout.item_game_challenges, container, false);

    TextViewFont txt = (TextViewFont) itemView.findViewById(R.id.txtChallenge);
    txt.setText(ok);
    container.addView(itemView);
    return itemView;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object view) {
    container.removeView((View) view);
  }
}
