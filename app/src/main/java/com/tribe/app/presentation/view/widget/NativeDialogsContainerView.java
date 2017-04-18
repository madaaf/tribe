package com.tribe.app.presentation.view.widget;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Created by madaaflak on 17/04/2017.
 */

public class NativeDialogsContainerView extends FrameLayout {
  @StringDef({ DISPLAY_BUZZ_POPUP, DISPLAY_DRAGING_FRIEND_POPUP }) public @interface PopupType {

  }

  private static final String DISPLAY_BUZZ_POPUP = "DISPLAY_BUZZ_POPUP";
  private static final String DISPLAY_DRAGING_FRIEND_POPUP = "DISPLAY_DRAGING_FRIEND_POPUP";

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  @Inject ScreenUtils screenUtils;

  @BindView(R.id.nativeDialogsContainer) FrameLayout nativeDialogsContainer;

  public NativeDialogsContainerView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public NativeDialogsContainerView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  private void initView(Context context) {
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.native_dialogs_container, this, true);

    unbinder = ButterKnife.bind(this);
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

  private int getDrawable(@PopupType String type) {
    switch (type) {
      case DISPLAY_BUZZ_POPUP:
        return R.layout.buzz_popup_view;
      case DISPLAY_DRAGING_FRIEND_POPUP:
        return R.layout.drag_friend_popup_view;
    }
    return 0;
  }

  public void displayPopup(View v, @PopupType String type) {
    int top = v.getTop();
    int left = v.getLeft();
    int bottom = v.getBottom();
    int centerX = v.getTop() + (v.getWidth() / 2);
    int centerY = v.getLeft() + (v.getHeight() / 2);

    Timber.e("SOEF CENTER X " + centerX + " " + centerY);

    boolean isInBottom;
    boolean isInTop;
    boolean isInLeft;
    boolean isInRight;
    boolean isInCenter;


  /*  float topHorizontal = ;
    float midelHorizontal = screenUtils.getHeightDp() / 2;
    float bottomHorizontal = screenUtils.getHeightDp() / 2;*/
    float unityHoritonal = screenUtils.getHeightDp() / 3;
    float unityVertical = screenUtils.getWidthDp() / 2;

    if (0 < centerY && centerY < unityHoritonal) {
      isInTop = true;
      isInBottom = false;
      isInCenter = false;
    } else if (2 * unityHoritonal < centerY && centerY < 3 * unityHoritonal) {
      isInTop = false;
      isInBottom = true;
      isInCenter = false;
    } else {
      isInTop = false;
      isInBottom = false;
      isInCenter = true;
    }

    if (0 < centerX && centerX < unityVertical) {
      isInLeft = true;
      isInRight = false;
    } else {
      isInLeft = false;
      isInRight = true;
    }

    if (isInBottom) {
      View view = inflater.inflate(R.layout.buzz_popup_view, null);
      FrameLayout.LayoutParams lp =
          new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
              ViewGroup.LayoutParams.WRAP_CONTENT);
      lp.gravity = Gravity.BOTTOM | Gravity.CENTER;
      view.setLayoutParams(lp);
      lp.setMargins(0, 0, 0, v.getHeight() + screenUtils.dpToPx(10));
      nativeDialogsContainer.addView(view);
    }

    if (true) {
      View view = inflater.inflate(R.layout.drag_friend_popup_view, null);
      FrameLayout.LayoutParams lp =
          new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
              ViewGroup.LayoutParams.WRAP_CONTENT);
      lp.gravity = Gravity.CENTER;
      view.setLayoutParams(lp);
      nativeDialogsContainer.addView(view);
    }
  }
}
