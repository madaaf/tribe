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
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.tribe.app.presentation.view.activity.LiveActivity.SOURCE_CALL_ROULETTE;

public class LiveRoomView extends FrameLayout {

  @IntDef({ TYPE_GRID, TYPE_LIST }) public @interface RoomUIType {
  }

  public static final int TYPE_GRID = 0;
  public static final int TYPE_LIST = 1;

  private static final int GUIDELINE_HALF_HEIGHT = View.generateViewId();
  private static final int GUIDELINE_HALF_WIDTH = View.generateViewId();
  private static final int GUIDELINE_FIRST_THIRD_HEIGHT = View.generateViewId();
  private static final int GUIDELINE_SECOND_THIRD_HEIGHT = View.generateViewId();
  private static final int GUIDELINE_FIRST_QUARTER_HEIGHT = View.generateViewId();
  private static final int GUIDELINE_SECOND_QUARTER_HEIGHT = View.generateViewId();
  private static final int GUIDELINE_THIRD_QUARTER_HEIGHT = View.generateViewId();

  public static final int CORNER_RADIUS = 5;
  private static final int DURATION = 300;

  @Inject ScreenUtils screenUtils;

  @Inject Navigator navigator;

  @Inject User currentUser;

  // VARIABLES
  private Unbinder unbinder;
  private boolean landscapeMode = false;
  private int witdhScreen;
  private int heightScreen;
  private boolean isConfigurationChanged = false;
  private @LiveActivity.Source String source;
  private boolean isCallRouletteMode = false;

  private Map<Integer, Guideline> guidelineMap = new HashMap<>();
  private List<Guideline> guidelineInUse = new ArrayList<>();
  private int type = TYPE_GRID;
  private Map<String, LiveStreamView> mapViews;

  @BindView(R.id.layoutConstraint) ConstraintLayout constraintLayout;

  @BindView(R.id.viewLocalLive) LiveLocalView viewLiveLocal;

  //@BindView(R.id.diceLayoutRoomView) DiceView diceView;

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
    mapViews.put(currentUser.getId(), viewLiveLocal);
    onViews.onNext(mapViews);

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

  /**
   * ON ROLL THE DICE MESSAGE HAS RECEIVED. SOMEONE NEXT THE DICE
   * IF I ENTER NORMALY => THE DICE ENROLL & I WAITING TO SOME CALL ROULETTEUR
   * IF I AM IN CALL ROULETTE MODE => I AM NEXED, I LEAVE THE ROOM AND ENTER I ANOTHER ONE
   */
  public void onRollTheDiceReceived() {
    isCallRouletteMode = true;
    onShouldCloseInvites.onNext(null);
    //diceView.setVisibility(VISIBLE);
    //diceView.startDiceAnimation();
    if (source != null && source.equals(SOURCE_CALL_ROULETTE)) {
      onChangeCallRouletteRoom.onNext(null);
    }
  }

  public void setSource(@LiveActivity.Source String source) {
    this.source = source;
    //if (source.equals(SOURCE_CALL_ROULETTE)) {
    //  diceView.setVisibility(VISIBLE);
    //}
  }

  /////////////////
  //   PUBLIC    //
  /////////////////

  public void removeView(String userId, LiveRowView view) {
    int childCount = constraintLayout.getChildCount() - guidelineInUse.size();
    manageGuidelines(childCount, false);

    if (view != null) {
      constraintLayout.removeView(view);
      refactorConstraintsOnChilds();
    }

    mapViews.remove(userId);
    onViews.onNext(mapViews);
  }

  public void setType(@RoomUIType int type) {
    if (this.type == type) return;
    this.type = type;
    if (type == TYPE_GRID) {
      setBackgroundColor(Color.BLACK);
    } else {
      setBackgroundColor(Color.TRANSPARENT);
    }
    refactorConstraintsOnChilds();
  }

  //
  //public int getRowsInLive() {
  //  return flexboxLayout.getChildCount();
  //}
  //
  //public void removeGuest(String userId) {
  //  LiveRowView liveRowView;
  //  for (int i = 0; i < flexboxLayout.getChildCount(); i++) {
  //    View view = flexboxLayout.getChildAt(i);
  //    if (view instanceof LiveRowView) {
  //      liveRowView = (LiveRowView) view;
  //      if (liveRowView.getGuest().getId().equals(userId)) {
  //        removeView(liveRowView);
  //      }
  //    }
  //  }
  //}
  //

  public void addViewConstraint(String userId, LiveRowView view) {
    mapViews.put(userId, view);
    onViews.onNext(mapViews);
    int childCount = constraintLayout.getChildCount() - guidelineInUse.size();
    manageGuidelines(childCount, true);
    addViewToContainer(childCount, view);
    refactorConstraintsOnChilds();
  }

