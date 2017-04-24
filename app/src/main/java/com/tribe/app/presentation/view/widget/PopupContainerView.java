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
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;

/**
 * Created by madaaflak on 17/04/2017.
 */

public class PopupContainerView extends FrameLayout {
  @StringDef({ DISPLAY_BUZZ_POPUP, DISPLAY_DRAGING_FRIEND_POPUP }) public @interface PopupType {

  }

  public static final String DISPLAY_BUZZ_POPUP = "DISPLAY_BUZZ_POPUP";
  public static final String DISPLAY_DRAGING_FRIEND_POPUP = "DISPLAY_DRAGING_FRIEND_POPUP";
  private static int DURATION_EXIT_POPUP = 800;
  private static float OVERSHOOT_TENSION = 1.5f;
  @Inject ScreenUtils screenUtils;

  @BindView(R.id.nativeDialogsContainer) FrameLayout nativeDialogsContainer;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private int defaultMargin;
  private int marginBottom = 0;
  private int marginTop = 0;
  private int marginLeft = 0;
  private int marginRight = 0;

  private boolean isInBottom = false;
  private boolean isInTop = false;
  private boolean isInLeft = false;
  private boolean isInRight = false;
  private boolean isCentred = false;
  private boolean buzzPopupDisplayed = false;
  private boolean dragFriendPopupDisplayed = false;

  public PopupContainerView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public PopupContainerView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  public void displayPopup(View v, @PopupType String type, String txt) {
    resetContainer();

    v.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {

            v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            int[] viewPositionInScreen = new int[2];
            v.getLocationOnScreen(viewPositionInScreen);
            int centerX = viewPositionInScreen[0] + (v.getWidth() / 2);
            int centerY = viewPositionInScreen[1] + (v.getHeight() / 2);

            float unityHorizontal = screenUtils.getHeightPx() / 3;
            float verticalUnit = screenUtils.getWidthPx() / 3;

            if (0 < centerY && centerY < unityHorizontal) {
              isInTop = true;
            } else if (2 * unityHorizontal < centerY && centerY < 3 * unityHorizontal) {
              isInBottom = true;
            } else {
              isCentred = true;
            }

            if (0 < centerX && centerX < verticalUnit) {
              isInLeft = true;
            } else if (2 * verticalUnit < centerX && centerX < 3 * verticalUnit) {
              isInRight = true;
            } else {
              isCentred = true;
            }

            if (isInBottom) {
              marginBottom = v.getHeight() + defaultMargin;
            } else if (isInTop) {
              marginTop = v.getHeight() + defaultMargin;
            }

            if (isInLeft) {
              marginLeft = v.getWidth() + defaultMargin;
            } else if (isInRight) {
              marginRight = v.getWidth() + defaultMargin;
            }

            displayView(type);

            TextViewFont text = (TextViewFont) findViewById(R.id.nativeDialogsTxt);
            if (txt != null && text != null) {
              text.setText(txt);
            }
          }
        });
  }

  ///////////////////////
  //       PRIVATE     //
  ///////////////////////

  private void initView(Context context) {
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.native_dialogs_container, this, true);
    defaultMargin = screenUtils.dpToPx(5);
    unbinder = ButterKnife.bind(this);
    setOnTouchListener((v, event) -> {
      hideViews();
      return true;
    });
  }

  private int getDrawable(@PopupType String type) {
    switch (type) {
      case DISPLAY_BUZZ_POPUP:
        buzzPopupDisplayed = true;
        return R.layout.buzz_popup_view;
      case DISPLAY_DRAGING_FRIEND_POPUP:
        dragFriendPopupDisplayed = true;
        return R.layout.drag_friend_popup_view;
    }
    return 0;
  }

  private void resetContainer() {
    marginBottom = 0;
    marginTop = 0;
    marginLeft = 0;
    marginRight = 0;

    isInBottom = false;
    isInTop = false;
    isInLeft = false;
    isInRight = false;
    isCentred = false;

    hideViews();
  }

  private void displayView(@PopupType String type) {
    View view = inflater.inflate(getDrawable(type), null);
    setLayoutParam(view);
    nativeDialogsContainer.addView(view);

    Animation scaleAnimation = android.view.animation.AnimationUtils.loadAnimation(getContext(),
        R.anim.scale_appear_popup);
    scaleAnimation.setDuration(DURATION_EXIT_POPUP);
    scaleAnimation.setFillAfter(true);
    scaleAnimation.setInterpolator(new OvershootInterpolator(OVERSHOOT_TENSION));
    startAnimation(scaleAnimation);
  }

  private void setLayoutParam(View view) {
    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);

    int verticalGravity = Gravity.CENTER_VERTICAL;
    int horizontalGravity = Gravity.CENTER_HORIZONTAL;

    if (isInBottom) {
      verticalGravity = Gravity.BOTTOM;
    } else if (isInTop) {
      verticalGravity = Gravity.TOP;
    }

    if (isInLeft) {
      horizontalGravity = Gravity.START;
    } else if (isInRight) {
      horizontalGravity = Gravity.END;
    }

    lp.gravity = horizontalGravity | verticalGravity;
    lp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
    view.setLayoutParams(lp);
  }

  private void hideViews() {
    for (int i = 0; i < nativeDialogsContainer.getChildCount(); i++) {
      View v = nativeDialogsContainer.getChildAt(i);
      ScaleAnimation scale = new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f,
          Animation.RELATIVE_TO_SELF, 0.5f);
      scale.setDuration(DURATION_EXIT_POPUP);
      scale.setFillAfter(true);
      scale.setAnimationListener(new AnimationListenerAdapter() {
        @Override public void onAnimationEnd(Animation animation) {
          super.onAnimationEnd(animation);
          ((ViewGroup) v.getParent()).removeView(v);
          if (nativeDialogsContainer != null && dragFriendPopupDisplayed && buzzPopupDisplayed) {
            setOnTouchListener(null);
          }
        }
      });
      v.startAnimation(scale);
    }
  }

  ///////////////////////
  //     CYCLE LIFE    //
  ///////////////////////

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
