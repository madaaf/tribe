package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

  // VARIABLES
  private Unbinder unbinder;
  private @TribeRoomViewType int type;

  @BindView(R.id.flexbox_layout) FlexboxLayout flexboxLayout;

  // @BindView(R.id.viewLocalLive) LiveLocalView viewLocalLive;

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
    // instanev of new thikngs
    //initResources();
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_flexbox, this);
    unbinder = ButterKnife.bind(this);

    flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
    flexboxLayout.setAlignContent(ALIGN_CONTENT_STRETCH);
    flexboxLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_STRETCH);
    flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP);
    initSubscriptions();
  }

  public void addView(LiveRowView liveRowView, ViewGroup.LayoutParams params) {
    int viewIndex = flexboxLayout.getChildCount();
    if (viewIndex < 8) {
      organiseGrid(viewIndex, liveRowView);
      // index starts from 0. New View's index is N if N views ([0, 1, 2, ... N-1])

      // exist.
      Timber.e("ok" + "ok3");
    }
    //super.addView(liveRowView, params);
    Timber.e("ok" + "ok");
  }

  private void initSubscriptions() {

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
 /*
  public void addView(View liveRowView, ViewGroup.LayoutParams params) {
    super.addView(liveRowView, params);
    test.setText("SOEF");
  if (flexboxLayout == null) {
      return;
    }
    int viewIndex = flexboxLayout.getChildCount();
    if (viewIndex < 8) {
      organiseGrid(viewIndex);
      // index starts from 0. New View's index is N if N views ([0, 1, 2, ... N-1])

      // exist.
      Timber.e("ok" + "ok3");
    }
    //super.addView(liveRowView, params);
    Timber.e("ok" + "ok");
  }*/

  public void setType(@TribeRoomViewType int type) {
    if (this.type == type) return;

    this.type = type;

    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);

      if (child instanceof LiveRowView) {
        LiveRowView liveRowView = (LiveRowView) child;
        liveRowView.setRoomType(type);
      }
    }

    //requestLayout();
  }

  public @TribeRoomViewType int getType() {
    return type;
  }

