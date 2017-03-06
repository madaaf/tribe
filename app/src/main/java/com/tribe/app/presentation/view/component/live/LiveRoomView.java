package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.google.android.flexbox.FlexboxLayout;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;
import timber.log.Timber;

import static com.google.android.flexbox.FlexboxLayout.ALIGN_CONTENT_STRETCH;

/**
 * Created by tiago on 22/01/2017.
 */

public class LiveRoomView extends FrameLayout {

  @IntDef({ GRID, LINEAR }) public @interface TribeRoomViewType {
  }

  public static final int GRID = 0;
  public static final int LINEAR = 1;
  private static final int DEFAULT_TYPE = GRID;
  private static boolean onDropEnabled = false;
  // VARIABLES
  private Unbinder unbinder;
  private @TribeRoomViewType int type;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.flexbox_layout) FlexboxLayout flexboxLayout;

  public LiveRoomView(Context context) {
    super(context);
    type = DEFAULT_TYPE;
    init();
  }

  public LiveRoomView(Context context, AttributeSet attrs) {
    super(context, attrs);
    type = DEFAULT_TYPE;
    init();
  }

  private void init() {
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_flexbox, this);
    unbinder = ButterKnife.bind(this);

    flexboxLayout.setAlignContent(ALIGN_CONTENT_STRETCH);
    flexboxLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_STRETCH);
    flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP);
  }

  public void removeView(LiveRowView view) {
    if (!onDropEnabled) {
      flexboxLayout.removeView(view);
    }
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

  public void onDropEnabled(Boolean enabled) {
    //Timber.e("set Value onDropEnabled " + enabled);
    this.onDropEnabled = enabled;
    if (enabled) {
      LiveRowView lastViewAdded =
          (LiveRowView) flexboxLayout.getChildAt(flexboxLayout.getChildCount() - 1);
      FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) lastViewAdded.getLayoutParams();
      l.maxHeight = flexboxLayout.getHeight() / 3;
      l.flexGrow = 1;

    /*  LayoutTransition itemLayoutTransition = new LayoutTransition();
      itemLayoutTransition.enableTransitionType(CHANGING);
      itemLayoutTransition.disableTransitionType(DISAPPEARING);
      itemLayoutTransition.setDuration(300);
      lastViewAdded.setLayoutTransition(itemLayoutTransition);*/

      ResizeAnimation resizeAnimation =
          new ResizeAnimation(lastViewAdded, flexboxLayout.getHeight() / 3, 100);
      resizeAnimation.setDuration(500);
      lastViewAdded.startAnimation(resizeAnimation);

      lastViewAdded.setLayoutParams(l);
      Timber.e("SOEF set HEIGHT MAX");
    } else {
      LiveRowView lastViewAdded =
          (LiveRowView) flexboxLayout.getChildAt(flexboxLayout.getChildCount() - 1);
      FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) lastViewAdded.getLayoutParams();
      l.maxHeight = 100;
      l.flexGrow = 1;

      ResizeAnimation resizeAnimation =
          new ResizeAnimation(lastViewAdded, 100, flexboxLayout.getHeight() / 3);
      resizeAnimation.setDuration(500);
      lastViewAdded.startAnimation(resizeAnimation);

  /*    lastViewAdded.animate()
          .scaleY(100 / (flexboxLayout.getHeight() / 3))
          .setDuration(3000)
          .setListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {
            }

            @Override public void onAnimationEnd(Animator animation) {
              l.height = 100;
              lastViewAdded.setLayoutParams(l);
            }

            @Override public void onAnimationCancel(Animator animation) {
            }

            @Override public void onAnimationRepeat(Animator animation) {
            }
          })
          .start();*/
    }
  }

  public void addView(LiveRowView liveRowView, ViewGroup.LayoutParams params) {
    int viewIndex = flexboxLayout.getChildCount();
    addViewInRow(viewIndex, liveRowView);
    setRowsOrder();
    if (type == GRID) {
      organizeGridParam();
      setRowsGridWidth();
    }
  }

  public void setType(@TribeRoomViewType int type) {
    if (this.type == type) return;
    this.type = type;
    if (type == GRID) {
      setRowsGridWidth();
      organizeGridParam();
    } else {
      flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
    }

    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child instanceof LiveRowView) {
        LiveRowView liveRowView = (LiveRowView) child;
        liveRowView.setRoomType(type);
      }
    }
  }

  public @TribeRoomViewType int getType() {
    return type;
  }

  private void addViewInRow(int viewIndex, LiveRowView liveRowView) {
    flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
    FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(1, 1);
    lp.flexGrow = 1;

    switch (viewIndex) {
      case 0:
      case 1:
        LiveLocalView viewLocalLive = (LiveLocalView) flexboxLayout.getChildAt(0);
        if (viewLocalLive.getParent() != null) {
          ((ViewGroup) viewLocalLive.getParent()).removeView(viewLocalLive);
        }

        viewLocalLive.setVisibility(VISIBLE);
        FlexboxLayout.LayoutParams lp1 = new FlexboxLayout.LayoutParams(1, 1);
        lp1.flexGrow = 1;
        viewLocalLive.setLayoutParams(lp1);
        flexboxLayout.addView(viewLocalLive);

        liveRowView.setLayoutParams(lp);
        flexboxLayout.addView(liveRowView);
        break;
      case 2:
      default:
        Timber.e("SOEF  addViewInRow ");
        if (true) {
          lp.maxHeight = 100;
          Timber.e("SOEF  set max height ");
        }
        liveRowView.setLayoutParams(lp);
        flexboxLayout.addView(liveRowView);
    }
  }

  private void organizeGridParam() {
    flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
    switch (flexboxLayout.getChildCount()) {
      case 2:
        flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
        break;
    }
  }

  private void setRowsOrder() {
    int peopleOnLine = flexboxLayout.getChildCount();
    for (int i = 0; i < peopleOnLine; i++) {
      if (i == 0) {
        setOrder(i, peopleOnLine);  // local view
      } else {
        setOrder(i, i);  // guest view
      }
    }
  }

  private void setOrder(int index, int order) {
    View view = flexboxLayout.getChildAt(index);
    FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) view.getLayoutParams();
    l.order = order;
    view.setLayoutParams(l);
  }

  private void setRowsGridWidth() {
    int peopleOnLine = flexboxLayout.getChildCount();
    if (peopleOnLine % 2 == 0) {
      for (int i = 0; i < peopleOnLine; i++) {
        setWidth(i, (flexboxLayout.getWidth() / 2));
      }
    } else {
      for (int i = 0; i < peopleOnLine; i++) {
        if (i == 0) {
          setWidth(i, (flexboxLayout.getWidth()));
        } else {
          setWidth(i, (flexboxLayout.getWidth() / 2));
        }
      }
    }
  }

  private void setWidth(int index, int width) {
    View view = flexboxLayout.getChildAt(index);
    FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) view.getLayoutParams();
    l.width = width - (LiveInviteView.WIDTH / 2);
    l.flexGrow = 1;
    view.setLayoutParams(l);
  }

/*
        flexboxLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));
        liveRowView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));

 */

  public class ResizeAnimation extends Animation {
    final int targetHeight;
    View view;
    int startHeight;

    public ResizeAnimation(View view, int targetHeight, int startHeight) {
      this.view = view;
      this.targetHeight = targetHeight;
      this.startHeight = startHeight;
    }

    @Override protected void applyTransformation(float interpolatedTime, Transformation t) {
      int newHeight = (int) (startHeight + targetHeight * interpolatedTime);
      //to support decent animation, change new heigt as Nico S. recommended in comments
      //int newHeight = (int) (startHeight+(targetHeight - startHeight) * interpolatedTime);
      view.getLayoutParams().height = newHeight;
      view.requestLayout();
    }

    @Override public void initialize(int width, int height, int parentWidth, int parentHeight) {
      super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override public boolean willChangeBounds() {
      return true;
    }
  }
}
