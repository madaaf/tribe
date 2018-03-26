package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.List;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GamePagerAdapter extends PagerAdapter {

  private Context context;
  private LayoutInflater mLayoutInflater;

  private GameManager gameManager;
  private int currentPosition;
  private View currentView;
  private List<Game> gamesList;
  private ScreenUtils screenUtils;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Boolean> onBlockOpenInviteView = PublishSubject.create();
  private PublishSubject<Game> onCurrentGame = PublishSubject.create();

  public GamePagerAdapter(Context context, List<Game> gamesList, ScreenUtils screenUtils) {
    this.context = context;
    this.gamesList = gamesList;
    this.screenUtils = screenUtils;
    mLayoutInflater =
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
    //View itemView = mLayoutInflater.inflate(R.layout.item_game_pager, container, false);
    Game game = (Game) gamesList.get(position);
    Test test = new Test(context, game);
    container.addView(test);
    return test;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object view) {
    container.removeView((View) view);
  }
}