/*  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    if (getChildCount() > 0) {
      if (type == GRID) {
        //layoutChildrenGrid();
      } else {
        //layoutChildrenLinear();
      }
    }
  }*/

  private LiveRoomView createBaseFlexItemTextView(int index) {
    LiveRoomView textView = new LiveRoomView(getContext());
    //textView.setBackgroundResource(R.drawable.flex_item_background);
    //textView.setText(String.valueOf(index + 1));
    // textView.setGravity(Gravity.CENTER);
    return textView;
  }

  private void organiseGrid(int viewIndex, LiveRowView textView) {
    //LiveRoomView textView = createBaseFlexItemTextView(viewIndex, liveRowView);
    //textView.setGravity(Gravity.CENTER);
    FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(1, 1);

    switch (viewIndex) {
      case 0:
      case 1:
        Log.e("0", "0");

        LiveLocalView viewLocalLive = (LiveLocalView) flexboxLayout.getChildAt(0);
        if (viewLocalLive.getParent() != null) {
          ((ViewGroup) viewLocalLive.getParent()).removeView(viewLocalLive); // <- fix
        }

        viewLocalLive.setVisibility(VISIBLE);
        FlexboxLayout.LayoutParams lp1 = new FlexboxLayout.LayoutParams(1, 1);
        lp1.flexGrow = 1;
        viewLocalLive.setLayoutParams(lp1);
        flexboxLayout.addView(viewLocalLive);

        Log.e("1", "1");
        lp.flexGrow = 1;
        textView.setLayoutParams(lp);
        flexboxLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.orange_1));
        textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.orange_1));
        //  textView.setText(viewIndex + "B : " + lp.order);
        flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
        flexboxLayout.addView(textView);
        break;
      case 2:
        Log.e("2", "2");

        setOrfer(1, 1);  //B
        lp.order = 2;    //C
        setOrfer(0, 3);  //A

        changeWhidth(0, flexboxLayout.getWidth());
        changeWhidth(1, flexboxLayout.getWidth() / 2);
        lp.minWidth = flexboxLayout.getWidth() / 2; // C

        textView.setLayoutParams(lp);
        flexboxLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
        textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
        //  textView.setText(viewIndex + "C: " + lp.order);
        flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
        flexboxLayout.addView(textView);
        break;
      case 3:
        Log.e("3", "3");
        changeWhidth(0, flexboxLayout.getWidth() / 2);
        changeWhidth(1, flexboxLayout.getWidth() / 2);
        changeWhidth(2, flexboxLayout.getWidth() / 2);

        setOrfer(0, 3);  //A
        setOrfer(1, 1);  //B
        setOrfer(2, 2);  //C
        lp.order = 4;

        lp.minWidth = flexboxLayout.getWidth() / 2;
        flexboxLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_group));
        textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_group));
        textView.setLayoutParams(lp);
        // textView.setText(viewIndex + "D: " + lp.order);
        flexboxLayout.addView(textView);
        break;
      case 4:
        Log.e("4", "4");

        setOrfer(0, 3);  //A
        setOrfer(1, 1);  //B
        setOrfer(2, 2);  //C
        setOrfer(3, 4);  //D
        lp.order = 5;

        lp.minWidth = flexboxLayout.getWidth();
        textView.setLayoutParams(lp);
        lp.flexGrow = 1;
        flexboxLayout.setBackgroundColor(
            ContextCompat.getColor(getContext(), R.color.purple_group));
        textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.purple_group));
        //textView.setText(viewIndex + "E: " + lp.order);
        flexboxLayout.addView(textView);
        break;
      case 5:
        Log.e("5", "5");
        changeWhidth(4, flexboxLayout.getWidth() / 2);

        setOrfer(0, 3);  //A
        setOrfer(1, 1);  //B
        setOrfer(2, 2);  //C
        setOrfer(3, 4);  //D
        setOrfer(4, 5);  //E
        lp.order = 6;

        //  lp.flexGrow = 1;
        lp.minWidth = flexboxLayout.getWidth() / 2;
        textView.setLayoutParams(lp);
        flexboxLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));
        textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));
        // textView.setText(viewIndex + "F: " + lp.order);
        flexboxLayout.addView(textView);
        break;
      case 6:
        Log.e("6", "6");

        setOrfer(0, 3);  //A
        setOrfer(1, 1);  //B
        setOrfer(2, 2);  //C
        setOrfer(3, 4);  //D
        setOrfer(4, 5);  //E
        setOrfer(5, 6);  //F
        lp.order = 7;

        lp.minWidth = flexboxLayout.getWidth();
        textView.setLayoutParams(lp);
        lp.flexGrow = 1;
        flexboxLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.yellow));
        textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.yellow));
        // textView.setText(viewIndex + "G: " + lp.order);
        flexboxLayout.addView(textView);
        break;
      case 7:
        Log.e("7", "7");

        setOrfer(0, 3);  //A
        setOrfer(1, 1);  //B
        setOrfer(2, 2);  //C
        setOrfer(3, 4);  //D
        setOrfer(4, 5);  //E
        setOrfer(5, 6);  //F
        setOrfer(6, 7);  //G

        changeWhidth(6, flexboxLayout.getWidth() / 2);
        lp.order = 8; //H

        lp.minWidth = flexboxLayout.getWidth() / 2;
        flexboxLayout.setBackgroundColor(
            ContextCompat.getColor(getContext(), R.color.grey_authentication));
        textView.setBackgroundColor(
            ContextCompat.getColor(getContext(), R.color.grey_authentication));
        textView.setLayoutParams(lp);
        //textView.setText(viewIndex + "H: " + lp.order);
        flexboxLayout.addView(textView);
        break;
      case 8:
        Log.e("8", "8");
        break;
    }
  }

  private void setOrfer(int index, int order) {
    View view = flexboxLayout.getChildAt(index);
    FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) view.getLayoutParams();
    l.order = order;
    view.setLayoutParams(l);
  }

  private void changeWhidth(int index, int width) {
    View view = flexboxLayout.getChildAt(index);
    FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) view.getLayoutParams();
    l.minWidth = width;
    view.setLayoutParams(l);
  }

  //////////////////
  //  OBSERVABLES //
  //////////////////
}
