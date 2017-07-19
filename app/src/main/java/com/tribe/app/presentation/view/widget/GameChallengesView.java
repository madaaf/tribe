package com.tribe.app.presentation.view.widget;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GameChallengesView extends FrameLayout {

  private static final int NUM_PAGES = 5;

  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private GameChallengeViewPagerAdapter adapter;

  @BindView(R.id.pager) ViewPager viewpager;

  public GameChallengesView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public GameChallengesView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  private void initView(Context context) {
    this.context = context;
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_game_challenges, this, true);
    unbinder = ButterKnife.bind(this);

    adapter = new GameChallengeViewPagerAdapter(context);
    // Affectation de l'adapter au ViewPager
    viewpager.setAdapter(adapter);
    //viewpager.setPageMargin(12);
    //
/*    viewpager.setClipToPadding(false);
   */
  }

  public void displayView() {
    setVisibility(VISIBLE);
/*    Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_right_to_center);
    startAnimation(anim);*/
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
