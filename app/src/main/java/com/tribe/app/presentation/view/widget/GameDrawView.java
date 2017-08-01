package com.tribe.app.presentation.view.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ViewPagerScroller;
import com.tribe.tribelivesdk.game.GameManager;
import java.lang.reflect.Field;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Created by madaaflak on 31/07/2017.
 */

public class GameDrawView extends FrameLayout {
  private static int pos = 0;

  @Inject User user;

  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private GameManager gameManager;
  private GameDrawViewPagerAdapter adapter;

  @BindView(R.id.pager) ViewPager viewpager;

  public GameDrawView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public GameDrawView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  private void initView(Context context) {
    this.context = context;
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_game_draw, this, true);
    unbinder = ButterKnife.bind(this);
    gameManager = GameManager.getInstance(getContext());

    adapter = new GameDrawViewPagerAdapter(context, user);
    viewpager.setAdapter(adapter);

    viewpager.setOnTouchListener((v, event) -> {
      return true;
    });
    changePagerScroller();
  }

  public void setNextGame() {
    setVisibility(VISIBLE); // MAYBE
    new Handler().post(() -> {
      pos++;
      viewpager.setCurrentItem(viewpager.getCurrentItem() + 1);
      Timber.e("soef set next game view " + pos);
    });
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

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }
}
