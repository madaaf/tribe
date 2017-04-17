package com.tribe.app.presentation.view.component.live;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class ChasingDotsView extends FrameLayout {

  private final static long DURATION = (long) 800.0f;
  private final static float NB_VIEWS = 5.0f;
  private final static int TRANSLATION_FROM_CENTER = 12;
  private final static float OFFSET_SCALE_DURATION_BETWEEN_DOTS = 0.30f;
  private final static int WAITING_DURATION_RESTARTING_ANIM = 1000;

  @Inject ScreenUtils screenUtils;

  // VARIABLES
  private Unbinder unbinder;
  private List<View> viewDots;
  private float centerX = 0.0f;
  private float centerY = 0.0f;
  private int sizeDot;
  private ArrayList<Float> scaleDots = new ArrayList<>();

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public ChasingDotsView(Context context) {
    super(context);
    init();
  }

  public ChasingDotsView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public ChasingDotsView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    animateSpin();
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();
    super.onDetachedFromWindow();
  }

  private void init() {
    viewDots = new ArrayList<>();
    initDependencyInjector();
    initResources();
    setBackground(null);
    setClipToPadding(false);
    initViews();
  }

  private void initResources() {
    sizeDot = getResources().getDimensionPixelSize(R.dimen.waiting_view_dot_size);
  }

  private void initViews() {
    float MIN_SCALE = 0.4f;

    for (int i = 0; i < NB_VIEWS; i++) {
      View v = new View(getContext());
      float scale = (i / NB_VIEWS) + (MIN_SCALE * (1 - (i / NB_VIEWS)));
      v.setBackgroundResource(R.drawable.shape_oval_white);
      FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sizeDot, sizeDot);
      lp.gravity = Gravity.CENTER;
      v.setLayoutParams(lp);
      viewDots.add(v);
      addView(v);
      scaleDots.add(scale);
    }
  }

  private void animateSpin() {
    for (int i = 0; i < viewDots.size(); i++) {
      final boolean last = (i == (viewDots.size() - 1));
      View viewDot = viewDots.get(i);

      ValueAnimator va = ValueAnimator.ofFloat(-90f, 270.0f);
      va.setDuration(DURATION);
      va.setInterpolator(new AccelerateDecelerateInterpolator());
      float delayBetweenDots = (i / NB_VIEWS) * (DURATION * OFFSET_SCALE_DURATION_BETWEEN_DOTS);
      long startDelay = (long) delayBetweenDots;
      va.setStartDelay(startDelay);
      float scaleAsc = scaleDots.get(i);
      float scaleDesc = scaleDots.get(scaleDots.size() - (i + 1));

      va.addUpdateListener(animation -> {
        Float angleDeg = (float) animation.getAnimatedValue();

        if (angleDeg < (-90.0f + 180.0f)) {
          viewDot.setScaleX(scaleAsc);
          viewDot.setScaleY(scaleAsc);
        } else {
          viewDot.setScaleX(scaleDesc);
          viewDot.setScaleY(scaleDesc);
        }

        float angleRad = (float) Math.toRadians(angleDeg);
        float x =
            (float) (centerX + screenUtils.dpToPx(TRANSLATION_FROM_CENTER) * Math.cos(angleRad));
        float y =
            (float) (centerY + screenUtils.dpToPx(TRANSLATION_FROM_CENTER) * Math.sin(angleRad));

        viewDot.setTranslationX(x);
        viewDot.setTranslationY(y);

        if (angleDeg == 270.0f && last) {
          Handler mHandler = new Handler();
          mHandler.postDelayed(new Runnable() {
            public void run() {
              animateSpin();
            }
          }, WAITING_DURATION_RESTARTING_ANIM);
        }
      });

      va.start();
    }
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
    subscriptions.clear();
  }
}
