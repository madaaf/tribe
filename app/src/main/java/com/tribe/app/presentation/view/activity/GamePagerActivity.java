package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.OnClick;
import com.jenzz.appstate.AppStateListener;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.view.adapter.GamePagerAdapter;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewPagerScroller;
import com.tribe.tribelivesdk.game.Game;
import java.lang.reflect.Field;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Created by madaaflak on 26/03/2018.
 */

public class GamePagerActivity extends GameActivity implements AppStateListener {

  private static final String FROM_AUTH = "FROM_AUTH";
  private final static String DOTS_TAG_MARKER = "DOTS_TAG_MARKER_";

  // VARIABLES
  private UserComponent userComponent;
  private GamePagerAdapter adapter;
  private PageListener pageListener;

  @BindView(R.id.pager) ViewPager viewpager;
  @BindView(R.id.dotsContainer) LinearLayout dotsContainer;

  @Inject ScreenUtils screenUtils;

  public static Intent getCallingIntent(Activity activity, boolean fromAuth) {
    Intent intent = new Intent(activity, GamePagerActivity.class);
    intent.putExtra(FROM_AUTH, fromAuth);
    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    adapter = new GamePagerAdapter(this, gameManager.getGames(), screenUtils);
    initDots(gameManager.getGames().size());
    pageListener = new PageListener(dotsContainer);
    viewpager.addOnPageChangeListener(pageListener);
    viewpager.setAdapter(adapter);
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

  private void initDots(int dotsNbr) {
    int sizeDot = getResources().getDimensionPixelSize(R.dimen.waiting_view_dot_size);
    for (int i = 0; i < dotsNbr; i++) {
      View v = new View(this);
      v.setTag(DOTS_TAG_MARKER + i);
      FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sizeDot * 2, sizeDot);
      lp.setMargins(0, 0, 15, 0);
      lp.gravity = Gravity.CENTER;
      v.setLayoutParams(lp);
      dotsContainer.addView(v);
      if (i == 0) {
        v.setBackgroundResource(R.drawable.shape_oval_white);
        v.setScaleX(1.2f);
        v.setScaleY(1.2f);
      } else {
        v.setBackgroundResource(R.drawable.shape_oval_white50);
        v.setScaleX(1f);
        v.setScaleY(1f);
      }
    }
  }

  public static class PageListener extends ViewPager.SimpleOnPageChangeListener {

    private LinearLayout dotsContainer;
    private int positionViewPager;

    public PageListener(LinearLayout dotsContainer) {
      this.dotsContainer = dotsContainer;
    }

    public int getPositionViewPage() {
      return positionViewPager;
    }

    public void onPageSelected(int position) {
      this.positionViewPager = position;
      positionViewPager = position;
      for (int i = 0; i < dotsContainer.getChildCount(); i++) {
        View v = dotsContainer.getChildAt(i);
        if (v.getTag().toString().startsWith(DOTS_TAG_MARKER + position)) {
          v.setBackgroundResource(R.drawable.shape_oval_white);
          v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).start();
        } else {
          v.setBackgroundResource(R.drawable.shape_oval_white50);
          v.setScaleX(1f);
          v.setScaleY(1f);
        }
      }
    }
  }

  private Game getCurrentGame() {
    return gameManager.getGames().get(pageListener.getPositionViewPage());
  }

  /**
   * ONCLICK
   */

  @OnClick({ R.id.btnFriends, R.id.imgLive, R.id.btnNewMessage }) void onClickHome() {
    navigator.navigateToHome(this);
  }

  @OnClick(R.id.btnLeaderboards) void onClickLeaderboards() {
    navigator.navigateToLeaderboards(this, getCurrentUser());
  }

  @OnClick(R.id.btnMulti) void voidOnClickBtnMulti() {
    navigator.navigateToGameMembers(this, getCurrentGame().getId());
  }

  @OnClick(R.id.btnSingle) void voidOnClickBtnSingle() {
    Bundle bundle = new Bundle();
    bundle.putString(TagManagerUtils.SOURCE, TagManagerUtils.HOME);
    bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.LAUNCHED);
    bundle.putString(TagManagerUtils.NAME, getCurrentGame().getId());
    tagManager.trackEvent(TagManagerUtils.NewGame, bundle);

    navigator.navigateToNewCall(this, LiveActivity.SOURCE_HOME, getCurrentGame().getId());
  }
}
