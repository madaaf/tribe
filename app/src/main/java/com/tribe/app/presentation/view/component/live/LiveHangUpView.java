package com.tribe.app.presentation.view.component.live;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.facebook.rebound.SpringUtil;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/05/2017.
 */
public class LiveHangUpView extends FrameLayout {

  private static final int DURATION = 300;

  public static final int MIN_WIDTH = 75;
  public static final int MAX_WIDTH = 800;
  public static final int MAX_ROTATION = -100;

  @Inject ScreenUtils screenUtils;

  @Inject StateManager stateManager;

  @BindView(R.id.imgPhone) ImageView imgPhone;

  @BindView(R.id.viewCard) CardView viewCard;

  @BindView(R.id.txtHangUp) TextViewFont txtHangUp;

  // VARIABLES
  private int maxWidth, minWidth, maxWidthRotation;

  // DIMENS

  // BINDERS / SUBSCRIPTIONS
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onEndCall = PublishSubject.create();

  public LiveHangUpView(Context context) {
    super(context);
  }

  public LiveHangUpView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_hang_up, this);
    unbinder = ButterKnife.bind(this);

    ApplicationComponent applicationComponent =
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent();
    applicationComponent.inject(this);
    screenUtils = applicationComponent.screenUtils();

    initResources();
    initUI();
    initSubscriptions();
  }

  public void dispose() {

  }

  private void initUI() {
    setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red_hangup));
    imgPhone.setRotation(MAX_ROTATION);
  }

  private void initResources() {
    maxWidth = screenUtils.dpToPx(MAX_WIDTH);
    minWidth = screenUtils.dpToPx(MIN_WIDTH);
    maxWidthRotation = screenUtils.getWidthPx() >> 1;
  }

  private void initSubscriptions() {

  }

  @OnClick(R.id.viewCard) void onHangUp() {
    if (stateManager.shouldDisplay(StateManager.LEAVING_ROOM_POPUP)) {
      subscriptions.add(DialogFactory.dialog(getContext(),
          EmojiParser.demojizedText(getContext().getString(R.string.tips_leavingroom_title)),
          EmojiParser.demojizedText(getContext().getString(R.string.tips_leavingroom_message)),
          getContext().getString(R.string.tips_leavingroom_action1),
          getContext().getString(R.string.tips_leavingroom_action2))
          .filter(x -> x == true)
          .subscribe(a -> onEndCall.onNext(null)));
      stateManager.addTutorialKey(StateManager.LEAVING_ROOM_POPUP);
    } else {
      onEndCall.onNext(null);
    }
  }

  ///////////////////////
  //      PUBLIC       //
  ///////////////////////

  public void applyTranslationX(float x) {
    float value = Math.max(x, minWidth);
    int rotation = Math.min(
        (int) SpringUtil.mapValueFromRangeToRange(value, minWidth, maxWidthRotation, MAX_ROTATION,
            0), 0);
    imgPhone.setRotation(rotation);
    UIUtils.changeWidthOfView(this, (int) value + screenUtils.dpToPx(5));
  }

  public int getMaxWidth() {
    return (int) (((float) 2 / 3) * screenUtils.getWidthPx());
  }

  public void showEndCall() {
    ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, screenUtils.dpToPx(20));
    valueAnimator.setDuration(DURATION);
    valueAnimator.addUpdateListener(animation -> {
      float value = (float) animation.getAnimatedValue();
      viewCard.setCardElevation(value);
    });
    valueAnimator.setInterpolator(new DecelerateInterpolator());
    valueAnimator.start();

    AnimationUtils.fadeIn(txtHangUp, DURATION);
  }

  public void hideEndCall() {
    ValueAnimator valueAnimator = ValueAnimator.ofFloat(viewCard.getCardElevation(), 0);
    valueAnimator.setDuration(DURATION);
    valueAnimator.addUpdateListener(animation -> {
      float value = (float) animation.getAnimatedValue();
      viewCard.setCardElevation(value);
    });
    valueAnimator.setInterpolator(new DecelerateInterpolator());
    valueAnimator.start();

    AnimationUtils.fadeOut(txtHangUp, DURATION);
  }

  public View getHangUpButton() {
    return viewCard;
  }

  ///////////////////////
  //    ANIMATIONS     //
  ///////////////////////

  ///////////////////////
  //    OBSERVABLES    //
  ///////////////////////

  public Observable<Void> onEndCall() {
    return onEndCall;
  }
}