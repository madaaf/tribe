package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveStatusNameView extends FrameLayout {

  private static final int DURATION = 300;
  private static final float OVERSHOOT = 0.90f;

  public static final int INITIATING = R.string.live_waiting_state_initiating;
  public static final int NOTIFYING = R.string.live_waiting_state_notifying;
  public static final int WAITING = R.string.live_waiting_state_waiting;
  public static final int DONE = -1;

  @IntDef({ INITIATING, NOTIFYING, WAITING, DONE }) public @interface StatusType {
  }

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.txtName) TextViewFont txtName;

  @BindView(R.id.txtStatus1) TextViewFont txtStatus1;

  @BindView(R.id.txtStatus2) TextViewFont txtStatus2;

  // VARIABLES
  private Unbinder unbinder;
  private Live live;
  private TextViewFont txtStatusLast;
  private @StatusType int status;

  // RESOURCES
  private int translationY;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LiveStatusNameView(Context context) {
    super(context);
    init();
  }

  public LiveStatusNameView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveStatusNameView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();

    super.onDetachedFromWindow();
  }

  private void init() {
    initDependencyInjector();
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_status_name, this);
    unbinder = ButterKnife.bind(this);

    txtStatus1.setAlpha(0);
    txtStatus1.setTranslationY(translationY);
    txtStatus2.setAlpha(0);
    txtStatus2.setTranslationY(translationY);

    setBackground(null);
  }

  private void initResources() {
    translationY = screenUtils.dpToPx(20);
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

  private void showView(TextViewFont view) {
    txtStatusLast = view;
    view.animate()
        .translationY(0)
        .alpha(1)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT))
        .setDuration(DURATION)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationStart(Animator animation) {
            view.setAlpha(0);
            view.setVisibility(View.VISIBLE);
          }

          @Override public void onAnimationEnd(Animator animation) {
            view.animate().setListener(null).start();
          }
        })
        .start();
  }

  private void hideView(View view) {
    view.animate()
        .translationY(translationY)
        .alpha(0)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT))
        .setDuration(DURATION)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            view.animate().setListener(null).start();
            view.setVisibility(View.GONE);
          }
        })
        .start();
  }

  private void clear() {
    txtStatus1.clearAnimation();
    txtStatus2.clearAnimation();
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void dispose() {
    txtStatus1.clearAnimation();
    txtStatus2.clearAnimation();
  }

  public void setLive(Live live) {
    this.live = live;

    if (live.isGroup()) {
      txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.picto_group_small_shadow, 0, 0, 0);
    } else {
      txtName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    txtName.setText(live.getDisplayName());

    subscriptions.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> setStatus(INITIATING)));
  }

  public void setStatus(@StatusType int status) {
    if (this.status == DONE) return;

    this.status = status;

    if (status == DONE) {
      hideView(txtStatus1);
      hideView(txtStatus2);
      return;
    }

    if (txtStatusLast == null || txtStatusLast == txtStatus2) {
      txtStatus1.setText(getContext().getString(status, live.getDisplayName()));
      hideView(txtStatus2);
      showView(txtStatus1);
    } else {
      txtStatus2.setText(getContext().getString(status, live.getDisplayName()));
      hideView(txtStatus1);
      showView(txtStatus2);
    }
  }

  public @LiveStatusNameView.StatusType int getStatus() {
    return status;
  }
}
