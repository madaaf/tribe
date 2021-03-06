package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
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

  @IntDef({ BIG, SMALL }) public @interface BuzzType {
  }

  public static final int BIG = 0;
  public static final int SMALL = 1;

  @Inject ScreenUtils screenUtils;

  @BindViews({
      R.id.viewBolt1, R.id.viewBolt2, R.id.viewBolt3, R.id.viewBolt4, R.id.viewBolt5,
      R.id.viewBolt6, R.id.viewBolt7, R.id.viewBolt8
  }) List<View> viewBolts;

  // RESOURCES

  // VARIABLES
  private int type;
  private Unbinder unbinder;
  private List<Pair<Float, Float>> listTranslations;
  private List<Integer> listSize;
  private boolean hasSentEndBuzz = false;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> buzzCompleted = PublishSubject.create();

  public BuzzView(Context context) {
    super(context);
    init(context, null);
  }

  public BuzzView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public BuzzView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();

    super.onDetachedFromWindow();
  }

  private void init(Context context, AttributeSet attrs) {
    initDependencyInjector();
    initResources();

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BuzzView);
    type = a.getInt(R.styleable.BuzzView_buzzType, BIG);

    setWillNotDraw(false);
    a.recycle();

    LayoutInflater.from(getContext())
        .inflate(type == BIG ? R.layout.view_buzz : R.layout.view_buzz_small, this);
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
    listSize = new ArrayList<>();
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
    hasSentEndBuzz = false;

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
            subscriptions.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                  animatorRotation.cancel();
                  hideBolts(view);
                }));
          }
        })
        .start();
  }

  private void hideBolts(View view) {
    Random r = new Random();
    int randomDelay = r.nextInt(DELAY - 0) + 0;

    if (!hasSentEndBuzz) {
      hasSentEndBuzz = true;
      buzzCompleted.onNext(null);
    }

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
