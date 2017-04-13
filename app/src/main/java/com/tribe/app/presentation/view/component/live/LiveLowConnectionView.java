package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveLowConnectionView extends FrameLayout {

  private static final int DURATION_FADE = 1000;
  private static final int DELAY = 500;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewBG) View viewBG;

  @BindView(R.id.viewLiveWave) View viewLiveWave;

  @BindView(R.id.layoutLabels) ViewGroup layoutLabels;

  // RESOURCES
  private int translationY;

  // VARIABLES
  private Unbinder unbinder;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LiveLowConnectionView(Context context) {
    super(context);
    init(context, null);
  }

  public LiveLowConnectionView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public LiveLowConnectionView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    initDependencyInjector();
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_low_connectivity, this);
    unbinder = ButterKnife.bind(this);
  }

  private void initResources() {
    translationY = screenUtils.dpToPx(10);
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

  //////////////
  //  PUBLIC  //
  //////////////

  public void dispose() {
    if (subscriptions != null) subscriptions.clear();
  }

  public int getTimeToHide() {
    return DURATION_FADE * 2 + DELAY;
  }

  public void show() {
    if (getVisibility() == View.VISIBLE) return;

    viewBG.setAlpha(0f);
    viewLiveWave.setAlpha(0f);
    layoutLabels.setAlpha(0f);
    layoutLabels.setTranslationY(-translationY);
    setVisibility(View.VISIBLE);

    viewBG.animate()
        .alpha(1)
        .setDuration(DURATION_FADE)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    viewLiveWave.animate()
        .alpha(1)
        .setDuration(DURATION_FADE)
        .setStartDelay(DURATION_FADE + DELAY)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    layoutLabels.animate()
        .alpha(1)
        .translationY(0)
        .setDuration(DURATION_FADE)
        .setStartDelay(DURATION_FADE + DELAY)
        .setInterpolator(new OvershootInterpolator(0.75f))
        .start();
  }

  public void hide() {
    if (getVisibility() == View.GONE) return;

    Timber.d("Preparing hide");

    viewBG.setAlpha(1f);
    viewLiveWave.setAlpha(1f);
    layoutLabels.setAlpha(1f);
    layoutLabels.setTranslationY(0);

    viewBG.animate()
        .alpha(0)
        .setDuration(DURATION_FADE)
        .setStartDelay(DURATION_FADE + DELAY)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    viewLiveWave.animate()
        .alpha(0)
        .setDuration(DURATION_FADE)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    layoutLabels.animate()
        .alpha(0)
        .translationY(-translationY)
        .setDuration(DURATION_FADE)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    int timer = getTimeToHide();
    subscriptions.add(Observable.timer(timer, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          Timber.d("Hiding the view");
          setVisibility(View.GONE);
        }));
  }

  /////////////////
  // OBSERVABLES //
  /////////////////
}
