package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.CircleView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.model.TribeGuest;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveWaitingView extends FrameLayout {

  private final static int DURATION_PULSE = 3000;

  @Inject ScreenUtils screenUtils;

  @Inject PaletteGrid paletteGrid;

  @BindView(R.id.avatar) AvatarView avatar;

  @BindView(R.id.viewCircle) CircleView viewCircle;

  @BindView(R.id.txtDropInTheLive)
  TextViewFont txtDropInTheLive;

  // VARIABLES
  private Unbinder unbinder;
  private Rect rect = new Rect();
  private Paint circlePaint = new Paint();
  private ValueAnimator circleAnimator;
  private TribeGuest guest;
  private Boolean isMeasuring = false;
  private @LiveRoomView.TribeRoomViewType int type = LiveRoomView.GRID;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LiveWaitingView(Context context) {
    super(context);
    init();
  }

  public LiveWaitingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveWaitingView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  private void init() {
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_waiting, this);
    unbinder = ButterKnife.bind(this);

    circlePaint.setStrokeWidth(screenUtils.dpToPx(1f));
    circlePaint.setAntiAlias(true);

    setBackground(null);
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

  ///////////////
  //   DRAW    //
  ///////////////

  @Override protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);

    if (circleAnimator == null || viewCircle.getRect() == null) {
      rect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

      viewCircle.setRect(rect);
      viewCircle.setPaint(circlePaint);
    }
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void startPulse() {
    txtDropInTheLive.setVisibility(View.GONE);
    avatar.setVisibility(View.VISIBLE);

    int finalHeight = screenUtils.getHeightPx() >> 1;

    circleAnimator = ValueAnimator.ofInt(0, finalHeight);
    circleAnimator.setDuration(DURATION_PULSE);
    circleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    circleAnimator.setRepeatMode(ValueAnimator.RESTART);
    circleAnimator.setRepeatCount(ValueAnimator.INFINITE);
    circleAnimator.addUpdateListener(animation -> {
      Integer value = (Integer) animation.getAnimatedValue();
      viewCircle.setRadius(value);
    });

    circleAnimator.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationRepeat(Animator animation) {
        setColor(circlePaint.getColor());
      }
    });

    circleAnimator.start();
  }

  public void stopPulse() {
    if (circleAnimator != null) {
      circleAnimator.cancel();
      circleAnimator.removeAllListeners();
    }
  }

  public void setColor(int color) {
    viewCircle.setBackgroundColor(color);
    circlePaint.setColor(paletteGrid.getRandomColorExcluding(color));
  }

  public void setGuest(TribeGuest guest) {
    this.guest = guest;
    avatar.load(guest.getPicture());
  }

  public void setRoomType(@LiveRoomView.TribeRoomViewType int type) {
    this.type = type;
  }

  public void release() {
    if (viewCircle != null) viewCircle.clearAnimation();

    if (circleAnimator != null) {
      circleAnimator.removeAllUpdateListeners();
      circleAnimator.cancel();
    }
  }
}