  /////////////////
  //    INIT     //
  /////////////////

  //@Override public void onConfigurationChanged(Configuration newConfig) {
  //  super.onConfigurationChanged(newConfig);
  //  isConfigurationChanged = true;
  //  flexboxLayout.invalidate();
  //  flexboxLayout.requestLayout();
  //
  //  if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
  //    landscapeMode = true;
  //  } else {
  //    landscapeMode = false;
  //  }
  //
  //  setScreenSize(0);
  //  setViewsOrder();
  //  setConfigurationScreen();
  //}

  //@Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
  //  super.onLayout(changed, l, t, r, b);
  //  if (isConfigurationChanged) {
  //    setScreenSize(0);
  //    setConfigurationScreen();
  //    isConfigurationChanged = false;
  //  }
  //}

  /////////////////
  //   PRIVATE   //
  /////////////////

  private void manageGuidelines(int childCount, boolean isAdd) {
    if (isAdd) {
      if (childCount == 1) {
        addGuideline(guidelineMap.get(GUIDELINE_HALF_HEIGHT));
      } else if (childCount == 2) {
        addGuideline(guidelineMap.get(GUIDELINE_HALF_WIDTH));
      } else if (childCount == 4) {
        removeGuideline(guidelineMap.get(GUIDELINE_HALF_HEIGHT));
        addGuideline(guidelineMap.get(GUIDELINE_FIRST_THIRD_HEIGHT));
        addGuideline(guidelineMap.get(GUIDELINE_SECOND_THIRD_HEIGHT));
      } else if (childCount == 6) {
        removeGuideline(guidelineMap.get(GUIDELINE_FIRST_THIRD_HEIGHT));
        removeGuideline(guidelineMap.get(GUIDELINE_SECOND_THIRD_HEIGHT));
        addGuideline(guidelineMap.get(GUIDELINE_FIRST_QUARTER_HEIGHT));
        addGuideline(guidelineMap.get(GUIDELINE_SECOND_QUARTER_HEIGHT));
        addGuideline(guidelineMap.get(GUIDELINE_THIRD_QUARTER_HEIGHT));
      }
    } else {
      if (childCount == 2) {
        removeGuideline(guidelineMap.get(GUIDELINE_HALF_HEIGHT));
      } else if (childCount == 3) {
        removeGuideline(guidelineMap.get(GUIDELINE_HALF_WIDTH));
      } else if (childCount == 5) {
        removeGuideline(guidelineMap.get(GUIDELINE_FIRST_THIRD_HEIGHT));
        removeGuideline(guidelineMap.get(GUIDELINE_SECOND_THIRD_HEIGHT));
        addGuideline(guidelineMap.get(GUIDELINE_HALF_HEIGHT));
      } else if (childCount == 7) {
        removeGuideline(guidelineMap.get(GUIDELINE_FIRST_QUARTER_HEIGHT));
        removeGuideline(guidelineMap.get(GUIDELINE_SECOND_QUARTER_HEIGHT));
        removeGuideline(guidelineMap.get(GUIDELINE_THIRD_QUARTER_HEIGHT));
        addGuideline(guidelineMap.get(GUIDELINE_FIRST_THIRD_HEIGHT));
        addGuideline(guidelineMap.get(GUIDELINE_SECOND_THIRD_HEIGHT));
      }
    }
  }

  private void addGuideline(Guideline guideline) {
    constraintLayout.addView(guideline);
    guidelineInUse.add(guideline);
  }

  private void removeGuideline(Guideline guideline) {
    constraintLayout.removeView(guideline);
    guidelineInUse.remove(guideline);
  }

  private void addViewToContainer(int childCount, View view) {
    ConstraintLayout.LayoutParams params =
        new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
    constraintLayout.addView(view, childCount, params);
  }

