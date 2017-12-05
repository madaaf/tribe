package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.facebook.rebound.SpringUtil;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/05/2017.
 */
public class LiveHangUpView extends FrameLayout {

  public static final int MIN_WIDTH = 75;
  public static final int MAX_WIDTH = 800;
  public static final int MAX_ROTATION = -100;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.imgPhone) ImageView imgPhone;

  // VARIABLES
  private int maxWidth, minWidth, maxWidthRotation;

  // DIMENS

  // BINDERS / SUBSCRIPTIONS
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();

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

  ///////////////////////
  //      PUBLIC       //
  ///////////////////////

  public void applyTranslationX(float x) {
    float value = Math.max(x, minWidth);
    int rotation = Math.min(
        (int) SpringUtil.mapValueFromRangeToRange(value, minWidth, maxWidthRotation, MAX_ROTATION,
            0), 0);
    imgPhone.setRotation(rotation);
    UIUtils.changeWidthOfView(this, (int) value);
  }

  ///////////////////////
  //    ANIMATIONS     //
  ///////////////////////

  ///////////////////////
  //    OBSERVABLES    //
  ///////////////////////
}