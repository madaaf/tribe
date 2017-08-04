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
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.lang.reflect.Field;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by madaaflak on 31/07/2017.
 */

public class GameDrawView extends FrameLayout {

  @Inject User user;

  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private GameManager gameManager;
  private GameDrawViewPagerAdapter adapter;
  private boolean gameClosed = false;

  @BindView(R.id.pager) ViewPager viewpager;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Boolean> onBlockOpenInviteView = PublishSubject.create();
  private PublishSubject<Boolean> onNextDraw = PublishSubject.create();
  private PublishSubject<Game> onCurrentGame = PublishSubject.create();
  private PublishSubject<Void> onClearDraw = PublishSubject.create();
  private PublishSubject<List<Float[]>> onDrawing = PublishSubject.create();

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

    changePagerScroller();

    subscriptions.add(adapter.onBlockOpenInviteView().subscribe(onBlockOpenInviteView));
    subscriptions.add(adapter.onNextDraw().subscribe(onNextDraw));
    subscriptions.add(adapter.onCurrentGame().subscribe(onCurrentGame));
    subscriptions.add(adapter.onClearDraw().subscribe(onClearDraw));
    subscriptions.add(adapter.onDrawing().subscribe(onDrawing));

    viewpager.setOnTouchListener((v, event) -> true);
  }

  public void close() {
    gameClosed = true;
  }

  public void setNextGame() {
    new Handler().post(() -> {
      setVisibility(VISIBLE); // MAYBE
      int currentItem;
      if (gameClosed) {
        currentItem = viewpager.getCurrentItem();
        gameClosed = false;
      } else {
        currentItem = (viewpager.getCurrentItem() + 1);
      }
      Timber.w("soef set next game view " + currentItem);
      viewpager.setCurrentItem(currentItem);
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

  public Observable<Boolean> onBlockOpenInviteView() {
    return onBlockOpenInviteView;
  }

  public Observable<Boolean> onNextDraw() {
    return onNextDraw;
  }

  public Observable<Game> onCurrentGame() {
    return onCurrentGame;
  }

  public Observable<Void> onClearDraw() {
    return onClearDraw;
  }

  public Observable<List<Float[]>> onDrawing() {
    return onDrawing;
  }
}
