package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Guideline;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.DiceView;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import static com.tribe.app.presentation.view.activity.LiveActivity.SOURCE_CALL_ROULETTE;

public class LiveRoomView extends FrameLayout {

  @IntDef({ TYPE_GRID, TYPE_LIST, TYPE_EMBED }) public @interface RoomUIType {
  }

  public static final int TYPE_GRID = 0;
  public static final int TYPE_LIST = 1;
  public static final int TYPE_EMBED = 2;

  private static final int GUIDELINE_HALF_HEIGHT = View.generateViewId();
  private static final int GUIDELINE_HALF_WIDTH = View.generateViewId();
  private static final int GUIDELINE_FIRST_THIRD_HEIGHT = View.generateViewId();
  private static final int GUIDELINE_SECOND_THIRD_HEIGHT = View.generateViewId();
  private static final int GUIDELINE_FIRST_QUARTER_HEIGHT = View.generateViewId();
  private static final int GUIDELINE_SECOND_QUARTER_HEIGHT = View.generateViewId();
  private static final int GUIDELINE_THIRD_QUARTER_HEIGHT = View.generateViewId();

  private static final int GUIDELINE_LANDSCAPE_HALF_HEIGHT = View.generateViewId();
  private static final int GUIDELINE_LANDSCAPE_HALF_WIDTH = View.generateViewId();
  private static final int GUIDELINE_LANDSCAPE_FIRST_THIRD_WIDTH = View.generateViewId();
  private static final int GUIDELINE_LANDSCAPE_SECOND_THIRD_WIDTH = View.generateViewId();
  private static final int GUIDELINE_LANDSCAPE_FIRST_QUARTER_WIDTH = View.generateViewId();
  private static final int GUIDELINE_LANDSCAPE_SECOND_QUARTER_WIDTH = View.generateViewId();
  private static final int GUIDELINE_LANDSCAPE_THIRD_QUARTER_WIDTH = View.generateViewId();

  public static final int CORNER_RADIUS = 5;
  private static final int DURATION = 300;

  @Inject ScreenUtils screenUtils;

  @Inject Navigator navigator;

  @Inject User currentUser;

  // VARIABLES
  private Unbinder unbinder;
  private boolean landscapeMode = false;
  private @LiveActivity.Source String source;
  private boolean isCallRouletteMode = false;
  private LiveRowViewAddFriend liveRowViewAddFriend;

  private Map<Integer, Guideline> guidelineMap = new HashMap<>(), guidelineLandscapeMap =
      new HashMap<>();
  private List<Guideline> guidelineInUse = new ArrayList<>();
  private int type = TYPE_GRID;
  private Map<String, LiveStreamView> mapViews;
  private Map<String, Subscription> mapViewsScoreChangeSubscription;

  @BindView(R.id.layoutConstraint) ConstraintLayout constraintLayout;

  @BindView(R.id.viewLocalLive) LiveLocalView viewLiveLocal;

  @BindView(R.id.diceLayoutRoomView) DiceView diceView;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onShouldCloseInvites = PublishSubject.create();
  private PublishSubject<Void> onChangeCallRouletteRoom = PublishSubject.create();
  private BehaviorSubject<Map<String, LiveStreamView>> onViews = BehaviorSubject.create();

  public LiveRoomView(Context context) {
    super(context);
    init();
  }

  public LiveRoomView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public void jump() {
    onViews = BehaviorSubject.create();
    mapViews.clear();
    mapViews.put(currentUser.getId(), viewLiveLocal);
    onViews.onNext(mapViews);
    mapViewsScoreChangeSubscription.put(currentUser.getId(),
        viewLiveLocal.onScoreChange().subscribe(pair -> refactorConstraintsOnChilds()));
  }

  public void dispose() {
    for (Subscription subscription : mapViewsScoreChangeSubscription.values())
      subscription.unsubscribe();
    mapViewsScoreChangeSubscription.clear();
    mapViews.clear();
  }