  private void refactorConstraintsOnChilds() {
    int childCount = constraintLayout.getChildCount() - guidelineInUse.size();

    ConstraintSet set = new ConstraintSet();
    set.clone(constraintLayout);

    LiveStreamView v = null;

    for (int i = 0; i < childCount; i++) {
      v = (LiveStreamView) constraintLayout.getChildAt(i);

      if (type == TYPE_GRID) {
        set.clear(v.getId());
        set.setElevation(v.getId(), 0);
        v.setStyle(LiveStreamView.TYPE_GRID);

        switch (i) {
          case 0:
            if (childCount == 1) {
              set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                  ConstraintSet.START);
              set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                  ConstraintSet.END);
              set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                  ConstraintSet.TOP);
              set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                  ConstraintSet.BOTTOM);
            } else if (childCount == 2 || childCount == 3) {
              Guideline guideline = guidelineMap.get(GUIDELINE_HALF_HEIGHT);
              set.connect(v.getId(), ConstraintSet.TOP, guideline.getId(), ConstraintSet.TOP);
              set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                  ConstraintSet.START);
              set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                  ConstraintSet.END);
              set.connect(v.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(),
                  ConstraintSet.BOTTOM);
            } else if (childCount == 4) {
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
            } else if (childCount == 5) {
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
            } else if (childCount == 6) {
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
            } else if (childCount == 7) {
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
            } else if (childCount == 8) {
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
            if (childCount == 2) {
              Guideline guideline = guidelineMap.get(GUIDELINE_HALF_HEIGHT);
              set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                  ConstraintSet.START);
              set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                  ConstraintSet.END);
              set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                  ConstraintSet.TOP);
              set.connect(v.getId(), ConstraintSet.BOTTOM, guideline.getId(), ConstraintSet.BOTTOM);
            } else if (childCount == 3 || childCount == 4) {
              Guideline guidelineHalfHeight = guidelineMap.get(GUIDELINE_HALF_HEIGHT);
              Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
              set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                  ConstraintSet.TOP);
              set.connect(v.getId(), ConstraintSet.START, guidelineHalfWidth.getId(),
                  ConstraintSet.END);
              set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                  ConstraintSet.END);
              set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineHalfHeight.getId(),
                  ConstraintSet.BOTTOM);
            } else if (childCount == 5 || childCount == 6) {
              Guideline guidelineFirstThirdHeight = guidelineMap.get(GUIDELINE_FIRST_THIRD_HEIGHT);
              Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
              set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                  ConstraintSet.TOP);
              set.connect(v.getId(), ConstraintSet.START, guidelineHalfWidth.getId(),
                  ConstraintSet.START);
              set.connect(v.getId(), ConstraintSet.END, constraintLayout.getId(),
                  ConstraintSet.END);
              set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineFirstThirdHeight.getId(),
                  ConstraintSet.BOTTOM);
            } else if (childCount == 7 || childCount == 8) {
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
            if (childCount == 3 || childCount == 4) {
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
            } else if (childCount == 5 || childCount == 6) {
              Guideline guidelineFirstThirdHeight = guidelineMap.get(GUIDELINE_FIRST_THIRD_HEIGHT);
              Guideline guidelineHalfWidth = guidelineMap.get(GUIDELINE_HALF_WIDTH);
              set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(),
                  ConstraintSet.TOP);
              set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(),
                  ConstraintSet.START);
              set.connect(v.getId(), ConstraintSet.END, guidelineHalfWidth.getId(),
                  ConstraintSet.START);
              set.connect(v.getId(), ConstraintSet.BOTTOM, guidelineFirstThirdHeight.getId(),
                  ConstraintSet.BOTTOM);
            } else if (childCount == 7 || childCount == 8) {
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
            if (childCount == 4) {
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
            } else if (childCount == 5 || childCount == 6) {
              Guideline guidelineFirstThirdHeight = guidelineMap.get(GUIDELINE_FIRST_THIRD_HEIGHT);
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
            } else if (childCount == 7 || childCount == 8) {
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
            if (childCount == 5 || childCount == 6) {
              Guideline guidelineFirstThirdHeight = guidelineMap.get(GUIDELINE_FIRST_THIRD_HEIGHT);
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
            } else if (childCount == 7 || childCount == 8) {
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
            if (childCount == 6) {
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
            } else if (childCount == 7 || childCount == 8) {
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
            if (childCount == 7 || childCount == 8) {
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
            if (childCount == 8) {
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
      } else {
        set.clear(v.getId());

        set.connect(v.getId(), ConstraintSet.START, constraintLayout.getId(), ConstraintSet.START,
            screenUtils.dpToPx(20));
        set.constrainWidth(v.getId(), screenUtils.dpToPx(150));
        set.constrainHeight(v.getId(), screenUtils.dpToPx(LiveStreamView.MAX_HEIGHT_LIST));
        set.setElevation(v.getId(), screenUtils.dpToPx(10));
        v.setStyle(LiveStreamView.TYPE_LIST);

        switch (i) {
          case 0:
            set.connect(v.getId(), ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP,
                screenUtils.dpToPx(20));
            break;

          default:
            View previous = constraintLayout.getChildAt(i - 1);
            set.connect(v.getId(), ConstraintSet.TOP, previous.getId(), ConstraintSet.BOTTOM,
                screenUtils.dpToPx(20));
            break;
        }
      }
    }

    AutoTransition autoTransition = new AutoTransition();
    TransitionManager.beginDelayedTransition(constraintLayout, autoTransition);
    set.applyTo(constraintLayout);
  }

  private void setConfigurationScreen() {
    //if (!landscapeMode) {
    //  setSizeGridViewsInPortaitMode();
    //
    //  if (flexboxLayout.getChildCount() < 3) {
    //    flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
    //  } else {
    //    flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
    //  }
    //} else {
    //  setSizeGirdViewsInLandscapeMode();
    //
    //  if (flexboxLayout.getChildCount() >= 5) {
    //    flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
    //  } else {
    //    flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
    //  }
    //}
  }

  private void addViewInContainer(int viewIndex, LiveRowView liveRowView) {

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

  //private void setScreenSize(int openInviteWidth) {
  //this.witdhScreen = flexboxLayout.getWidth() + openInviteWidth;
  //this.heightScreen = flexboxLayout.getHeight();
  //}

  //private void setSizeGirdViewsInLandscapeMode() {
  //  int peopleOnLine = flexboxLayout.getChildCount();
  //  if (peopleOnLine < 5) {
  //    for (int i = 0; i < peopleOnLine; i++) {
  //      setWidth(i, witdhScreen / peopleOnLine);
  //      setHeight(i, heightScreen);
  //    }
  //  } else {
  //    for (int i = 0; i < peopleOnLine; i++) {
  //      if (peopleOnLine % 2 == 0) { // LOCAL VIEW
  //        setHeight(i, heightScreen / 2);
  //        setWidth(i, witdhScreen / ((peopleOnLine / 2) + 1));
  //      } else { // Impair
  //        setWidth(i, witdhScreen / ((peopleOnLine / 2) + 1));
  //        if (i == 0) {
  //          setHeight(i, heightScreen);
  //        } else {
  //          setHeight(i, heightScreen / 2);
  //        }
  //      }
  //    }
  //  }
  //}

  //private void setSizeGridViewsInPortaitMode() {
  //  int peopleOnLine = flexboxLayout.getChildCount();
  //  if (peopleOnLine % 2 == 0) {
  //    for (int i = 0; i < peopleOnLine; i++) {
  //      if (peopleOnLine > 2) {
  //        setWidth(i, (witdhScreen / 2));
  //        setHeight(i, (heightScreen / (peopleOnLine / 2)));
  //      } else {
  //        setWidth(i, (witdhScreen));
  //        setHeight(i, (heightScreen / peopleOnLine));
  //      }
  //    }
  //  } else { // IMPAIR
  //    for (int i = 0; i < peopleOnLine; i++) {
  //      setHeight(i, heightScreen / (peopleOnLine + 1));
  //      if (i == 0) {
  //        setWidth(i, (witdhScreen));
  //      } else {
  //        setWidth(i, (witdhScreen / 2));
  //      }
  //    }
  //  }
  //}
  //
  //private void setSizeLinearViews() {
  //  int peopleOnLine = flexboxLayout.getChildCount();
  //  for (int i = 0; i < peopleOnLine; i++) {
  //    setWidth(i, (witdhScreen));
  //    setHeight(i, (heightScreen / peopleOnLine));
  //  }
  //}
  //
  //private void setViewsOrder() {
  //  int peopleOnLine = flexboxLayout.getChildCount();
  //  for (int i = 0; i < peopleOnLine; i++) {
  //    if (i == 0) {
  //      setOrder(i, peopleOnLine);  // local view
  //    } else {
  //      setOrder(i, i);  // guest view
  //    }
  //  }
  //}
  //
  //private void setOrder(int index, int order) {
  //  View view = flexboxLayout.getChildAt(index);
  //  FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) view.getLayoutParams();
  //  l.order = order;
  //  view.setLayoutParams(l);
  //}
  //
  //private void setHeight(int index, int height) {
  //  View view = flexboxLayout.getChildAt(index);
  //  FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) view.getLayoutParams();
  //  l.height = height;
  //  l.flexGrow = 1;
  //  view.setLayoutParams(l);
  //}
  //
  //private void setWidth(int index, int width) {
  //  View view = flexboxLayout.getChildAt(index);
  //  FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) view.getLayoutParams();
  //  l.width = width;
  //  l.flexGrow = 1;
  //  view.setLayoutParams(l);
  //}

  public Observable<Void> onShouldCloseInvites() {
    return onShouldCloseInvites;
  }

  public Observable<Void> onChangeCallRouletteRoom() {
    return onChangeCallRouletteRoom;
  }

  public Observable<Map<String, LiveStreamView>> onLiveViewsChange() {
    return onViews;
  }
}