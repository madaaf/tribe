package com.tribe.app.presentation.view.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;

/**
 * Created by madaaflak on 14/03/2017.
 */

public class RatingNotificationView extends FrameLayout implements View.OnClickListener {

  private static int DURATION = 300;
  private static int ratingScore;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.btnStar1) ImageView btnStart1;
  @BindView(R.id.btnStar2) ImageView btnStart2;
  @BindView(R.id.btnStar3) ImageView btnStart3;
  @BindView(R.id.btnStar4) ImageView btnStart4;
  @BindView(R.id.btnStar5) ImageView btnStart5;

  @BindView(R.id.txtAction) TextViewFont txtAction;
  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;

  public RatingNotificationView(Context context) {
    super(context);
    initView(context, null);
  }

  public RatingNotificationView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context, attrs);
  }

  private void initView(Context context, AttributeSet attrs) {
    initDependencyInjector();

    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_rating_notification, this, true);

    unbinder = ButterKnife.bind(this);

    txtTitle.setText(EmojiParser.demojizedText(getContext().getString(R.string.live_rating_title)));

    fillStartsColors(0);
    setOnClickListener(this);
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

  ///////////////////
  //    CLICKS     //
  ///////////////////

  @OnClick(R.id.btnStar1) void onClickStar1() {
    fillStartsColors(1);
  }

  @OnClick(R.id.btnStar2) void onClickStar2() {
    fillStartsColors(2);
  }

  @OnClick(R.id.btnStar3) void onClickStar3() {
    fillStartsColors(3);
  }

  @OnClick(R.id.btnStar4) void onClickStar4() {
    fillStartsColors(4);
  }

  @OnClick(R.id.btnStar5) void onClickStar5() {
    fillStartsColors(5);
  }

  @OnClick(R.id.txtAction) void onClickTextAction() {
    // TODO set ratingScore
    hideView();
  }

  @Override public void onClick(View v) {
    fillStartsColors(0);
  }

  private void hideView() {
    animate().setDuration(DURATION).alpha(0f).withEndAction(new Runnable() {
      @Override public void run() {
        setVisibility(GONE);
        setAlpha(0f);
      }
    });
  }

  public void displayView() {
    fillStartsColors(0);
    animate().setStartDelay(DURATION).setDuration(DURATION).alpha(1f).withEndAction(new Runnable() {
      @Override public void run() {
        setVisibility(VISIBLE);
        setAlpha(1f);
      }
    });
  }

  private void fillStartsColors(int index) {
    ratingScore = index;

    if (index == 0) {
      txtAction.setText(getResources().getString(R.string.live_rating_dismiss));
    } else {
      txtAction.setText(getResources().getString(R.string.live_rating_send));
    }

    switch (index) {
      case 0:
        btnStart1.setImageResource(R.drawable.picto_rating_star);
        btnStart2.setImageResource(R.drawable.picto_rating_star);
        btnStart3.setImageResource(R.drawable.picto_rating_star);
        btnStart4.setImageResource(R.drawable.picto_rating_star);
        btnStart5.setImageResource(R.drawable.picto_rating_star);
        break;
      case 1:
        btnStart1.setImageResource(R.drawable.picto_rating_star_red);
        btnStart2.setImageResource(R.drawable.picto_rating_star);
        btnStart3.setImageResource(R.drawable.picto_rating_star);
        btnStart4.setImageResource(R.drawable.picto_rating_star);
        btnStart5.setImageResource(R.drawable.picto_rating_star);

        scaleAnim(btnStart1);

        break;
      case 2:
        btnStart1.setImageResource(R.drawable.picto_rating_star_orange);
        btnStart2.setImageResource(R.drawable.picto_rating_star_orange);

        btnStart3.setImageResource(R.drawable.picto_rating_star);
        btnStart4.setImageResource(R.drawable.picto_rating_star);
        btnStart5.setImageResource(R.drawable.picto_rating_star);

        scaleAnim(btnStart2);
        break;
      case 3:
        btnStart1.setImageResource(R.drawable.picto_rating_star_yellow);
        btnStart2.setImageResource(R.drawable.picto_rating_star_yellow);
        btnStart3.setImageResource(R.drawable.picto_rating_star_yellow);

        btnStart4.setImageResource(R.drawable.picto_rating_star);
        btnStart5.setImageResource(R.drawable.picto_rating_star);

        scaleAnim(btnStart3);
        break;
      case 4:
        btnStart1.setImageResource(R.drawable.picto_rating_star_yellow_light);
        btnStart2.setImageResource(R.drawable.picto_rating_star_yellow_light);
        btnStart3.setImageResource(R.drawable.picto_rating_star_yellow_light);
        btnStart4.setImageResource(R.drawable.picto_rating_star_yellow_light);

        btnStart5.setImageResource(R.drawable.picto_rating_star);

        scaleAnim(btnStart4);
        break;
      case 5:
        btnStart1.setImageResource(R.drawable.picto_rating_star_green);
        btnStart2.setImageResource(R.drawable.picto_rating_star_green);
        btnStart3.setImageResource(R.drawable.picto_rating_star_green);
        btnStart4.setImageResource(R.drawable.picto_rating_star_green);
        btnStart5.setImageResource(R.drawable.picto_rating_star_green);

        scaleAnim(btnStart5);
        break;
    }
  }

  private void scaleAnim(View view) {
    Animation scaleAnimation =
        android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.scale_stars);
    view.startAnimation(scaleAnimation);
  }
}
