package com.tribe.app.presentation.view.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class LiveNotificationContainer extends FrameLayout {

  private final static int DURATION = 300;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewBG) View viewBG;

  @BindView(R.id.txtSwipe) TextViewFont txtSwipe;

  // RESOURCES
  private int margin;

  // VARIABLES
  private Unbinder unbinder;
  private Map<String, LiveNotificationView> liveNotificationViewMap;
  private LiveNotificationView latestNotificationView;
  private boolean expanded = false;
  private GestureDetector gestureDetector;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private Subscription notificationSubscription;
  private PublishSubject<Pair<LiveNotificationView, ViewGroup.LayoutParams>> onNewNotification =
      PublishSubject.create();

  public LiveNotificationContainer(Context context) {
    super(context);
    init(context, null);
  }

  public LiveNotificationContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  @Override protected void onDetachedFromWindow() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.clear();
    if (notificationSubscription != null) notificationSubscription.unsubscribe();
    super.onDetachedFromWindow();
  }

  private void init(Context context, AttributeSet attrs) {
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_live_notification_container, this, true);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

    unbinder = ButterKnife.bind(this);

    liveNotificationViewMap = new LinkedHashMap<>();
    gestureDetector = new GestureDetector(getContext(), new GestureListener());
    margin = screenUtils.dpToPx(40);
    txtSwipe.setTranslationY(-getTxtTranslationY());

    notificationSubscription =
        onNewNotification
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(pair -> {
              setVisibility(View.VISIBLE);
              if (latestNotificationView != null) latestNotificationView.cancelTimer();
              latestNotificationView = pair.first;
              liveNotificationViewMap.put(pair.first.getViewId(), pair.first);
              super.addView(pair.first, pair.second);
            });
        //Observable.zip(onNewNotification, Observable.interval(1000, TimeUnit.MILLISECONDS).onBackpressureDrop(),
        //    (pair, aLong) -> pair)
        //    .subscribeOn(Schedulers.newThread())
        //    .observeOn(AndroidSchedulers.mainThread())
        //    .subscribe(pair -> {
        //      Timber.d("Emitting notifications");
        //      setVisibility(View.VISIBLE);
        //      if (latestNotificationView != null) latestNotificationView.cancelTimer();
        //      latestNotificationView = pair.first;
        //      liveNotificationViewMap.put(pair.first.getViewId(), pair.first);
        //      super.addView(pair.first, pair.second);
        //    });
  }

  private void cleanUp() {
    setVisibility(View.GONE);

    for (LiveNotificationView view : liveNotificationViewMap.values()) {
      removeView(view);
      view = null;
    }

    liveNotificationViewMap.clear();
    latestNotificationView = null;
    subscriptions.clear();
  }

  private int getTxtTranslationY() {
    return screenUtils.getHeightPx() / 3;
  }

  ///////////////////
  //    PUBLIC     //
  ///////////////////

  public void addView(LiveNotificationView child, ViewGroup.LayoutParams params) {
    onNewNotification.onNext(new Pair<>(child, params));
  }

  public void addSubscriptionNotification(Observable<Float> obs, View viewNotification) {
    subscriptions.add(obs.subscribe(percent -> {
      viewBG.setAlpha(percent);
      txtSwipe.setTranslationY(-(1 - percent) * getTxtTranslationY());
      viewNotification.setTranslationY(percent * margin);
    }));
  }

  public void addSubscriptionExpanded(Observable<Void> obs) {
    subscriptions.add(obs.subscribe(aVoid -> {
      expanded = true;
    }));
  }

  public void addSubscriptionDismissed(Observable<Void> obs) {
    subscriptions.add(obs.subscribe(aVoid -> {
      cleanUp();
    }));
  }

  public void addSubscriptionAnimationDone(Observable<LiveNotificationView> obs) {
    subscriptions.add(obs.subscribe(view -> {
      for (String key : liveNotificationViewMap.keySet()) {
        if (!key.equals(view.getViewId())) {
          removeView(liveNotificationViewMap.get(key));
        }
      }
    }));
  }

  public void dismissNotification(LiveNotificationView liveNotificationView) {
    expanded = false;

    viewBG.animate()
        .alpha(0)
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            animate().setListener(null).start();
          }
        })
        .start();

    txtSwipe.animate()
        .translationY(-getTxtTranslationY())
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    liveNotificationView.dismiss();
  }

  public boolean isExpanded() {
    return expanded;
  }

  ///////////////
  //   TOUCH   //
  ///////////////

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    if (expanded) {
      boolean intercept = true;

      if (ViewUtils.isIn(latestNotificationView, (int) ev.getX(), (int) ev.getY())) {
        intercept = false;
      }

      return intercept;
    } else {
      return super.onInterceptTouchEvent(ev);
    }
  }

  @Override public boolean onTouchEvent(MotionEvent ev) {
    if (expanded) {
      return gestureDetector.onTouchEvent(ev);
    } else {
      return super.onTouchEvent(ev);
    }
  }

  private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override public boolean onDown(MotionEvent e) {
      return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      boolean result = false;

      try {
        float diffY = e2.getY() - e1.getY();

        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
          if (diffY < 0) {
            if (latestNotificationView != null) dismissNotification(latestNotificationView);
            result = true;
          }
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }

      return result;
    }
  }
}
