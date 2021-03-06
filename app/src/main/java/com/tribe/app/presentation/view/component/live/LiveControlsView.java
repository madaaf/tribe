package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.adapter.GamesFiltersAdapter;
import com.tribe.app.presentation.view.adapter.manager.GamesFiltersLayoutManager;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.GlideUtils;
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
  private static final int DURATION_NAME = 200;
  private static final float OVERSHOOT_LIGHT = 0.45f;

  @Inject ScreenUtils screenUtils;

  @Inject StateManager stateManager;

  @Inject User user;

  @BindView(R.id.btnCameraOn) View btnCameraOn;

  @BindView(R.id.btnCameraOff) View btnCameraOff;

  @BindView(R.id.layoutFilter) ViewGroup layoutFilter;

  @Nullable @BindView(R.id.btnFilterOn) ImageView btnFilterOn;

  @BindView(R.id.btnFilterOff) ImageView btnFilterOff;

  @BindView(R.id.imgTriangleCloseFilters) ImageView imgTriangleCloseFilters;

  @BindView(R.id.layoutGame) ViewGroup layoutGame;

  @BindView(R.id.btnNewGameOff) ImageView btnNewGameOff;

  @BindView(R.id.btnExpand) ImageView btnExpand;

  @BindView(R.id.btnOrientationCamera) View btnOrientationCamera;

  @BindView(R.id.btnMicro) ImageView btnMicro;

  @BindView(R.id.layoutContainerParamLive) FrameLayout layoutContainerParamLive;

  @BindView(R.id.layoutContainerParamExtendedLive) LinearLayout layoutContainerParamExtendedLive;

  @BindView(R.id.btnChat) LiveChatButton btnChat;

  @BindView(R.id.viewStatusName) LiveStatusNameView viewStatusName;

  @BindViews({
      R.id.btnExpand, R.id.layoutGame
  }) List<View> viewToHideBottomFilters;

  @BindViews({ R.id.btnChat, R.id.viewStatusName }) List<View> viewToHideTopFilters;

  @BindViews({ R.id.btnChat, R.id.viewStatusName }) List<View> viewToHideTopGames;

  @BindViews({
      R.id.viewStatusName, R.id.btnLeave
  }) List<View> viewToHideTopChat;

  @BindViews({
      R.id.btnChat, R.id.btnLeave
  }) List<View> viewToHideTopInvites;

  @BindViews({
      R.id.btnExpand, R.id.layoutFilter, R.id.layoutGame
  }) List<View> viewToHideBottom;

  @BindView(R.id.btnLeave) ImageView btnLeave;

  @BindView(R.id.recyclerViewFilters) RecyclerView recyclerViewFilters;

  // VARIABLES
  private Unbinder unbinder;
  private boolean cameraEnabled = true, microEnabled = true, isParamExpanded = false,
      filtersMenuOn = false, gamesMenuOn = false, chatMenuOn = false, invitesMenuOn = false;
  private float xTranslation;
  private GameManager gameManager;
  private FilterManager filterManager;
  private GamesFiltersLayoutManager filtersLayoutManager;
  private List<GameFilter> filterList;
  private GamesFiltersAdapter filtersAdapter;
  private int[] btnFilterLocation;
  private ImageView currentGameView;
  private @LiveContainer.Event int drawerState = LiveContainer.CLOSED;

  // RESOURCES
  private int sizeGameFilter;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onClickCameraOrientation = PublishSubject.create();
  private PublishSubject<Boolean> onClickMicro = PublishSubject.create();
  private PublishSubject<Boolean> onClickParamExpand = PublishSubject.create();
  private PublishSubject<Void> onClickCameraEnable = PublishSubject.create();
  private PublishSubject<Void> onClickCameraDisable = PublishSubject.create();
  private PublishSubject<Filter> onClickFilter = PublishSubject.create();
  private PublishSubject<Game> onStartGame = PublishSubject.create();
  private PublishSubject<Void> onLeave = PublishSubject.create();
  private PublishSubject<Game> onRestartGame = PublishSubject.create();
  private PublishSubject<Game> onStopGame = PublishSubject.create();
  private PublishSubject<View> onGameUIActive = PublishSubject.create();
  private PublishSubject<Boolean> onGameMenuOpened = PublishSubject.create();
  private PublishSubject<Game> onResetScores = PublishSubject.create();
  private PublishSubject<Void> openGameStore = PublishSubject.create();
  private PublishSubject<Game> onOpenLeaderboard = PublishSubject.create();

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

  public void hideGamesBtn() {
    btnNewGameOff.setVisibility(INVISIBLE);
  }

  public void displayGamesBtn() {
    btnNewGameOff.setVisibility(VISIBLE);
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
    initSubscriptions();
    initFilters();
    initGames();
  }

  private void initResources() {
    sizeGameFilter =
        getContext().getResources().getDimensionPixelSize(R.dimen.filter_game_size_with_border);
  }

  private void initUI() {
    xTranslation = getResources().getDimension(R.dimen.nav_icon_size) + screenUtils.dpToPx(10);

    btnFilterOff.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            btnFilterOff.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            btnFilterOff.getLocationInWindow(btnFilterLocation);
          }
        });
  }

  private void initSubscriptions() {

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
    if (filtersMenuOn) hideFilters();

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

    if (cameraEnabled) {
      setXTranslateAnimation(btnExpand, widthExtended);
    } else {
      setXTranslateAnimation(btnExpand, layoutContainerParamExtendedLive.getWidth() - xTranslation);
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
    setXTranslateAnimation(btnExpand, 0);
  }

  private void showFilters() {
    filtersMenuOn = true;

    int toX =
        (screenUtils.getWidthPx() >> 1) - btnFilterLocation[0] - (layoutFilter.getWidth() >> 1) +
            screenUtils.dpToPx(2.5f);
    int toY = -screenUtils.dpToPx(65);

    layoutFilter.animate()
        .translationX(toX)
        .translationY(toY)
        .setDuration(DURATION_GAMES_FILTERS)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT_LIGHT))
        .start();

    showBtnFilterOn();

    imgTriangleCloseFilters.setAlpha(0f);
    imgTriangleCloseFilters.setVisibility(View.VISIBLE);
    imgTriangleCloseFilters.animate()
        .setDuration(DURATION_GAMES_FILTERS)
        .alpha(1)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    for (View v : viewToHideBottomFilters) {
      hideView(v, false);
    }

    for (View v : viewToHideTopFilters) {
      hideView(v, true);
    }

    if (currentGameView != null) hideView(currentGameView, false);

    showRecyclerView(recyclerViewFilters);
  }

  private void hideFilters() {
    filtersMenuOn = false;

    layoutFilter.animate()
        .translationX(0)
        .translationY(0)
        .setDuration(DURATION_GAMES_FILTERS)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT_LIGHT))
        .start();

    showBtnFilterOff();

    imgTriangleCloseFilters.setVisibility(View.GONE);

    for (View v : viewToHideBottomFilters) {
      if (currentGameView == null || v != layoutGame) showView(v);
    }

    if (currentGameView == null) {
      for (View v : viewToHideTopFilters) {
        showView(v);
      }
    }

    if (currentGameView != null) showView(currentGameView);

    hideRecyclerView(recyclerViewFilters);
  }

  private void showActiveGame(boolean shouldDisplayGameTutorialPopup) {
    gamesMenuOn = false;
    onGameMenuOpened.onNext(gamesMenuOn);

    if (shouldDisplayGameTutorialPopup) onGameUIActive.onNext(currentGameView);

    hideGameControls();
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
    hideView(layoutGame, false);
    hideView(btnChat, true);
    hideView(viewStatusName, true);
  }

  private void showGameControls() {
    gamesMenuOn = false;
    showView(layoutGame);
    showView(viewStatusName);
    showView(btnChat);
  }

  private void hideView(View view, boolean top) {
    view.animate()
        .translationY(top ? -screenUtils.getHeightPx() >> 1 : screenUtils.getHeightPx() >> 1)
        .setDuration(DURATION_GAMES_FILTERS)
        .setInterpolator(new DecelerateInterpolator())
        .start();
  }

  private void showView(View view) {
    view.animate()
        .translationY(0)
        .setDuration(DURATION_GAMES_FILTERS)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT_LIGHT))
        .start();
  }

  private void showBtnFilterOn() {
    AnimationUtils.fadeIn(btnFilterOn, DURATION_GAMES_FILTERS);
    AnimationUtils.fadeOut(btnFilterOff, DURATION_GAMES_FILTERS);
  }

  private void showBtnFilterOff() {
    AnimationUtils.fadeIn(btnFilterOff, DURATION_GAMES_FILTERS);
    AnimationUtils.fadeOut(btnFilterOn, DURATION_GAMES_FILTERS);
  }

  private ImageView addGameToView(View viewFrom, Game newGame) {
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizeGameFilter, sizeGameFilter);
    params.bottomMargin = screenUtils.dpToPx(5);
    params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

    currentGameView = new ImageView(getContext());
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

            showActiveGame(viewFrom != null);

            new GlideUtils.GameImageBuilder(getContext(), screenUtils).url(newGame.getIcon())
                .hasBorder(true)
                .hasPlaceholder(true)
                .rounded(true)
                .target(currentGameView)
                .load();
          }
        });
    currentGameView.setClickable(true);

    subscriptions.add(RxView.clicks(currentGameView)
        .doOnNext(aVoid -> AnimationUtils.makeItBounce(currentGameView, DURATION_GAMES_FILTERS,
            new OvershootInterpolator(OVERSHOOT_LIGHT)))
        .map(aVoid -> gameManager.getCurrentGame())
        .filter(game -> game != null)
        .filter(game -> {
          if ((game.getCurrentMaster() != null &&
              game.getCurrentMaster().getId().equals(user.getId())) ||
              game.getId().equals(Game.GAME_POST_IT)) {
            return true;
          } else {
            Toast.makeText(getContext(), getContext().getString(R.string.game_update_forbidden,
                game.getCurrentMaster() != null ? game.getCurrentMaster().getDisplayName() : ""),
                Toast.LENGTH_LONG).show();
            return false;
          }
        })
        .flatMap(game -> DialogFactory.showBottomSheetForGame(getContext(), game),
            ((game, labelType) -> {
              if (labelType.getTypeDef().equals(LabelType.GAME_RESTART)) {
                onRestartGame.onNext(game);
              } else if (labelType.getTypeDef().equals(LabelType.GAME_PLAY_ANOTHER)) {
                onStopGame.onNext(game);
              } else if (labelType.getTypeDef().equals(LabelType.GAME_LEADERBOARD)) {
                onOpenLeaderboard.onNext(game);
              } else if (labelType.getTypeDef().equals(LabelType.GAME_STOP)) {
                onStopGame.onNext(game);
              }

              return labelType;
            }))
        .filter(labelType -> labelType.getTypeDef().equals(LabelType.GAME_PLAY_ANOTHER))
        .delay(1, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(labelType -> openGameStore.onNext(null)));

    layoutContainerParamLive.addView(currentGameView, params);

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

  private void showMenuTop(List<View> viewsToHide) {
    for (View v : viewsToHide) {
      hideView(v, true);
    }

    for (View v : viewToHideBottom) {
      hideView(v, false);
    }
  }

  private void closeMenuTop(List<View> viewsToHide) {
    chatMenuOn = false;

    for (View v : viewsToHide) {
      showView(v);
    }

    for (View v : viewToHideBottom) {
      showView(v);
    }
  }

  ///////////////
  //  ONCLICK  //
  ///////////////

  @OnClick(R.id.btnLeave) void clickLeave() {
    onLeave.onNext(null);
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
              layoutContainerParamExtendedLive.getWidth() - xTranslation);
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

  @OnClick({ R.id.btnNewGameOff }) void clickNewGame() {
    openGameStore.onNext(null);
  }

  @OnClick({ R.id.btnFilterOn, R.id.btnFilterOff }) void clickFilter() {
    //onClickFilter.onNext(null);
    if (!filtersMenuOn) {
      showFilters();
    } else {
      hideFilters();
    }
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void initDrawerEventChangeObservable(Observable<Integer> onEventChange) {
    subscriptions.add(onEventChange.subscribe(event -> {
      if (drawerState == LiveContainer.CLOSED && event != LiveContainer.CLOSED) {
        drawerState = event;
        viewStatusName.openView();
      } else if ((drawerState == LiveContainer.OPEN_PARTIAL ||
          drawerState == LiveContainer.OPEN_FULL) && event == LiveContainer.CLOSED) {
        drawerState = event;
        viewStatusName.closeView();
      }
    }));
  }

  public void onNewMessage() {
    btnChat.setNewBtn();
  }

  public ImageView getCurrentGameView() {
    return currentGameView;
  }

  public void dispose() {
    viewStatusName.dispose();
  }

  public void setLive(Live live) {
    viewStatusName.setLive(live);
  }

  public void setMicroEnabled(boolean enabled) {
    btnMicro.setImageResource(
        enabled ? R.drawable.picto_micro_on_live : R.drawable.picto_micro_off_live);
  }

  public void startGameFromAnotherUser(Game game) {
    hideGameControls();
    setupCurrentGameView(game, null);
  }

  public void startGame(Game game) {
    onStartGame.onNext(game);
    subscriptions.add(Observable.timer(400, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pairViewGame -> setupCurrentGameView(game, null)));
  }

  public void setupCurrentGameView(Game game, View viewFrom) {
    if (currentGameView == null) {
      currentGameView = addGameToView(viewFrom, game);
    }

    filterManager.suspendFilter();
  }

  public void stopGame() {
    if (currentGameView != null) {
      layoutContainerParamLive.removeView(currentGameView);
      currentGameView = null;
    }

    filterManager.resumeFilter();

    gameManager.stop();
    showGameControls();
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

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

  public Observable<Filter> onClickFilter() {
    return onClickFilter;
  }

  public Observable<Boolean> onGameMenuOpen() {
    return onGameMenuOpened;
  }

  public Observable<Game> onStartGame() {
    return onStartGame;
  }

  public Observable<Void> onLeave() {
    return onLeave;
  }

  public Observable<Integer> onOpenInvite() {
    return viewStatusName.onOpenView().doOnNext(type -> {
      invitesMenuOn = true;
      if (gameManager.getCurrentGame() == null) showMenuTop(viewToHideTopInvites);

      int width = viewStatusName.getNewWidth();

      viewStatusName.animate()
          .translationX((screenUtils.getWidthPx() >> 1) - (width >> 1) - screenUtils.dpToPx(15))
          .setDuration(DURATION_NAME)
          .setInterpolator(new DecelerateInterpolator())
          .start();
    }).filter(type -> drawerState == LiveContainer.CLOSED);
  }

  public Observable<Boolean> onCloseInvite() {
    return viewStatusName.onCloseView()
        .doOnNext(aBoolean -> {
          invitesMenuOn = false;
          if (gameManager.getCurrentGame() == null) closeMenuTop(viewToHideTopInvites);

          viewStatusName.animate()
              .translationX(0)
              .setDuration(DURATION_NAME)
              .setInterpolator(new DecelerateInterpolator())
              .start();
        })
        .filter(aBoolean -> drawerState == LiveContainer.OPEN_PARTIAL ||
            drawerState == LiveContainer.OPEN_FULL);
  }

  public Observable<Boolean> onOpenChat() {
    return btnChat.onOpenChat().doOnNext(aBoolean -> {
      chatMenuOn = true;
      showMenuTop(viewToHideTopChat);
    });
  }

  public Observable<Boolean> onCloseChat() {
    return btnChat.onCloseChat().doOnNext(aBoolean -> {
      chatMenuOn = false;
      closeMenuTop(viewToHideTopChat);
    });
  }

  public Observable<Game> onRestartGame() {
    return onRestartGame;
  }

  public Observable<Game> onStopGame() {
    return onStopGame;
  }

  public Observable<Game> onResetScores() {
    return onResetScores;
  }

  public Observable<Void> openGameStore() {
    return openGameStore.debounce(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread());
  }

  public Observable<Game> onOpenLeaderboard() {
    return onOpenLeaderboard;
  }
}
