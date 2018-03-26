package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import butterknife.BindView;
import com.jenzz.appstate.AppStateListener;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.view.adapter.GamePagerAdapter;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewPagerScroller;
import com.tribe.app.presentation.view.widget.game.GameViewPager;
import com.tribe.tribelivesdk.game.Game;
import java.lang.reflect.Field;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Created by madaaflak on 26/03/2018.
 */

public class GamePagerActivity extends GameActivity implements AppStateListener {

  private static final String FROM_AUTH = "FROM_AUTH";

  // VARIABLES
  private UserComponent userComponent;
  private GamePagerAdapter adapter;

  @BindView(R.id.pager) ViewPager viewpager;

  @Inject ScreenUtils screenUtils;

  public static Intent getCallingIntent(Activity activity, boolean fromAuth) {
    Intent intent = new Intent(activity, GamePagerActivity.class);
    intent.putExtra(FROM_AUTH, fromAuth);
    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    adapter = new GamePagerAdapter(this, gameManager.getGames(), screenUtils);
    Timber.e("SOEF SIZE " + gameManager.getGames().size());
    viewpager.setAdapter(adapter);
    changePagerScroller();
    recyclerViewGames.setVisibility(View.GONE);

    onGames.onNext(gameManager.getGames());
  }

  private void changePagerScroller() {
    try {
      Field mScroller = null;
      mScroller = ViewPager.class.getDeclaredField("mScroller");
      mScroller.setAccessible(true);
      ViewPagerScroller scroller =
          new ViewPagerScroller(viewpager.getContext(), new OvershootInterpolator(1.3f));
      mScroller.set(viewpager, scroller);
    } catch (Exception e) {
      Timber.e("error of change scroller " + e);
    }
  }

  @Override public void onAppDidEnterForeground() {

  }

  @Override public void onAppDidEnterBackground() {

  }

  @Override protected void onGameSelected(Game game) {

  }

  @Override protected int getContentView() {
    return R.layout.activity_game_pagerstore;
  }

  /**
   * PUBLIC
   */
  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
  }

  @Override protected void initDependencyInjector() {
    this.userComponent = DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build();

    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }
}
