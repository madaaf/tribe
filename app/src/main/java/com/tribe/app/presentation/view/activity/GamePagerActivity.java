package com.tribe.app.presentation.view.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.jenzz.appstate.AppStateListener;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.view.NotifView;
import com.tribe.app.presentation.view.NotificationModel;
import com.tribe.app.presentation.view.adapter.GamePagerAdapter;
import com.tribe.app.presentation.view.adapter.interfaces.HomeAdapterInterface;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.popup.PopupManager;
import com.tribe.app.presentation.view.popup.listener.PopupDigestListener;
import com.tribe.app.presentation.view.popup.view.PopupDigest;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewPagerScroller;
import com.tribe.app.presentation.view.widget.PulseLayout;
import com.tribe.tribelivesdk.game.Game;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.inject.Inject;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by madaaflak on 26/03/2018.
 */

public class GamePagerActivity extends GameActivity implements AppStateListener {

  private static final int DURATION = 400;
  private static final int DURATION_MOVING = 2500;
  private static final String FROM_AUTH = "FROM_AUTH";
  private final static String DOTS_TAG_MARKER = "DOTS_TAG_MARKER_";

  // VARIABLES
  private UserComponent userComponent;
  private GamePagerAdapter adapter;
  private PageListener pageListener;
  private Map<String, ValueAnimator> mapAnimator;

  @BindView(R.id.pager) ViewPager viewpager;
  @BindView(R.id.dotsContainer) LinearLayout dotsContainer;
  @BindView(R.id.imgAnimation1) ImageView imgAnimation1;
  @BindView(R.id.imgAnimation2) ImageView imgAnimation2;
  @BindView(R.id.imgAnimation3) ImageView imgAnimation3;
  @BindView(R.id.test) ImageView test;


  @BindView(R.id.layoutPulse) PulseLayout layoutPulse;
  @BindView(R.id.layoutCall) FrameLayout layoutCall;
  @BindView(R.id.btnFriends) ImageView btnFriends;
  @BindView(R.id.btnNewMessage) ImageView btnNewMessage;

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
    pageListener = new PageListener(dotsContainer, this);
    viewpager.addOnPageChangeListener(pageListener);
    viewpager.setAdapter(adapter);
    recyclerViewGames.setVisibility(View.GONE);

    onGames.onNext(gameManager.getGames());

    if (gameManager.getGames() != null) {
      initUI();
    } else {
      gameMVPViewAdapter = new GameMVPViewAdapter() {
        @Override public Context context() {
          return GamePagerActivity.this;
        }

        @Override public void onGameList(List<Game> gameList) {
          gameManager.addGames(gameList);
          onGames.onNext(gameList);
          initUI();
        }
      };

      gamePresenter.getGames();
    }
    /*subscriptions.add(Observable.timer(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          showImgAnimations();
        }));*/

  }

  private void initUI() {
    mapAnimator = new HashMap<>();

    for (int i = 0; i < getCurrentGame().getAnimation_icons().size(); i++) {
      String url = getCurrentGame().getAnimation_icons().get(i);
      ImageView imageView = null;

      if (i == 0) {
        imageView = imgAnimation1;
      } else if (i == 1) {
        imageView = imgAnimation2;
      } else if (i == 2) {
        imageView = imgAnimation3;
      }

      Glide.with(this).load(url).into(imageView);
    }
    animateImg(imgAnimation1, true);
    animateImg(imgAnimation1, false);
    animateImg(imgAnimation2, true);
    animateImg(imgAnimation2, false);
    animateImg(imgAnimation3, true);
    animateImg(imgAnimation3, false);
  }

  private void showImgAnimations() {
    imgAnimation1.animate()
        .scaleX(1)
        .scaleY(1)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(DURATION)
        .start();
    imgAnimation2.animate()
        .scaleX(1)
        .scaleY(1)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(DURATION)
        .start();
    imgAnimation3.animate()
        .scaleX(1)
        .scaleY(1)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(DURATION)
        .start();
  }

  private void animateImg(ImageView imgAnimation, boolean isX) {
    int rdm = new Random().nextInt(50) - 25;

    ValueAnimator animator = mapAnimator.get(imgAnimation.getId());
    if (animator != null) {
      animator.cancel();
    }

    animator = ValueAnimator.ofInt(0, screenUtils.dpToPx(rdm));
    animator.setDuration(DURATION_MOVING);
    animator.setRepeatCount(ValueAnimator.INFINITE);
    animator.setRepeatMode(ValueAnimator.REVERSE);
    animator.addUpdateListener(animation -> {
      int translation = (int) animation.getAnimatedValue();
      if (isX) {
        imgAnimation.setTranslationX(translation);
      } else {
        imgAnimation.setTranslationY(translation);
      }
    });
    animator.start();
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
    for (ValueAnimator animator : mapAnimator.values()) animator.cancel();
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

  public class PageListener extends ViewPager.SimpleOnPageChangeListener {

    private LinearLayout dotsContainer;
    private int positionViewPager;
    private Context context;

    private void setAnim(View v, float positionOffset, float xTrans, float yTrans) {
      if (positionOffset != 0f) {
        v.clearAnimation();
        v.setAlpha(1 - positionOffset);
        //  v.setTranslationX(xTrans);
        //  v.setTranslationY(yTrans);
        v.setScaleX(1 + positionOffset);
        v.setScaleY(1 + positionOffset);
      } else {
        initUI();
        resetView(imgAnimation1);
        resetView(imgAnimation2);
        resetView(imgAnimation3);
      }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      super.onPageScrolled(position, positionOffset, positionOffsetPixels);

      setAnim(imgAnimation3, positionOffset, positionOffset * 200, positionOffset * 200);
      setAnim(imgAnimation2, positionOffset, positionOffset * 200, (1 - positionOffset) * 200);
      setAnim(imgAnimation1, positionOffset, (1 - positionOffset) * 200,
          (1 - positionOffset) * 200);
      Timber.e("SOEF onPageScrolled : " + positionOffset + " " + positionOffsetPixels);
    }

    public PageListener(LinearLayout dotsContainer, Context context) {
      this.dotsContainer = dotsContainer;
      this.context = context;
    }

    public int getPositionViewPage() {
      return positionViewPager;
    }

    private void resetView(View v) {
      v.clearAnimation();
      v.setTranslationX(0);
      v.setTranslationY(0);
      v.setScaleX(1);
      v.setScaleY(1);
      v.setAlpha(1);
      //v.animate().scaleX(1).scaleY(1).alpha(1).setDuration(DURATION).start();
    }

    public void onPageSelected(int position) {
      Timber.w("SOEF onPageSelected : " + position);

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
