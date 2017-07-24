package com.tribe.app.presentation.view.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
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
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.tribelivesdk.game.GameChallenge;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GameChallengesView extends FrameLayout {

  @Inject User user;

  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private GameChallengeViewPagerAdapter adapter;
  private GameChallenge gameChallenge;

  @BindView(R.id.pager) ViewPager viewpager;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<GameChallenge> onNextChallenge = PublishSubject.create();

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
    Timber.e("init GameChallengeViewPagerAdapter");
    adapter = new GameChallengeViewPagerAdapter(context, user);
    viewpager.setAdapter(adapter);

    viewpager.setOnTouchListener((v, event) -> true);

    subscriptions.add(adapter.onNextChallenge().subscribe(onNextChallenge));
    //viewpager.setPageMargin(12);
    // viewpager.setClipToPadding(false);

  }

  private static int pos = 0;

  public void setNextChallenge() {
    new Handler().post(() -> {
      pos++;
      viewpager.setCurrentItem(pos, true);
      Timber.e("soef set next challenge " + pos);
    });
  }

  public void setGameChallenge(GameChallenge gameChallenge) {
    Timber.e("soef setGameChallenge visisble");
    setVisibility(VISIBLE);
    this.gameChallenge = gameChallenge;
    adapter.setGameChallenge(gameChallenge);
    adapter.notifyDataSetChanged();
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

  public Observable<GameChallenge> onNextChallenge() {
    return onNextChallenge;
  }
}
