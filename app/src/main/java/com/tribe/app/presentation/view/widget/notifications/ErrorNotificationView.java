package com.tribe.app.presentation.view.widget.notifications;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by madaaflak on 14/03/2017.
 */

public class ErrorNotificationView extends FrameLayout {

  public static final String DISPLAY_ERROR_NOTIF = "DISPLAY_ERROR_NOTIF";

  private final static int DURATION = 300;
  private final static int DURATION_COLOR = 300;
  private final static int DELAY = 0;
  private final static float OVERSHOOT = 0.5f;
  private final static float SCALE = 1.10f;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;

  // OBSERVABLES
  Subscription timerSubscription;

  public ErrorNotificationView(Context context) {
    super(context);
    initView(context, null);
  }

  public ErrorNotificationView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context, attrs);
  }

  @Override protected void onDetachedFromWindow() {
    if (timerSubscription != null) {
      timerSubscription.unsubscribe();
      timerSubscription = null;
    }
    super.onDetachedFromWindow();
  }

  private void initView(Context context, AttributeSet attrs) {
    initDependencyInjector();

    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_room_full_notification, this, true);

    unbinder = ButterKnife.bind(this);

    txtTitle.setText(
        EmojiParser.demojizedText(getContext().getString(R.string.live_join_impossible)));
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  ///////////////////
  //    PRIVATE    //
  ///////////////////

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  private void hideView() {
    if (timerSubscription != null) timerSubscription.unsubscribe();

    Animation slideInAnimation =
        AnimationUtils.loadAnimation(getContext(), R.anim.alerter_slide_out_to_top);
    setAnimation(slideInAnimation);
    slideInAnimation.setAnimationListener(new AnimationListenerAdapter() {
      @Override public void onAnimationStart(Animation animation) {
        super.onAnimationStart(animation);
        setClickable(false);
        setOnClickListener(null);
      }

      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        clearAnimation();
        setVisibility(GONE);
      }
    });
    startAnimation(slideInAnimation);
  }

  private void setTimer() {
    timerSubscription = Observable.timer(5, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> hideView());
  }

  ////////////
  // PUBLIC //
  ////////////

  public void displayView() {
    setTimer();
    setVisibility(VISIBLE);
    Animation slideInAnimation =
        AnimationUtils.loadAnimation(getContext(), R.anim.alerter_slide_in_from_top);
    slideInAnimation.setStartOffset(1000);
    slideInAnimation.setDuration(800);
    setAnimation(slideInAnimation);
    startAnimation(slideInAnimation);
  }
}
