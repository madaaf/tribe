package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/18/2017.
 */
public class LiveInviteBottomView extends FrameLayout {

  private static final int DURATION = 300;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.txtLabel) TextViewFont txtLabel;

  @BindView(R.id.imgExpand) ImageView imgExpand;

  // VARIABLES
  private Unbinder unbinder;

  // RESOURCES
  private int widthArrowSmall, widthArrowLarge;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LiveInviteBottomView(Context context) {
    super(context);
    init();
  }

  public LiveInviteBottomView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    unbinder.unbind();

    if (subscriptions != null && subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
    }

    super.onDetachedFromWindow();
  }

  @Override protected void onFinishInflate() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_live_invite_bottom, this);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    initResources();
    initUI();
    initSubscriptions();

    super.onFinishInflate();
  }

  //////////////////////
  //      INIT        //
  //////////////////////

  private void init() {
  }

  private void initUI() {
  }

  private void initSubscriptions() {
  }

  private void initResources() {
    widthArrowSmall =
        getResources().getDimensionPixelSize(R.dimen.live_invite_bottom_arrow_width_small);
    widthArrowLarge =
        getResources().getDimensionPixelSize(R.dimen.live_invite_bottom_arrow_width_large);
  }

  ////////////////
  //   PUBLIC   //
  ////////////////

  public void showMore() {
    txtLabel.setText(R.string.live_members_menu_more);

    txtLabel.animate()
        .setInterpolator(new DecelerateInterpolator())
        .translationX(0)
        .setDuration(DURATION)
        .start();

    AnimationUtils.animateWidth(imgExpand, widthArrowLarge, widthArrowSmall, DURATION,
        new DecelerateInterpolator());
  }

  public void showLess() {
    txtLabel.setText(R.string.live_members_menu_less);

    txtLabel.animate()
        .setInterpolator(new DecelerateInterpolator())
        .translationX(widthArrowSmall - widthArrowLarge)
        .setDuration(DURATION)
        .start();

    AnimationUtils.animateWidth(imgExpand, widthArrowSmall, widthArrowLarge, DURATION,
        new DecelerateInterpolator());
  }

  //////////////////////
  //   OBSERVABLES    //
  //////////////////////
}

