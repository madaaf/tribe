package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.tribelivesdk.model.TribeGuest;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GameDrawViewPagerAdapter extends PagerAdapter {
  private static int nbrPage = 1;
  private Context mContext;
  private LayoutInflater mLayoutInflater;

  private User user;
  private String challenge;
  private TribeGuest guest;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Boolean> onBlockOpenInviteView = PublishSubject.create();

  public GameDrawViewPagerAdapter(Context context, User user) {
    mContext = context;
    this.user = user;
    mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public int getCount() {
    return 10;
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {
    View itemView = mLayoutInflater.inflate(R.layout.item_game_draw, container, false);

    container.addView(itemView);
    return itemView;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object view) {
    container.removeView((View) view);
  }
}