  private void init() {
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_room, this);
    unbinder = ButterKnife.bind(this);

    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      landscapeMode = true;
    } else {
      landscapeMode = false;
    }

    mapViews = new HashMap<>();
    mapViewsScoreChangeSubscription = new HashMap<>();
    mapViews.put(currentUser.getId(), viewLiveLocal);
    mapViewsScoreChangeSubscription.put(currentUser.getId(),
        viewLiveLocal.onScoreChange().subscribe(pair -> refactorConstraintsOnChilds()));
    onViews.onNext(mapViews);

    liveRowViewAddFriend = new LiveRowViewAddFriend(getContext());
    liveRowViewAddFriend.setId(View.generateViewId());

    //setScreenSize(0);
    initGuidelines();
  }

  private void initGuidelines() {
    Guideline guidelineHalfWidth = new Guideline(getContext());
    guidelineHalfWidth.setId(GUIDELINE_HALF_WIDTH);
    ConstraintLayout.LayoutParams glpHalfWidth =
        new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    glpHalfWidth.guidePercent = 0.5f;
    glpHalfWidth.orientation = LinearLayout.VERTICAL;
    guidelineHalfWidth.setLayoutParams(glpHalfWidth);

    Guideline guidelineHalfHeight = new Guideline(getContext());
    guidelineHalfHeight.setId(GUIDELINE_HALF_HEIGHT);
    ConstraintLayout.LayoutParams glpHalfHeight =
        new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    glpHalfHeight.guidePercent = 0.5f;
    glpHalfHeight.orientation = LinearLayout.HORIZONTAL;
    guidelineHalfHeight.setLayoutParams(glpHalfHeight);

    Guideline guidelineFirstThirdHeight = new Guideline(getContext());
    guidelineFirstThirdHeight.setId(GUIDELINE_FIRST_THIRD_HEIGHT);
    ConstraintLayout.LayoutParams glpFirstThirdHeight =
        new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    glpFirstThirdHeight.guidePercent = (float) 1 / 3;
    glpFirstThirdHeight.orientation = LinearLayout.HORIZONTAL;
    guidelineFirstThirdHeight.setLayoutParams(glpFirstThirdHeight);

    Guideline guidelineSecondThirdHeight = new Guideline(getContext());
    guidelineSecondThirdHeight.setId(GUIDELINE_SECOND_THIRD_HEIGHT);
    ConstraintLayout.LayoutParams glpSecondThirdHeight =
        new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    glpSecondThirdHeight.guidePercent = (float) 2 / 3;
    glpSecondThirdHeight.orientation = LinearLayout.HORIZONTAL;
    guidelineSecondThirdHeight.setLayoutParams(glpSecondThirdHeight);

    Guideline guidelineFirstQuarterHeight = new Guideline(getContext());
    guidelineFirstQuarterHeight.setId(GUIDELINE_FIRST_QUARTER_HEIGHT);
    ConstraintLayout.LayoutParams glpFirstQuarterHeight =
        new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    glpFirstQuarterHeight.guidePercent = (float) 1 / 4;
    glpFirstQuarterHeight.orientation = LinearLayout.HORIZONTAL;
    guidelineFirstQuarterHeight.setLayoutParams(glpFirstQuarterHeight);

    Guideline guidelineSecondQuarterHeight = new Guideline(getContext());
    guidelineSecondQuarterHeight.setId(GUIDELINE_SECOND_QUARTER_HEIGHT);
    ConstraintLayout.LayoutParams glpSecondQuarterHeight =
        new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    glpSecondQuarterHeight.guidePercent = (float) 2 / 4;
    glpSecondQuarterHeight.orientation = LinearLayout.HORIZONTAL;
    guidelineSecondQuarterHeight.setLayoutParams(glpSecondQuarterHeight);

    Guideline guidelineThirdQuarterHeight = new Guideline(getContext());
    guidelineThirdQuarterHeight.setId(GUIDELINE_THIRD_QUARTER_HEIGHT);
    ConstraintLayout.LayoutParams glpThirdQuarterHeight =
        new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    glpThirdQuarterHeight.guidePercent = (float) 3 / 4;
    glpThirdQuarterHeight.orientation = LinearLayout.HORIZONTAL;
    guidelineThirdQuarterHeight.setLayoutParams(glpThirdQuarterHeight);

    guidelineMap.put(GUIDELINE_HALF_WIDTH, guidelineHalfWidth);
    guidelineMap.put(GUIDELINE_HALF_HEIGHT, guidelineHalfHeight);
    guidelineMap.put(GUIDELINE_FIRST_THIRD_HEIGHT, guidelineFirstThirdHeight);
    guidelineMap.put(GUIDELINE_SECOND_THIRD_HEIGHT, guidelineSecondThirdHeight);
    guidelineMap.put(GUIDELINE_FIRST_QUARTER_HEIGHT, guidelineFirstQuarterHeight);
    guidelineMap.put(GUIDELINE_SECOND_QUARTER_HEIGHT, guidelineSecondQuarterHeight);
    guidelineMap.put(GUIDELINE_THIRD_QUARTER_HEIGHT, guidelineThirdQuarterHeight);

    Guideline guidelineLandscapeHalfWidth = new Guideline(getContext());
    guidelineLandscapeHalfWidth.setId(GUIDELINE_LANDSCAPE_HALF_WIDTH);
    ConstraintLayout.LayoutParams glpLandscapeHalfWidth =
        new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    glpLandscapeHalfWidth.guidePercent = 0.5f;
    glpLandscapeHalfWidth.orientation = LinearLayout.VERTICAL;
    guidelineLandscapeHalfWidth.setLayoutParams(glpLandscapeHalfWidth);

    Guideline guidelineLandscapeHalfHeight = new Guideline(getContext());
    guidelineLandscapeHalfHeight.setId(GUIDELINE_HALF_HEIGHT);
    ConstraintLayout.LayoutParams glpLandscapeHalfHeight =
        new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    glpLandscapeHalfHeight.guidePercent = 0.5f;
    glpLandscapeHalfHeight.orientation = LinearLayout.HORIZONTAL;
    guidelineLandscapeHalfHeight.setLayoutParams(glpLandscapeHalfHeight);

    Guideline guidelineLandscapeFirstThirdWidth = new Guideline(getContext());
    guidelineLandscapeFirstThirdWidth.setId(GUIDELINE_LANDSCAPE_FIRST_THIRD_WIDTH);
    ConstraintLayout.LayoutParams glpLandscapeFirstThirdWidth =
        new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    glpLandscapeFirstThirdWidth.guidePercent = (float) 1 / 3;
    glpLandscapeFirstThirdWidth.orientation = LinearLayout.VERTICAL;
    guidelineLandscapeFirstThirdWidth.setLayoutParams(glpLandscapeFirstThirdWidth);

    Guideline guidelineLandscapeSecondThirdWidth = new Guideline(getContext());
    guidelineLandscapeSecondThirdWidth.setId(GUIDELINE_LANDSCAPE_SECOND_THIRD_WIDTH);
    ConstraintLayout.LayoutParams glpLandscapeSecondThirdWidth =
        new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    glpLandscapeSecondThirdWidth.guidePercent = (float) 2 / 3;
    glpLandscapeSecondThirdWidth.orientation = LinearLayout.VERTICAL;
    guidelineLandscapeSecondThirdWidth.setLayoutParams(glpLandscapeSecondThirdWidth);

    Guideline guidelineLandscapeFirstQuarterWidth = new Guideline(getContext());
    guidelineLandscapeFirstQuarterWidth.setId(GUIDELINE_LANDSCAPE_FIRST_QUARTER_WIDTH);
    ConstraintLayout.LayoutParams glpLandscapeFirstQuarterWidth =
        new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    glpLandscapeFirstQuarterWidth.guidePercent = (float) 1 / 4;
    glpLandscapeFirstQuarterWidth.orientation = LinearLayout.VERTICAL;
    guidelineLandscapeFirstQuarterWidth.setLayoutParams(glpLandscapeFirstQuarterWidth);

    Guideline guidelineLandscapeSecondQuarterWidth = new Guideline(getContext());
    guidelineLandscapeSecondQuarterWidth.setId(GUIDELINE_LANDSCAPE_SECOND_QUARTER_WIDTH);
    ConstraintLayout.LayoutParams glpLandscapeSecondQuarterWidth =
        new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    glpLandscapeSecondQuarterWidth.guidePercent = (float) 2 / 4;
    glpLandscapeSecondQuarterWidth.orientation = LinearLayout.VERTICAL;
    guidelineLandscapeSecondQuarterWidth.setLayoutParams(glpLandscapeSecondQuarterWidth);

    Guideline guidelineLandscapeThirdQuarterWidth = new Guideline(getContext());
    guidelineLandscapeThirdQuarterWidth.setId(GUIDELINE_LANDSCAPE_THIRD_QUARTER_WIDTH);
    ConstraintLayout.LayoutParams glpLandscapeThirdQuarterWidth =
        new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    glpLandscapeThirdQuarterWidth.guidePercent = (float) 3 / 4;
    glpLandscapeThirdQuarterWidth.orientation = LinearLayout.VERTICAL;
    guidelineLandscapeThirdQuarterWidth.setLayoutParams(glpLandscapeThirdQuarterWidth);

    guidelineLandscapeMap.put(GUIDELINE_LANDSCAPE_HALF_WIDTH, guidelineLandscapeHalfWidth);
    guidelineLandscapeMap.put(GUIDELINE_LANDSCAPE_HALF_HEIGHT, guidelineLandscapeHalfHeight);
    guidelineLandscapeMap.put(GUIDELINE_LANDSCAPE_FIRST_THIRD_WIDTH,
        guidelineLandscapeFirstThirdWidth);
    guidelineLandscapeMap.put(GUIDELINE_LANDSCAPE_SECOND_THIRD_WIDTH,
        guidelineLandscapeSecondThirdWidth);
    guidelineLandscapeMap.put(GUIDELINE_LANDSCAPE_FIRST_QUARTER_WIDTH,
        guidelineLandscapeFirstQuarterWidth);
    guidelineLandscapeMap.put(GUIDELINE_LANDSCAPE_SECOND_QUARTER_WIDTH,
        guidelineLandscapeSecondQuarterWidth);
    guidelineLandscapeMap.put(GUIDELINE_LANDSCAPE_THIRD_QUARTER_WIDTH,
        guidelineLandscapeThirdQuarterWidth);
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

  private int getNBLiveViews() {
    return mapViews.size();
  }

  /**
   * ON ROLL THE DICE MESSAGE HAS RECEIVED. SOMEONE NEXT THE DICE
   * IF I ENTER NORMALY => THE DICE ENROLL & I WAITING TO SOME CALL ROULETTEUR
   * IF I AM IN CALL ROULETTE MODE => I AM NEXED, I LEAVE THE ROOM AND ENTER I ANOTHER ONE
   */
  public void onRollTheDiceReceived() {
    isCallRouletteMode = true;
    onShouldCloseInvites.onNext(null);
    diceView.setVisibility(VISIBLE);
    diceView.startDiceAnimation();
    if (source != null && source.equals(SOURCE_CALL_ROULETTE)) {
      onChangeCallRouletteRoom.onNext(null);
    }
  }

  public void setSource(@LiveActivity.Source String source) {
    this.source = source;
    if (!source.equals(SOURCE_CALL_ROULETTE)) {
      diceView.setVisibility(GONE);
    } else {
      diceView.setVisibility(VISIBLE);
    }
  }

  /////////////////
  //   PUBLIC    //
  /////////////////

  public LiveRowView getLiveRowViewFromId(String userId) {
    LiveRowView row = null;

    for (int i = 0; i < constraintLayout.getChildCount(); i++) {
      if (constraintLayout.getChildAt(i) instanceof LiveRowView) {
        LiveRowView v = (LiveRowView) constraintLayout.getChildAt(i);
        if (v.getGuest().getId().equals(userId)) {
          row = v;
        }
      }
    }
    return row;
  }

  public void removeView(String userId, LiveRowView view) {
    int childCount = getNBLiveViews();
    manageGuidelines(childCount - 1);

    mapViews.remove(userId);
    onViews.onNext(mapViews);
    mapViewsScoreChangeSubscription.get(userId).unsubscribe();
    mapViewsScoreChangeSubscription.remove(userId);

    if (view != null) {
      constraintLayout.removeView(view);
      refactorConstraintsOnChilds();
    }

    if (source != null && source.equals(SOURCE_CALL_ROULETTE) && getNBLiveViews() < 2) {
      diceView.setVisibility(VISIBLE);
      diceView.startDiceAnimation();
    }
  }

  public void setType(@RoomUIType int type) {
    if (this.type == type) return;

    this.type = type;

    int translation = 0;

    if (type == TYPE_GRID) {
      setBackgroundColor(Color.BLACK);
      constraintLayout.removeView(liveRowViewAddFriend);
    } else if (type == TYPE_LIST || type == TYPE_EMBED) {
      translation = (getMeasuredHeight() >> 1) - screenUtils.dpToPx(60);
      setBackgroundColor(Color.TRANSPARENT);
      if (type == TYPE_LIST) {
        MarginLayoutParams params =
            new MarginLayoutParams(screenUtils.dpToPx(LiveStreamView.MAX_HEIGHT_LIST),
                screenUtils.dpToPx(LiveStreamView.MAX_HEIGHT_LIST));
        constraintLayout.addView(liveRowViewAddFriend, params);
      } else {
        constraintLayout.removeView(liveRowViewAddFriend);
      }
    }

    diceView.translateDice(translation, true);

    refactorConstraintsOnChilds();
  }

  public void addViewConstraint(String userId, LiveRowView view) {
    if ((source != null && source.equals(SOURCE_CALL_ROULETTE)) || isCallRouletteMode) {
      diceView.setNextAnimation();
    }
    mapViews.put(userId, view);
    onViews.onNext(mapViews);
    mapViewsScoreChangeSubscription.put(userId,
        view.onScoreChange().subscribe(pair -> refactorConstraintsOnChilds()));
    int childCount = getNBLiveViews();
    manageGuidelines(childCount);
    addViewToContainer(childCount, view);
    refactorConstraintsOnChilds();
  }

  /////////////////
  //    INIT     //
  /////////////////

  @Override public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      landscapeMode = true;
    } else {
      landscapeMode = false;
    }

    int childCount = getNBLiveViews();
    manageGuidelines(childCount);
    refactorConstraintsOnChilds();
  }

  /////////////////
  //   PRIVATE   //
  /////////////////

  private void manageGuidelines(int childCount) {
    for (Guideline guideline : guidelineInUse) {
      constraintLayout.removeView(guideline);
    }

    guidelineInUse.clear();

    if (landscapeMode) {
      if (childCount == 2) {
        addGuideline(guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_WIDTH));
      } else if (childCount == 3) {
        addLandscapeThirdGuidelines();
      } else if (childCount == 4) {
        addLandscapeQuarterGuidelines();
      } else if (childCount == 5 || childCount == 6) {
        addLandscapeThirdGuidelines();
        addGuideline(guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT));
      } else if (childCount == 7 || childCount == 8) {
        addLandscapeQuarterGuidelines();
        addGuideline(guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT));
      }
    } else {
      if (childCount == 2) {
        addGuideline(guidelineMap.get(GUIDELINE_HALF_HEIGHT));
      } else if (childCount == 3 || childCount == 4) {
        addGuideline(guidelineMap.get(GUIDELINE_HALF_HEIGHT));
        addGuideline(guidelineMap.get(GUIDELINE_HALF_WIDTH));
      } else if (childCount == 5 || childCount == 6) {
        addGuideline(guidelineMap.get(GUIDELINE_HALF_WIDTH));
        addGuideline(guidelineMap.get(GUIDELINE_FIRST_THIRD_HEIGHT));
        addGuideline(guidelineMap.get(GUIDELINE_SECOND_THIRD_HEIGHT));
      } else if (childCount == 7 || childCount == 8) {
        addGuideline(guidelineMap.get(GUIDELINE_HALF_WIDTH));
        addGuideline(guidelineMap.get(GUIDELINE_FIRST_QUARTER_HEIGHT));
        addGuideline(guidelineMap.get(GUIDELINE_SECOND_QUARTER_HEIGHT));
        addGuideline(guidelineMap.get(GUIDELINE_THIRD_QUARTER_HEIGHT));
      }
    }
  }

  private void addLandscapeThirdGuidelines() {
    addGuideline(guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_FIRST_THIRD_WIDTH));
    addGuideline(guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_SECOND_THIRD_WIDTH));
  }

  private void addLandscapeQuarterGuidelines() {
    addGuideline(guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_FIRST_QUARTER_WIDTH));
    addGuideline(guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_SECOND_QUARTER_WIDTH));
    addGuideline(guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_THIRD_QUARTER_WIDTH));
  }

  private void addGuideline(Guideline guideline) {
    if (guidelineInUse.contains(guideline)) return;
    constraintLayout.addView(guideline);
    guidelineInUse.add(guideline);
  }

  private void addViewToContainer(int childCount, LiveRowView view) {
    ConstraintLayout.LayoutParams params =
        new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
    constraintLayout.addView(view, childCount, params);
  }

  private void refactorConstraintsOnChilds() {
    int liveViewsCount = getNBLiveViews();
    int lastStream = 0;

    ConstraintSet set = new ConstraintSet();
    set.clone(constraintLayout);

    LiveStreamView v = null;
    int liveStreamCount = -1;

    List<LiveStreamView> views = new ArrayList<>(mapViews.values());
    if (type == TYPE_LIST) {
      Collections.sort(views, (o1, o2) -> new Integer(o2.getScore()).compareTo(o1.getScore()));
    }

    for (int i = 0; i < views.size(); i++) {
      v = views.get(i);
      liveStreamCount++;

      if (v != null) {
        if (type == TYPE_GRID) {
          set.clear(v.getId());
          set.setElevation(v.getId(), 0);
          v.setStyle(type);

          if (landscapeMode) {
            switch (liveStreamCount) {
              case 0:
                if (liveViewsCount == 1) {
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                } else if (liveViewsCount == 2) {
                  Guideline guideline = guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guideline.getId(),
                      ConstraintSet.START);
                } else if (liveViewsCount == 3 || liveViewsCount == 5) {
                  Guideline guideline =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_SECOND_THIRD_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guideline.getId(),
                      ConstraintSet.START);
                } else if (liveViewsCount == 4 || liveViewsCount == 7) {
                  Guideline guideline =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_THIRD_QUARTER_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guideline.getId(),
                      ConstraintSet.START);
                } else if (liveViewsCount == 6) {
                  Guideline guidelineSecondThirdWidth =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_SECOND_THIRD_WIDTH);
                  Guideline guidelineHalfHeight =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineHalfHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineSecondThirdWidth.getId(),
                      ConstraintSet.START);
                } else if (liveViewsCount == 8) {
                  Guideline guidelineThirdQuarterWidth =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_THIRD_QUARTER_WIDTH);
                  Guideline guidelineHalfHeight =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineHalfHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineThirdQuarterWidth.getId(),
                      ConstraintSet.START);
                }

                set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                    ConstraintSet.END);
                set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                    ConstraintSet.BOTTOM);

                break;

              case 1:
                if (liveViewsCount == 2) {
                  Guideline guideline = guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_WIDTH);

                  set.connect(v.getId(), ConstraintSet.END, guideline.getId(), ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 3) {
                  Guideline guideline =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_FIRST_THIRD_WIDTH);

                  set.connect(v.getId(), ConstraintSet.END, guideline.getId(), ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 4) {
                  Guideline guideline =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_FIRST_QUARTER_WIDTH);

                  set.connect(v.getId(), ConstraintSet.END, guideline.getId(), ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 5 || liveViewsCount == 6) {
                  Guideline guidelineHalfHeight =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT);
                  Guideline guideline =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_FIRST_THIRD_WIDTH);
                  set.connect(v.getId(), ConstraintSet.END, guideline.getId(), ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineHalfHeight.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 7 || liveViewsCount == 8) {
                  Guideline guidelineHalfHeight =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT);
                  Guideline guideline =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_FIRST_QUARTER_WIDTH);
                  set.connect(v.getId(), ConstraintSet.END, guideline.getId(), ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineHalfHeight.getId(),
                      ConstraintSet.BOTTOM);
                }

                set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                    ConstraintSet.TOP);
                set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                    ConstraintSet.START);

                break;

              case 2:
                if (liveViewsCount == 3) {
                  Guideline guidelineFirstThird =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_FIRST_THIRD_WIDTH);
                  Guideline guidelineSecondThird =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_SECOND_THIRD_WIDTH);

                  set.connect(v.getId(), ConstraintSet.END, guidelineSecondThird.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineFirstThird.getId(),
                      ConstraintSet.START);
                } else if (liveViewsCount == 4) {
                  Guideline guidelineFirstQuarter =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_FIRST_QUARTER_WIDTH);
                  Guideline guidelineSecondQuarter =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_SECOND_QUARTER_WIDTH);

                  set.connect(v.getId(), ConstraintSet.END, guidelineSecondQuarter.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineFirstQuarter.getId(),
                      ConstraintSet.START);
                } else if (liveViewsCount == 5 || liveViewsCount == 6) {
                  Guideline guidelineFirstThird =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_FIRST_THIRD_WIDTH);
                  Guideline guidelineSecondThird =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_SECOND_THIRD_WIDTH);
                  Guideline guidelineHalfHeight =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT);

                  set.connect(v.getId(), ConstraintSet.END, guidelineSecondThird.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineHalfHeight.getId(),
                      ConstraintSet.BOTTOM);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineFirstThird.getId(),
                      ConstraintSet.START);
                } else if (liveViewsCount == 7 || liveViewsCount == 8) {
                  Guideline guidelineFirstQuarter =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_FIRST_QUARTER_WIDTH);
                  Guideline guidelineSecondQuarter =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_SECOND_QUARTER_WIDTH);
                  Guideline guidelineHalfHeight =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT);

                  set.connect(v.getId(), ConstraintSet.END, guidelineSecondQuarter.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineHalfHeight.getId(),
                      ConstraintSet.BOTTOM);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineFirstQuarter.getId(),
                      ConstraintSet.START);
                }

                break;

              case 3:
                if (liveViewsCount == 4) {
                  Guideline guidelineSecondQuarter =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_SECOND_QUARTER_WIDTH);
                  Guideline guidelineThirdQuarter =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_THIRD_QUARTER_WIDTH);

                  set.connect(v.getId(), ConstraintSet.END, guidelineThirdQuarter.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineSecondQuarter.getId(),
                      ConstraintSet.START);
                } else if (liveViewsCount == 5 || liveViewsCount == 6) {
                  Guideline guideline =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_FIRST_THIRD_WIDTH);
                  Guideline guidelineHalfHeight =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT);

                  set.connect(v.getId(), ConstraintSet.END, guideline.getId(), ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineHalfHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                } else if (liveViewsCount == 7 || liveViewsCount == 8) {
                  Guideline guideline =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_FIRST_QUARTER_WIDTH);
                  Guideline guidelineHalfHeight =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT);

                  set.connect(v.getId(), ConstraintSet.END, guideline.getId(), ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineHalfHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                }

                break;

              case 4:
                if (liveViewsCount == 5 || liveViewsCount == 6) {
                  Guideline guidelineFirstThird =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_FIRST_THIRD_WIDTH);
                  Guideline guidelineSecondThird =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_SECOND_THIRD_WIDTH);
                  Guideline guidelineHalfHeight =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT);

                  set.connect(v.getId(), ConstraintSet.END, guidelineSecondThird.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineHalfHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineFirstThird.getId(),
                      ConstraintSet.START);
                } else if (liveViewsCount == 7 || liveViewsCount == 8) {
                  Guideline guidelineFirstQuarter =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_FIRST_QUARTER_WIDTH);
                  Guideline guidelineSecondQuarter =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_SECOND_QUARTER_WIDTH);
                  Guideline guidelineHalfHeight =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT);

                  set.connect(v.getId(), ConstraintSet.END, guidelineSecondQuarter.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineHalfHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineFirstQuarter.getId(),
                      ConstraintSet.START);
                }

                break;

              case 5:
                if (liveViewsCount == 6) {
                  Guideline guidelineSecondThirdWidth =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_SECOND_THIRD_WIDTH);
                  Guideline guidelineHalfHeight =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineSecondThirdWidth.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineHalfHeight.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 7 || liveViewsCount == 8) {
                  Guideline guidelineThirdQuarter =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_THIRD_QUARTER_WIDTH);
                  Guideline guidelineSecondQuarter =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_SECOND_QUARTER_WIDTH);
                  Guideline guidelineHalfHeight =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT);

                  set.connect(v.getId(), ConstraintSet.END, guidelineThirdQuarter.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineHalfHeight.getId(),
                      ConstraintSet.BOTTOM);
                  set.connect(v.getId(), ConstraintSet.START, guidelineSecondQuarter.getId(),
                      ConstraintSet.START);
                }

                break;
              case 6:
                if (liveViewsCount == 7 || liveViewsCount == 8) {
                  Guideline guidelineThirdQuarter =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_THIRD_QUARTER_WIDTH);
                  Guideline guidelineSecondQuarter =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_SECOND_QUARTER_WIDTH);
                  Guideline guidelineHalfHeight =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT);

                  set.connect(v.getId(), ConstraintSet.END, guidelineThirdQuarter.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineHalfHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                  set.connect(v.getId(), ConstraintSet.START, guidelineSecondQuarter.getId(),
                      ConstraintSet.START);
                }

                break;

              case 7:
                if (liveViewsCount == 8) {
                  Guideline guidelineThirdQuarterWidth =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_THIRD_QUARTER_WIDTH);
                  Guideline guidelineHalfHeight =
                      guidelineLandscapeMap.get(GUIDELINE_LANDSCAPE_HALF_HEIGHT);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineThirdQuarterWidth.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineHalfHeight.getId(),
                      ConstraintSet.BOTTOM);
                }

                break;
            }
          } else {
            switch (liveStreamCount) {
              case 0:
                if (liveViewsCount == 1) {
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 2 || liveViewsCount == 3) {
                  Guideline guideline = guidelineMap.get(GUIDELINE_HALF_HEIGHT);
                  set.connect(v.getId(), ConstraintSet.TOP, guideline.getId(), ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 4) {
                  Guideline guidelineHalfHeight = guidelineMap.get(GUIDELINE_HALF_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineHalfHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineHalfWidth.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 5) {
                  Guideline guidelineSecondThirdHeight =
                      guidelineMap.get(GUIDELINE_SECOND_THIRD_HEIGHT);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineSecondThirdHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 6) {
                  Guideline guidelineSecondThirdHeight =
                      guidelineMap.get(GUIDELINE_SECOND_THIRD_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineSecondThirdHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineHalfWidth.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 7) {
                  Guideline guidelineThirdQuarterHeight =
                      guidelineMap.get(GUIDELINE_THIRD_QUARTER_HEIGHT);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineThirdQuarterHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 8) {
                  Guideline guidelineThirdQuarterHeight =
                      guidelineMap.get(GUIDELINE_THIRD_QUARTER_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineThirdQuarterHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineHalfWidth.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                }

                break;
              case 1:
                if (liveViewsCount == 2) {
                  Guideline guideline = guidelineMap.get(GUIDELINE_HALF_HEIGHT);
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guideline.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 3 || liveViewsCount == 4) {
                  Guideline guidelineHalfHeight = guidelineMap.get(GUIDELINE_HALF_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineHalfWidth.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineHalfHeight.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 5 || liveViewsCount == 6) {
                  Guideline guidelineFirstThirdHeight =
                      guidelineMap.get(GUIDELINE_FIRST_THIRD_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineHalfWidth.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineFirstThirdHeight.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 7 || liveViewsCount == 8) {
                  Guideline guidelineFirstQuarterHeight =
                      guidelineMap.get(GUIDELINE_FIRST_QUARTER_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineHalfWidth.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineFirstQuarterHeight.getId(),
                      ConstraintSet.BOTTOM);
                }

                break;

              case 2:
                if (liveViewsCount == 3 || liveViewsCount == 4) {
                  Guideline guidelineHalfHeight = guidelineMap.get(GUIDELINE_HALF_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, guidelineHalfWidth.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineHalfHeight.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 5 || liveViewsCount == 6) {
                  Guideline guidelineFirstThirdHeight =
                      guidelineMap.get(GUIDELINE_FIRST_THIRD_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, guidelineHalfWidth.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineFirstThirdHeight.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 7 || liveViewsCount == 8) {
                  Guideline guidelineFirstQuarterHeight =
                      guidelineMap.get(GUIDELINE_FIRST_QUARTER_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, guidelineHalfWidth.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineFirstQuarterHeight.getId(),
                      ConstraintSet.BOTTOM);
                }

                break;

              case 3:
                if (liveViewsCount == 4) {
                  Guideline guidelineHalfHeight = guidelineMap.get(GUIDELINE_HALF_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineHalfHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, guidelineHalfWidth.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 5 || liveViewsCount == 6) {
                  Guideline guidelineFirstThirdHeight =
                      guidelineMap.get(GUIDELINE_FIRST_THIRD_HEIGHT);
                  Guideline guidelineSecondThirdHeight =
                      guidelineMap.get(GUIDELINE_SECOND_THIRD_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineFirstThirdHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, guidelineHalfWidth.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineSecondThirdHeight.getId(),
                      ConstraintSet.TOP);
                } else if (liveViewsCount == 7 || liveViewsCount == 8) {
                  Guideline guidelineFirstQuarterHeight =
                      guidelineMap.get(GUIDELINE_FIRST_QUARTER_HEIGHT);
                  Guideline guidelineSecondQuarterHeight =
                      guidelineMap.get(GUIDELINE_SECOND_QUARTER_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineFirstQuarterHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, guidelineHalfWidth.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineSecondQuarterHeight.getId(),
                      ConstraintSet.TOP);
                }

                break;

              case 4:
                if (liveViewsCount == 5 || liveViewsCount == 6) {
                  Guideline guidelineFirstThirdHeight =
                      guidelineMap.get(GUIDELINE_FIRST_THIRD_HEIGHT);
                  Guideline guidelineSecondThirdHeight =
                      guidelineMap.get(GUIDELINE_SECOND_THIRD_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineFirstThirdHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineHalfWidth.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineSecondThirdHeight.getId(),
                      ConstraintSet.TOP);
                } else if (liveViewsCount == 7 || liveViewsCount == 8) {
                  Guideline guidelineFirstQuarterHeight =
                      guidelineMap.get(GUIDELINE_FIRST_QUARTER_HEIGHT);
                  Guideline guidelineSecondQuarterHeight =
                      guidelineMap.get(GUIDELINE_SECOND_QUARTER_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineFirstQuarterHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineHalfWidth.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineSecondQuarterHeight.getId(),
                      ConstraintSet.TOP);
                }

                break;

              case 5:
                if (liveViewsCount == 6) {
                  Guideline guidelineSecondThirdHeight =
                      guidelineMap.get(GUIDELINE_SECOND_THIRD_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineSecondThirdHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, guidelineHalfWidth.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                } else if (liveViewsCount == 7 || liveViewsCount == 8) {
                  Guideline guidelineSecondQuarterHeight =
                      guidelineMap.get(GUIDELINE_SECOND_QUARTER_HEIGHT);
                  Guideline guidelineThirdQuarterHeight =
                      guidelineMap.get(GUIDELINE_THIRD_QUARTER_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineSecondQuarterHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, guidelineHalfWidth.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineThirdQuarterHeight.getId(),
                      ConstraintSet.TOP);
                }

                break;

              case 6:
                if (liveViewsCount == 7 || liveViewsCount == 8) {
                  Guideline guidelineSecondQuarterHeight =
                      guidelineMap.get(GUIDELINE_SECOND_QUARTER_HEIGHT);
                  Guideline guidelineThirdQuarterHeight =
                      guidelineMap.get(GUIDELINE_THIRD_QUARTER_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineSecondQuarterHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, guidelineHalfWidth.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineThirdQuarterHeight.getId(),
                      ConstraintSet.TOP);
                }

                break;

              case 7:
                if (liveViewsCount == 8) {
                  Guideline guidelineThirdQuarterHeight =
                      guidelineMap.get(GUIDELINE_THIRD_QUARTER_HEIGHT);
                  Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
                  set.connect(v.getId(), ConstraintSet.TOP, guidelineThirdQuarterHeight.getId(),
                      ConstraintSet.TOP);
                  set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                      ConstraintSet.START);
                  set.connect(v.getId(), ConstraintSet.END, guidelineHalfWidth.getId(),
                      ConstraintSet.END);
                  set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                      ConstraintSet.BOTTOM);
                }

                break;
            }
          }
        } else {
          set.clear(v.getId());

          set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(), ConstraintSet.START,
              screenUtils.dpToPx(20));
          set.constrainWidth(v.getId(), screenUtils.dpToPx(150));
          set.constrainHeight(v.getId(), screenUtils.dpToPx(
              type == TYPE_EMBED ? LiveStreamView.MAX_HEIGHT_EMBED
                  : LiveStreamView.MAX_HEIGHT_LIST));
          set.setElevation(v.getId(), screenUtils.dpToPx(10));
          v.setStyle(type);

          switch (i) {
            case 0:
              set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP,
                  screenUtils.dpToPx(20));
              break;

            default:
              View previous = views.get(lastStream);
              set.connect(v.getId(), ConstraintSet.TOP, previous.getId(), ConstraintSet.BOTTOM,
                  screenUtils.dpToPx(20));
              break;
          }

          lastStream = i;
        }
      }
    }

    if (type == TYPE_LIST) {
      View previous = views.get(lastStream);
      set.connect(liveRowViewAddFriend.getId(), ConstraintSet.TOP, previous.getId(),
          ConstraintSet.BOTTOM, screenUtils.dpToPx(20));
      set.connect(liveRowViewAddFriend.getId(), ConstraintSet.START, constraintLayout.getId(),
          ConstraintSet.START, screenUtils.dpToPx(20));
    }

    AutoTransition autoTransition = new AutoTransition();
    TransitionManager.beginDelayedTransition(constraintLayout, autoTransition);
    set.applyTo(constraintLayout);
  }

  private void setAvatarPicto(LiveRowView liveRowView, int index) {
    switch (index) {
      case 0:
        setAvatar(liveRowView, Gravity.CENTER, index);
      case 1:
        setAvatar(liveRowView, Gravity.START | Gravity.BOTTOM, index);
        break;
      case 2:
        setAvatar(liveRowView, Gravity.END | Gravity.BOTTOM, index);
        break;
      case 3:
        setAvatar(liveRowView, Gravity.START | Gravity.TOP, index);
        break;
      case 4:
        setAvatar(liveRowView, Gravity.END | Gravity.TOP, index);
        break;
      case 5:
        setAvatar(liveRowView, Gravity.START | Gravity.BOTTOM, index);
        break;
      case 6:
        setAvatar(liveRowView, Gravity.END | Gravity.BOTTOM, index);
        break;
      case 7:
        setAvatar(liveRowView, Gravity.START | Gravity.TOP, index);
        break;
      case 8:
        setAvatar(liveRowView, Gravity.CENTER, index);
        break;
    }
  }

  private void setAvatar(LiveRowView liveRowView, int gravity, int index) {
    AvatarView avatarView = new AvatarView(getContext());
    //avatarView.setBackgroundResource(R.drawable.shape_circle_white);
    int padding = screenUtils.dpToPx(10);
    avatarView.setPadding(padding, padding, padding, padding);
    avatarView.load(liveRowView.getGuest().getPicture());
    ViewCompat.setElevation(avatarView, 10);

 /*   TextView avatarView = new TextView(getContext());
    avatarView.setBackgroundResource(R.drawable.shape_circle_white);
    avatarView.setText(index + " ");*/

    FrameLayout.LayoutParams layoutParams =
        new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    layoutParams.gravity = gravity;
    layoutParams.width = screenUtils.dpToPx(60);
    layoutParams.height = screenUtils.dpToPx(60);
    avatarView.setLayoutParams(layoutParams);
    liveRowView.addView(avatarView);
  }

  public Observable<Void> onChangeCallRouletteRoom() {
    return onChangeCallRouletteRoom;
  }

  public Observable<Map<String, LiveStreamView>> onLiveViewsChange() {
    return onViews;
  }

  public Observable<Void> onClickAddFriend() {
    return liveRowViewAddFriend.onClick();
  }
}