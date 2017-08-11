package com.tribe.app.presentation.view.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;

/**
 * Created by madaaflak on 17/04/2017.
 */

public class PopupContainerView extends FrameLayout {

  @StringDef({
      DISPLAY_BUZZ_POPUP, DISPLAY_DRAGING_FRIEND_POPUP, DISPLAY_INVITE_POPUP,
      DISPLAY_NEW_CALL_POPUP, DISPLAY_PROFILE_POPUP, DISPLAY_POST_IT_GAME
  }) public @interface PopupType {
  }

  public static final String DISPLAY_BUZZ_POPUP = "DISPLAY_BUZZ_POPUP";
  public static final String DISPLAY_DRAGING_FRIEND_POPUP = "DISPLAY_DRAGING_FRIEND_POPUP";
  public static final String DISPLAY_NEW_CALL_POPUP = "DISPLAY_NEW_CALL_POPUP";
  public static final String DISPLAY_INVITE_POPUP = "DISPLAY_INVITE_POPUP";
  public static final String DISPLAY_PROFILE_POPUP = "DISPLAY_PROFILE_POPUP";
  public static final String DISPLAY_POST_IT_GAME = "DISPLAY_POST_IT_GAME";

  private static int DURATION_EXIT_POPUP = 300;
  private static double TENSION = 400;
  private static double DAMPER = 20;
  @Inject ScreenUtils screenUtils;

  @BindView(R.id.nativeDialogsContainer) FrameLayout nativeDialogsContainer;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private int defaultMargin, tooltipFirstGameHeight;

  private int marginBottom = 0, marginTop = 0, marginLeft = 0, marginRight = 0;
  private boolean isInBottom = false, isInTop = false, isInLeft = false, isInRight = false,
      isCentred = false;

  public PopupContainerView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public PopupContainerView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  public void displayPopup(View v, @PopupType String type, String txt) {
    setVisibility(View.VISIBLE);
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

            MarginLayoutParams margins = ((MarginLayoutParams) v.getLayoutParams());

            if (type.equals(DISPLAY_INVITE_POPUP)
                || type.equals(DISPLAY_NEW_CALL_POPUP)
                || type.equals(DISPLAY_PROFILE_POPUP)
                || type.equals(DISPLAY_POST_IT_GAME)) {

              if (type.equals(DISPLAY_INVITE_POPUP)) {
                isInRight = true;
                marginTop = viewPositionInScreen[1] + v.getHeight();
                marginRight = defaultMargin;
              } else if (type.equals(DISPLAY_PROFILE_POPUP)) {
                isInLeft = true;
                marginTop = viewPositionInScreen[1] + v.getHeight();
                marginLeft = margins.leftMargin - defaultMargin;
              } else if (type.equals(DISPLAY_POST_IT_GAME)) {
                marginLeft = screenUtils.dpToPx(2.5f);
                marginBottom =
                    margins.bottomMargin + v.getHeight() + defaultMargin + tooltipFirstGameHeight;
              } else {
                marginBottom = margins.bottomMargin + v.getHeight() + defaultMargin;
              }
            } else {
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
    tooltipFirstGameHeight =
        context.getResources().getDimensionPixelSize(R.dimen.game_tooltip_first_height);
    unbinder = ButterKnife.bind(this);
    setOnTouchListener((v, event) -> {
      hideViews();
      return false;
    });
  }

  private int getDrawable(@PopupType String type) {
    switch (type) {
      case DISPLAY_BUZZ_POPUP:
        return R.layout.buzz_popup_view;
      case DISPLAY_DRAGING_FRIEND_POPUP:
        return R.layout.drag_friend_popup_view;
      case DISPLAY_INVITE_POPUP:
        return R.layout.invite_popup_view;
      case DISPLAY_NEW_CALL_POPUP:
        return R.layout.new_call_popup_view;
      case DISPLAY_PROFILE_POPUP:
        return R.layout.profile_popup_view;
      case DISPLAY_POST_IT_GAME:
        return R.layout.post_it_popup;
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
    SpringSystem springSystem = SpringSystem.create();
    Spring spring = springSystem.createSpring();
    SpringConfig config = new SpringConfig(TENSION, DAMPER);
    spring.setSpringConfig(config);

    spring.addListener(new SimpleSpringListener() {
      @Override public void onSpringUpdate(Spring spring) {
        float value = (float) spring.getCurrentValue();
        view.setScaleX(value);
        view.setScaleY(value);
      }
    });
    spring.setEndValue(1);
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
      ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(v, "scaleX", 0f);
      ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(v, "scaleY", 0f);
      scaleDownX.setDuration(DURATION_EXIT_POPUP);
      scaleDownY.setDuration(DURATION_EXIT_POPUP);

      AnimatorSet scaleDown = new AnimatorSet();
      scaleDown.play(scaleDownX).with(scaleDownY);
      scaleDownX.addUpdateListener(valueAnimator -> {
        float value = (float) valueAnimator.getAnimatedValue();
        v.setAlpha(value);
      });

      scaleDownX.addListener(new AnimatorListenerAdapter() {
        @Override public void onAnimationEnd(Animator animation) {
          super.onAnimationEnd(animation);
          if (v != null && v.getParent() != null) {
            ((ViewGroup) v.getParent()).removeView(v);
          }
          setVisibility(View.GONE);
        }
      });
      scaleDown.start();
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
