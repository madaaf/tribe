package com.tribe.app.presentation.view.widget;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.preferences.MinutesOfCalls;
import com.tribe.app.presentation.utils.preferences.NumberOfCalls;
import com.tribe.app.presentation.view.utils.DialogFactory;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by madaaflak on 04/04/2017.
 */

public class EnjoyingTribeNotificationView extends FrameLayout {

  private final static int START_OFFSET_DURATION = 500;
  private final static int NOTIF_DURATION = 800;

  private final static int MIN_USER_CALL_COUNT = 10;
  private final static int MIN_USER_CALL_MINUTES = 30;

  @Inject Navigator navigator;

  @Inject @NumberOfCalls Preference<Integer> numberOfCalls;

  @Inject @MinutesOfCalls Preference<Float> minutesOfCalls;

  @BindView(R.id.enjoyingTribePopupView) LinearLayout enjoyingTribePopupView;
  @BindView(R.id.btnAction1) TextViewFont btnAction1;
  @BindView(R.id.btnAction2) TextViewFont btnAction2;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public EnjoyingTribeNotificationView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public EnjoyingTribeNotificationView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  ///////////////////
  //    PUBLIC     //
  ///////////////////

  public void displayView() {
    if (!(numberOfCalls.get() > MIN_USER_CALL_COUNT) || !(minutesOfCalls.get()
        > MIN_USER_CALL_MINUTES)) {
      return;
    }
    setVisibility(VISIBLE);
    Animation slideInAnimation =
        AnimationUtils.loadAnimation(getContext(), R.anim.alerter_slide_in_from_top);
    slideInAnimation.setStartOffset(START_OFFSET_DURATION);
    slideInAnimation.setDuration(NOTIF_DURATION);
    setAnimation(slideInAnimation);
    startAnimation(slideInAnimation);
  }

  @OnClick(R.id.btnAction1) void onClickAction1() {
    enjoyingTribePopupView.setVisibility(INVISIBLE);

    subscriptions.add(DialogFactory.dialog(getContext(),
        EmojiParser.demojizedText(getContext().getString(R.string.popup_feedback_title)),
        getContext().getString(R.string.popup_feedback_subtitle),
        getContext().getString(R.string.popup_feedback_action2),
        getContext().getString(R.string.popup_feedback_action1)).
        subscribe(positiveAction -> {
          if (positiveAction) {
            String[] emails = new String[] {
                getContext().getString(R.string.popup_feedback_tribe_mail)
            };
            navigator.composeEmail(getContext(), emails,
                getContext().getString(R.string.popup_feedback_tribe_mail_subject));
          }
          hideView();
        }));
  }

  @OnClick(R.id.btnAction2) void onClickAction2() {
    enjoyingTribePopupView.setVisibility(INVISIBLE);
    subscriptions.add(DialogFactory.dialog(getContext(),
        EmojiParser.demojizedText(getContext().getString(R.string.popup_review_playstore_title)),
        EmojiParser.demojizedText(getContext().getString(R.string.popup_review_playstore_subtitle)),
        getContext().getString(R.string.popup_review_playstore_action2),
        getContext().getString(R.string.popup_review_playstore_action1))
        .subscribe(positiveAction -> {
          if (positiveAction) {
            navigator.rateApp(getContext());
          }
          hideView();
        }));
  }
  ///////////////////
  //    PRIVATE    //
  ///////////////////

  private void initView(Context context) {

    initDependencyInjector();

    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_enjoying_tribe_popup, this, true);

    unbinder = ButterKnife.bind(this);

    btnAction1.setText(
        EmojiParser.demojizedText(getContext().getString(R.string.enjoying_tribe_popup_action1)));
    btnAction2.setText(
        EmojiParser.demojizedText(getContext().getString(R.string.enjoying_tribe_popup_action2)));
  }

  private void hideView() {
    setVisibility(GONE);
    enjoyingTribePopupView.setVisibility(VISIBLE);
  }

  ///////////////////
  //  LIFE CYCLE   //
  ///////////////////
  @Override protected void onDetachedFromWindow() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    clearAnimation();
    super.onDetachedFromWindow();
  }

  private void initDependencyInjector() {
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
