package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.adapter.GamesFiltersAdapter;
import com.tribe.app.presentation.view.adapter.manager.GamesFiltersLayoutManager;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.tribelivesdk.entity.GameFilter;
import com.tribe.tribelivesdk.filters.Filter;
import com.tribe.tribelivesdk.filters.lut3d.FilterManager;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveControlsView extends FrameLayout {

  private static final int MAX_DURATION_LAYOUT_CONTROLS = 5;
  private static final int DURATION_GAMES_FILTERS = 300;
  private static final int DURATION_PARAM = 450;
  private static final float OVERSHOOT_LIGHT = 0.75f;

  @Inject ScreenUtils screenUtils;

  @Inject StateManager stateManager;

  @BindView(R.id.btnInviteLive) View btnInviteLive;

  @BindView(R.id.btnNotify) View btnNotify;

  @BindView(R.id.btnCameraOn) View btnCameraOn;

  @BindView(R.id.btnCameraOff) View btnCameraOff;

  @BindView(R.id.layoutFilter) ViewGroup layoutFilter;

  @BindView(R.id.btnFilter) ImageView btnFilter;

  @BindView(R.id.imgTriangleCloseFilters) ImageView imgTriangleCloseFilters;

  @BindView(R.id.layoutGame) ViewGroup layoutGame;

  @BindView(R.id.btnNewGame) ImageView btnNewGame;

  @BindView(R.id.imgTriangleCloseGames) ImageView imgTriangleCloseGames;

  @BindView(R.id.btnExpand) ImageView btnExpand;

  @BindView(R.id.btnOrientationCamera) View btnOrientationCamera;

  @BindView(R.id.btnMicro) ImageView btnMicro;

  @BindView(R.id.layoutContainerParamLive) FrameLayout layoutContainerParamLive;

  @BindView(R.id.layoutContainerParamExtendedLive) LinearLayout layoutContainerParamExtendedLive;

  @BindViews({
      R.id.btnCameraOn, R.id.btnCameraOff, R.id.btnOrientationCamera, R.id.btnMicro, R.id.btnExpand
  }) List<View> viewToHideFilters;

  @BindViews({
      R.id.btnInviteLive, R.id.btnExpand
  }) List<View> viewToHideGames;

  @BindView(R.id.btnLeave) ImageView btnLeave;

  @BindView(R.id.recyclerViewFilters) RecyclerView recyclerViewFilters;

  @BindView(R.id.recyclerViewGames) RecyclerView recyclerViewGames;

  // VARIABLES
  private Unbinder unbinder;
  private boolean cameraEnabled = true, microEnabled = true, isParamExpanded = false, filtersOn =
      false, gamesOn = false;
  private float xTranslation;
  private GameManager gameManager;
  private FilterManager filterManager;
  private GamesFiltersLayoutManager filtersLayoutManager;
  private GamesFiltersLayoutManager gamesLayoutManager;
  private List<GameFilter> filterList;
  private GamesFiltersAdapter filtersAdapter;
  private List<GameFilter> gameList;
  private GamesFiltersAdapter gamesAdapter;
  private TransitionDrawable btnFilterTD;
  private int[] btnFilterLocation;
  private TransitionDrawable btnNewGameTD;
  private ImageView currentGameView;

  // RESOURCES
  private int sizeGameFilter;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onOpenInvite = PublishSubject.create();
  private PublishSubject<Void> onClickCameraOrientation = PublishSubject.create();
  private PublishSubject<Boolean> onClickMicro = PublishSubject.create();
  private PublishSubject<Boolean> onClickParamExpand = PublishSubject.create();
  private PublishSubject<Void> onClickCameraEnable = PublishSubject.create();
  private PublishSubject<Void> onClickCameraDisable = PublishSubject.create();
  private PublishSubject<Void> onClickNotify = PublishSubject.create();
  private PublishSubject<Void> onNotifyAnimationDone = PublishSubject.create();
  private PublishSubject<Filter> onClickFilter = PublishSubject.create();
  private PublishSubject<Game> onStartGame = PublishSubject.create();
  private PublishSubject<Void> onLeave = PublishSubject.create();
  private PublishSubject<Game> onGameOptions = PublishSubject.create();
  private Subscription timerSubscription;

  public LiveControlsView(Context context) {
    super(context);
    init();
  }

  public LiveControlsView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveControlsView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();
    if (timerSubscription != null) {
      timerSubscription.unsubscribe();
      timerSubscription = null;
    }
    super.onDetachedFromWindow();
  }

  private void init() {
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_controls, this);
    unbinder = ButterKnife.bind(this);

    gameManager = GameManager.getInstance(getContext());
    btnFilterLocation = new int[2];

    setBackground(null);
    initResources();
    initUI();
    initFilters();
    initGames();
  }

  private void initResources() {
    sizeGameFilter =
        getContext().getResources().getDimensionPixelSize(R.dimen.filter_game_size_with_border);
  }

  private void initUI() {
    xTranslation = getResources().getDimension(R.dimen.nav_icon_size) + screenUtils.dpToPx(10);
    btnNotify.setEnabled(false);

    btnFilterTD = (TransitionDrawable) ContextCompat.getDrawable(getContext(),
        R.drawable.picto_filter_live_transition);
    btnFilter.setImageDrawable(btnFilterTD);
    btnFilter.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            btnFilter.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            btnFilter.getLocationInWindow(btnFilterLocation);
          }
        });

    btnNewGameTD = (TransitionDrawable) ContextCompat.getDrawable(getContext(),
        R.drawable.picto_game_live_transition);
    btnNewGame.setImageDrawable(btnNewGameTD);
  }

  private void initFilters() {
    filterManager = FilterManager.getInstance(getContext());

    filterList = new ArrayList<>();
    filtersLayoutManager = new GamesFiltersLayoutManager(getContext());
    recyclerViewFilters.setLayoutManager(filtersLayoutManager);
    recyclerViewFilters.setItemAnimator(null);

    filtersAdapter = new GamesFiltersAdapter(getContext());
    filterList.addAll(filterManager.getFilterList());
    filtersAdapter.setItems(filterList);

    recyclerViewFilters.setAdapter(filtersAdapter);
    recyclerViewFilters.getRecycledViewPool().setMaxRecycledViews(0, 50);

    recyclerViewFilters.setTranslationY(screenUtils.getHeightPx() >> 1);

    subscriptions.add(filtersAdapter.onClick()
        .map(view -> (Filter) filtersAdapter.getItemAtPosition(
            recyclerViewFilters.getChildLayoutPosition(view)))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(filter -> {
          filtersAdapter.updateSelected(filter);
          filterManager.setCurrentFilter(filter);
        }));
  }

  private void initGames() {
    gameManager = GameManager.getInstance(getContext());

    gameList = new ArrayList<>();
    gamesLayoutManager = new GamesFiltersLayoutManager(getContext());
    recyclerViewGames.setLayoutManager(gamesLayoutManager);
    recyclerViewGames.setItemAnimator(null);

    gamesAdapter = new GamesFiltersAdapter(getContext());
    gameList.addAll(gameManager.getGames());
    gamesAdapter.setItems(gameList);

    recyclerViewGames.setAdapter(gamesAdapter);
    recyclerViewGames.getRecycledViewPool().setMaxRecycledViews(0, 50);

    recyclerViewGames.setTranslationY(screenUtils.getHeightPx() >> 1);

    subscriptions.add(gamesAdapter.onClick()
        .map(view -> {
          Game game =
              (Game) gamesAdapter.getItemAtPosition(recyclerViewGames.getChildLayoutPosition(view));
          return new Pair<>(view, game);
        })
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(pairViewGame -> {
          gamesAdapter.updateSelected(pairViewGame.second);
          gameManager.setCurrentGame(pairViewGame.second);
          onStartGame.onNext(pairViewGame.second);
        })
        .delay(200, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pairViewGame -> {
          int[] locationViewGame = new int[2];
          pairViewGame.first.getLocationInWindow(locationViewGame);

          pairViewGame.first.setDrawingCacheEnabled(true);
          pairViewGame.first.buildDrawingCache();
          Bitmap viewBmp = Bitmap.createBitmap(pairViewGame.first.getDrawingCache());
          pairViewGame.first.setDrawingCacheEnabled(false);

          currentGameView = addGameToView(pairViewGame.first, locationViewGame);
          currentGameView.setImageBitmap(viewBmp);
        }));
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  private void setTimer() {
    if (timerSubscription != null) timerSubscription.unsubscribe();

    timerSubscription = Observable.timer(MAX_DURATION_LAYOUT_CONTROLS, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aVoid -> reduceParam());
  }

  private void clearTimer() {
    if (timerSubscription != null) timerSubscription.unsubscribe();
  }

  private void clickOnParam() {
    if (gamesOn) hideGames();
    if (filtersOn) hideFilters();

    if (!isParamExpanded) {
      expandParam();
    } else {
      reduceParam();
    }
  }

  private void expandParam() {
    setTimer();
    isParamExpanded = true;

    onClickParamExpand.onNext(isParamExpanded);

    int widthExtended = layoutContainerParamExtendedLive.getWidth();
    layoutContainerParamExtendedLive.setTranslationX(-(screenUtils.getWidthPx()));
    layoutContainerParamExtendedLive.setVisibility(VISIBLE);

    btnExpand.setImageResource(R.drawable.picto_extend_left_live);

    setXTranslateAnimation(layoutContainerParamExtendedLive, 0);
    setXTranslateAnimation(layoutContainerParamLive, getWidth());
    if (currentGameView != null) setXTranslateAnimation(currentGameView, getWidth());

    if (cameraEnabled) {
      setXTranslateAnimation(btnExpand, widthExtended);
    } else {
      setXTranslateAnimation(btnExpand,
          layoutContainerParamExtendedLive.getWidth() - xTranslation * 2);
    }
  }

  public void reduceParam() {
    clearTimer();
    isParamExpanded = false;
    onClickParamExpand.onNext(isParamExpanded);
    layoutContainerParamExtendedLive.setTranslationX(0);
    btnExpand.setImageResource(R.drawable.picto_extend_right_live);

    setXTranslateAnimation(layoutContainerParamExtendedLive, -screenUtils.getWidthPx());
    setXTranslateAnimation(layoutContainerParamLive, 0);
    if (currentGameView != null) setXTranslateAnimation(currentGameView, 0);
    setXTranslateAnimation(btnExpand, 0);
  }

  private void showFilters() {
    filtersOn = true;
    clearTimer();

    int toX =
        (screenUtils.getWidthPx() >> 1) - btnFilterLocation[0] - (layoutFilter.getWidth() >> 1)
            + screenUtils.dpToPx(2.5f);
    int toY = -screenUtils.dpToPx(65);

    Interpolator customInterpolator = PathInterpolatorCompat.create(0.88f, 1.33f, 0.71f, 1.22f);
    layoutFilter.animate()
        .translationX(toX)
        .translationY(toY)
        .setDuration(DURATION_GAMES_FILTERS)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT_LIGHT))
        .start();

    btnFilterTD.startTransition(DURATION_GAMES_FILTERS);

    imgTriangleCloseFilters.setAlpha(0f);
    imgTriangleCloseFilters.setVisibility(View.VISIBLE);
    imgTriangleCloseFilters.animate()
        .setDuration(DURATION_GAMES_FILTERS)
        .alpha(1)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    for (View v : viewToHideFilters) {
      hideView(v);
    }

    showRecyclerView(recyclerViewFilters);
  }

  private void hideFilters() {
    filtersOn = false;
    setTimer();

    Interpolator customInterpolator = PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1);
    layoutFilter.animate()
        .translationX(0)
        .translationY(0)
        .setDuration(DURATION_GAMES_FILTERS)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT_LIGHT))
        .start();

    btnFilterTD.reverseTransition(DURATION_GAMES_FILTERS);

    imgTriangleCloseFilters.setVisibility(View.GONE);

    for (View v : viewToHideFilters) {
      showView(v);
    }

    hideRecyclerView(recyclerViewFilters);
  }

  private void showGames() {
    gamesOn = true;
    clearTimer();

    recyclerViewGames.getRecycledViewPool().clear();
    gamesAdapter.notifyDataSetChanged();

    int toY = -screenUtils.dpToPx(65);

    layoutGame.animate()
        .translationY(toY)
        .setDuration(DURATION_GAMES_FILTERS)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT_LIGHT))
        .start();

    btnNewGameTD.startTransition(DURATION_GAMES_FILTERS);

    imgTriangleCloseGames.setAlpha(0f);
    imgTriangleCloseGames.setVisibility(View.VISIBLE);
    imgTriangleCloseGames.animate()
        .setDuration(DURATION_GAMES_FILTERS)
        .alpha(1)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    for (View v : viewToHideGames) {
      hideView(v);
    }

    showRecyclerView(recyclerViewGames);
  }

  private void showActiveGame() {
    hideRecyclerView(recyclerViewGames);

    for (View v : viewToHideGames) {
      showView(v);
    }

    hideGameControls();
  }

  private void hideGames() {
    gamesOn = false;
    setTimer();

    layoutGame.animate()
        .translationX(0)
        .translationY(0)
        .setDuration(DURATION_GAMES_FILTERS)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT_LIGHT))
        .start();

    btnNewGameTD.reverseTransition(DURATION_GAMES_FILTERS);

    imgTriangleCloseGames.setVisibility(View.GONE);

    for (View v : viewToHideGames) {
      showView(v);
    }

    hideRecyclerView(recyclerViewGames);
  }

  private void hideRecyclerView(RecyclerView recyclerView) {
    recyclerView.animate()
        .translationY(screenUtils.getHeightPx() >> 1)
        .setDuration(DURATION_GAMES_FILTERS)
        .setInterpolator(new DecelerateInterpolator())
        .start();
  }

  private void showRecyclerView(RecyclerView recyclerView) {
    recyclerView.animate()
        .translationY(0)
        .setDuration(DURATION_GAMES_FILTERS)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT_LIGHT))
        .start();
  }

  private void hideGameControls() {
    hideView(layoutGame);
  }

  private void showGameControls() {
    gamesOn = false;
    imgTriangleCloseGames.setVisibility(View.GONE);
    btnNewGameTD.resetTransition();
    showView(layoutGame);
  }

  private void hideView(View view) {
    view.animate()
        .translationY(screenUtils.getHeightPx() >> 1)
        .setDuration(DURATION_GAMES_FILTERS)
        .setInterpolator(new DecelerateInterpolator())
        .start();
  }

  private void showView(View view) {
    view.animate()
        .translationY(0)
        .setDuration(DURATION_GAMES_FILTERS)
        .setInterpolator(new DecelerateInterpolator())
        .start();
  }

  private ImageView addGameToView(View viewFrom, int[] locationViewGame) {
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizeGameFilter, sizeGameFilter);

    if (viewFrom != null) {
      params.leftMargin = locationViewGame[0];
      params.topMargin = locationViewGame[1] - screenUtils.dpToPx(20);
    } else {
      params.leftMargin = (screenUtils.getWidthPx() >> 1) - (sizeGameFilter >> 1);
      params.topMargin = getHeight() - sizeGameFilter - screenUtils.dpToPx(10);
    }

    ImageView currentGameView = new ImageView(getContext());
    currentGameView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    currentGameView.setBackgroundResource(R.drawable.selectable_button_oval_light);
    currentGameView.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            currentGameView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            if (viewFrom != null) {
              viewFrom.setVisibility(View.GONE);
            } else {
              currentGameView.setTranslationY(screenUtils.getHeightPx() >> 1);
              showView(currentGameView);
            }
            showActiveGame();
          }
        });
    currentGameView.setClickable(true);
    currentGameView.setOnClickListener(v -> onGameOptions.onNext(gameManager.getCurrentGame()));
    addView(currentGameView, params);
    return currentGameView;
  }

  private ViewPropertyAnimator setXTranslateAnimation(View view, float translation) {
    ViewPropertyAnimator xAnim = view.animate();
    xAnim.translationX(translation)
        .setInterpolator(new OvershootInterpolator(0.45f))
        .alpha(1)
        .setDuration(DURATION_PARAM)
        .setListener(null)
        .start();

    return xAnim;
  }

  ///////////////
  //  ONCLICK  //
  ///////////////

  @OnClick(R.id.btnLeave) void clickLeave() {
    onLeave.onNext(null);
  }

  @OnClick(R.id.btnInviteLive) void openInvite() {
    onOpenInvite.onNext(null);
    setTimer();
  }

  @OnClick(R.id.btnOrientationCamera) void clickOrientationCamera() {
    btnOrientationCamera.animate()
        .rotation(btnOrientationCamera.getRotation() == 0 ? 180 : 0)
        .setDuration(DURATION_GAMES_FILTERS)
        .start();
    onClickCameraOrientation.onNext(null);
    setTimer();
  }

  @OnClick(R.id.btnMicro) void clickMicro() {
    microEnabled = !microEnabled;
    onClickMicro.onNext(microEnabled);
    setTimer();
  }

  @OnClick(R.id.btnExpand) void clickExpandParam() {
    clickOnParam();
  }

  @OnClick(R.id.btnCameraOn) void clickCameraEnable() {
    setTimer();

    cameraEnabled = false;
    btnCameraOn.setVisibility(GONE);
    btnCameraOff.setVisibility(VISIBLE);

    Animation scaleAnimation =
        android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.scale_disappear);

    btnOrientationCamera.setAnimation(scaleAnimation);
    layoutFilter.setAnimation(scaleAnimation);

    subscriptions.add(Observable.timer(scaleAnimation.getDuration() / 3, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          setXTranslateAnimation(btnMicro, -xTranslation);
          setXTranslateAnimation(btnExpand,
              layoutContainerParamExtendedLive.getWidth() - xTranslation * 2);
        }));

    onClickCameraEnable.onNext(null);
  }

  @OnClick(R.id.btnCameraOff) void clickCameraDisable() {
    setTimer();

    cameraEnabled = true;
    btnCameraOff.setVisibility(GONE);
    btnCameraOn.setVisibility(VISIBLE);

    Animation scaleAnimation =
        android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.scale_appear);

    layoutContainerParamLive.setVisibility(VISIBLE);

    setXTranslateAnimation(btnMicro, 0);
    setXTranslateAnimation(btnExpand, layoutContainerParamExtendedLive.getWidth());

    btnOrientationCamera.setAnimation(scaleAnimation);
    layoutFilter.setAnimation(scaleAnimation);

    onClickCameraDisable.onNext(null);
  }

  @OnClick(R.id.btnNewGame) void clickNewGame() {
    if (!gamesOn) {
      showGames();
    } else {
      hideGames();
    }
  }

  @OnLongClick(R.id.btnNewGame) boolean clickGameOptions() {
    if (gameManager.getCurrentGame() != null) {
      onGameOptions.onNext(gameManager.getCurrentGame());
    }
    return false;
  }

  @OnClick(R.id.btnNotify) void clickNotify() {
    stateManager.addTutorialKey(StateManager.BUZZ_FRIEND_POPUP);

    btnNotify.setEnabled(false);
    btnNotify.animate()
        .alpha(0.2f)
        .setDuration(DURATION_GAMES_FILTERS)
        .setInterpolator(new DecelerateInterpolator())
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            onNotifyAnimationDone.onNext(null);
            btnNotify.animate().setListener(null);
          }
        })
        .start();

    onClickNotify.onNext(null);
  }

  @OnClick(R.id.btnFilter) void clickFilter() {
    //onClickFilter.onNext(null);
    if (!filtersOn) {
      showFilters();
    } else {
      hideFilters();
    }
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void dispose() {
    btnNotify.clearAnimation();
    btnNotify.animate().setListener(null);
  }

  public void setNotifyEnabled(boolean enable) {
    btnNotify.setEnabled(enable);
  }

  public void setMicroEnabled(boolean enabled) {
    btnMicro.setImageResource(
        enabled ? R.drawable.picto_micro_on_live : R.drawable.picto_micro_off_live);
  }

  public void refactorNotifyButton(boolean enable) {
    if (!enable) {
      btnNotify.setVisibility(View.GONE);
      return;
    } else {
      btnNotify.setVisibility(View.VISIBLE);
    }

    if (enable != btnNotify.isEnabled()) {
      btnNotify.animate().alpha(1).setDuration(DURATION_GAMES_FILTERS);
      btnNotify.setEnabled(true);
    }
  }

  public void startGame(Game game) {
    gameManager.setCurrentGame(game);
    hideGameControls();
    currentGameView = addGameToView(null, null);
    currentGameView.setImageResource(game.getDrawableRes());
  }

  public void stopGame() {
    if (currentGameView != null) {
      removeView(currentGameView);
      currentGameView = null;
    }

    gameManager.stop();
    showGameControls();
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Void> onOpenInvite() {
    return onOpenInvite;
  }

  public Observable<Void> onClickCameraOrientation() {
    return onClickCameraOrientation;
  }

  public Observable<Boolean> onClickMicro() {
    return onClickMicro;
  }

  public Observable<Boolean> onClickParamExpand() {
    return onClickParamExpand;
  }

  public Observable<Void> onClickCameraEnable() {
    return onClickCameraEnable;
  }

  public Observable<Void> onClickCameraDisable() {
    return onClickCameraDisable;
  }

  public Observable<Void> onClickNotify() {
    return onClickNotify;
  }

  public Observable<Void> onNotifyAnimationDone() {
    return onNotifyAnimationDone;
  }

  public Observable<Filter> onClickFilter() {
    return onClickFilter;
  }

  public Observable<Game> onStartGame() {
    return onStartGame;
  }

  public Observable<Void> onLeave() {
    return onLeave;
  }

  public Observable<Game> onGameOptions() {
    return onGameOptions;
  }
}
