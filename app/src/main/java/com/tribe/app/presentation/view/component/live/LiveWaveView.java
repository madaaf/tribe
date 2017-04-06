package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
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
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 04/05/17.
 */
public class LiveWaveView extends FrameLayout {

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewWave) View viewWave;

  // RESOURCES

  // VARIABLES
  private Unbinder unbinder;
  private Animation animationWave;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LiveWaveView(Context context) {
    super(context);
    init(context, null);
  }

  public LiveWaveView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public LiveWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();

    if (animationWave != null) animationWave.cancel();

    super.onDetachedFromWindow();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (getMeasuredWidth() != 0 && animationWave == null) {
      Timber.d("Measure");
      initWave();
    }
  }

  private void init(Context context, AttributeSet attrs) {
    initDependencyInjector();
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_wave, this);
    unbinder = ButterKnife.bind(this);

    setBackground(null);
  }

  private void initResources() {

  }

  private void initWave() {
    if (animationWave != null) animationWave.cancel();

    final int waveImgWidth =
        ContextCompat.getDrawable(getContext(), R.drawable.picto_wave_low_connection)
            .getIntrinsicWidth();

    int measuredWidth = getMeasuredWidth();
    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) viewWave.getLayoutParams();
    layoutParams.width = measuredWidth + waveImgWidth * 2;
    viewWave.setLayoutParams(layoutParams);

    animationWave = new TranslateAnimation(0, -waveImgWidth, 0, 0);
    animationWave.setInterpolator(new LinearInterpolator());
    animationWave.setRepeatCount(Animation.INFINITE);
    animationWave.setDuration(measuredWidth * 3);

    viewWave.startAnimation(animationWave);
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

  /////////////////
  // OBSERVABLES //
  /////////////////
}
