package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class BuzzView extends FrameLayout {

  private final static int DURATION = 300;
  private final static float OVERSHOOT = 1.25f;

  private final static int DELAY = 200;

  @Inject ScreenUtils screenUtils;

  @BindViews({
      R.id.viewBolt1, R.id.viewBolt2, R.id.viewBolt3, R.id.viewBolt4, R.id.viewBolt5,
      R.id.viewBolt6, R.id.viewBolt7, R.id.viewBolt8
  }) List<View> viewBolts;

  // RESOURCES

  // VARIABLES
  private Unbinder unbinder;
  private List<Pair<Float, Float>> listTranslations;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> buzzCompleted = PublishSubject.create();

  public BuzzView(Context context) {
    super(context);
    init();
  }

  public BuzzView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public BuzzView(Context context, AttributeSet attrs, int defStyleAttr) {
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

    LayoutInflater.from(getContext()).inflate(R.layout.view_buzz, this);
    unbinder = ButterKnife.bind(this);

    setBackground(null);

    for (int i = 0; i < viewBolts.size(); i++) {
      View v = viewBolts.get(i);
      listTranslations.add(new Pair<>(v.getTranslationX(), v.getTranslationY()));
      v.setTranslationX(0);
      v.setTranslationY(0);
    }
  }

  private void initResources() {
    listTranslations = new ArrayList<>();
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

  public void buzz() {
    for (int i = 0; i < viewBolts.size(); i++) {
      View view = viewBolts.get(i);
      view.clearAnimation();
      showBolts(view, i);
    }
  }

  private void showBolts(View view, int i) {
    Random r = new Random();
    int randomDelay = r.nextInt(DELAY - 0) + 0;

    view.animate()
        .translationX(listTranslations.get(i).first)
        .translationY(listTranslations.get(i).second)
        .setDuration(DURATION)
        .setStartDelay(randomDelay)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT))
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            ObjectAnimator animatorRotation = ObjectAnimator.ofFloat(view, ROTATION, 7, -7);
            animatorRotation.setDuration(60);
            animatorRotation.setRepeatCount(ValueAnimator.INFINITE);
            animatorRotation.setRepeatMode(ValueAnimator.REVERSE);
            animatorRotation.start();
            Observable.timer(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> hideBolts(view));
          }
        })
        .start();
  }

  private void hideBolts(View view) {
    Random r = new Random();
    int randomDelay = r.nextInt(DELAY - 0) + 0;

    buzzCompleted.onNext(null);

    view.clearAnimation();
    view.animate()
        .translationX(0)
        .translationY(0)
        .setDuration(DURATION)
        .setStartDelay(randomDelay)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT))
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            view.animate().setListener(null).start();
          }
        })
        .start();
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Void> onBuzzCompleted() {
    return buzzCompleted;
  }
}
