package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.widget.avatar.AvatarLiveView;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class LiveNotificationView extends FrameLayout implements Animation.AnimationListener {

  @IntDef({ LIVE, ERROR }) public @interface LiveNotificationType {
  }

  private static final long DISPLAY_TIME_IN_SECONDS = 5000;
  public static final int LIVE = 0;
  public static final int ERROR = 1;
  private static final int CLEAN_UP_DELAY_MILLIS = 100;
  private static final int SCREEN_SCALE_FACTOR = 6;

  // VARIABLES
  private @LiveNotificationView.LiveNotificationType int type;
  private Animation slideInAnimation;
  private Animation slideOutAnimation;
  private boolean marginSet;
  private int sound = -1;
  private long duration = DISPLAY_TIME_IN_SECONDS;

  //UI
  @BindView(R.id.view_live_notification_container) LinearLayout notificationContainer;
  @BindView(R.id.view_live_notification_action_container) LinearLayout actionContainer;
  @BindView(R.id.view_notification_container) LinearLayout screen;
  @BindView(R.id.tvTitle) TextViewFont txtTitle;
  @BindView(R.id.avatar) AvatarLiveView avatarLiveView;
  @Nullable @BindView(R.id.imgIcon) ImageView imgIcon;

  @Inject SoundManager soundManager;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<LiveNotificationActionView.Action> onClickAction = PublishSubject.create();

  public LiveNotificationView(@NonNull final Context context,
      @LiveNotificationView.LiveNotificationType int type) {
    super(context, null, R.attr.alertStyle);
    this.type = type;
    initView();
  }

  public LiveNotificationView(@NonNull final Context context, @Nullable final AttributeSet attrs,
      @LiveNotificationView.LiveNotificationType int type) {
    super(context, attrs, R.attr.alertStyle);
    this.type = type;
    initView();
  }

  private void initView() {
    LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_live_notification, this, true);

    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    unbinder = ButterKnife.bind(this);
    setHapticFeedbackEnabled(true);

    screen.setOnClickListener(v -> hide());

    setAnimation();

    notificationContainer.setPadding(notificationContainer.getPaddingLeft(),
        notificationContainer.getPaddingTop() + (getScreenHeight() / SCREEN_SCALE_FACTOR),
        notificationContainer.getPaddingRight(), 0);
  }

  //////////////////////
  //      INIT        //
  //////////////////////

  @Override protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (!marginSet) {
      marginSet = true;
      // Add a negative top margin to compensate for overshoot enter animation
      final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
      params.topMargin = params.topMargin - (getScreenHeight() / SCREEN_SCALE_FACTOR);
      requestLayout();
    }
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
      subscriptions.clear();
    }
    slideInAnimation.setAnimationListener(null);
    unbinder.unbind();
  }

  /* Interface Method Implementations */

  @Override public void onAnimationStart(final Animation animation) {
    if (!isInEditMode()) {
      performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
      if (sound != -1) {
        soundManager.playSound(sound, SoundManager.SOUND_MAX);
      }
    }
  }

  @Override public void onAnimationEnd(final Animation animation) {
    postDelayed(() -> hide(), duration);
  }

  @Override public void onAnimationRepeat(final Animation animation) {
  }

  public void setAlertBackgroundColor(@ColorInt final int color) {
    notificationContainer.setBackgroundColor(color);
  }

  /////////////////
  //  PRIVATE   ///
  /////////////////

  private void hide() {
    try {
      slideOutAnimation.setAnimationListener(new AnimationListenerAdapter() {
        @Override public void onAnimationStart(final Animation animation) {
          notificationContainer.setOnClickListener(null);
          notificationContainer.setClickable(false);
        }

        @Override public void onAnimationEnd(final Animation animation) {
          removeFromParent();
        }
      });
      startAnimation(slideOutAnimation);
    } catch (Exception ex) {
      Log.e(getClass().getSimpleName(), Log.getStackTraceString(ex));
    }
  }

  private void removeFromParent() {
    postDelayed(() -> {
      try {
        if (getParent() == null) {
          Timber.e("getParent() returning Null");
        } else {
          try {
            ((ViewGroup) getParent()).removeView(LiveNotificationView.this);
          } catch (Exception ex) {
            Timber.e("Cannot remove from parent layout");
          }
        }
      } catch (Exception ex) {
        Timber.e(Log.getStackTraceString(ex));
      }
    }, CLEAN_UP_DELAY_MILLIS);
  }

  private void addAction(LiveNotificationActionView.Action action, boolean isLast) {
    int sizeActionItem =
        getResources().getDimensionPixelSize(R.dimen.live_notification_item_height);

    LiveNotificationActionView actionView =
        new LiveNotificationActionView.Builder(getContext(), action).isLast(isLast).build();

    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, sizeActionItem);
    params.gravity = Gravity.CENTER_VERTICAL;

    actionContainer.addView(actionView, params);
    subscriptions.add(actionView.onClick().subscribe(onClickAction));
  }

  private void setTitle(@NonNull final String title) {
    if (!TextUtils.isEmpty(title)) {
      txtTitle.setVisibility(VISIBLE);
      txtTitle.setText(title);
    }
  }

  private void setSound(int sound) {
    this.sound = sound;
  }

  private void setImgUrl(String url) {
    if (type == ERROR) {
      imgIcon.setImageResource(R.drawable.picto_lock);
    } else {
      avatarLiveView.load(url);
    }
  }

  private int getScreenHeight() {
    final WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    final DisplayMetrics metrics = new DisplayMetrics();
    wm.getDefaultDisplay().getMetrics(metrics);
    return metrics.heightPixels;
  }

  private void setAnimation() {
    slideInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.alerter_slide_in_from_top);
    slideOutAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.alerter_slide_out_to_top);
    slideInAnimation.setAnimationListener(this);
    setAnimation(slideInAnimation);
  }

  public static class Builder {

    private final Context context;
    private String imgUrl;
    private String title;
    private List<LiveNotificationActionView.Action> actionList;
    private @LiveNotificationView.LiveNotificationType int type;
    private int sound;

    public Builder(Context context, @LiveNotificationView.LiveNotificationType int type) {
      this.context = context;
      this.type = type;
      this.actionList = new ArrayList<>();
    }

    public Builder imgUrl(String imgUrl) {
      this.imgUrl = imgUrl;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder addAction(String id, String title, Intent intent) {
      this.actionList.add(new LiveNotificationActionView.Action(id, title, intent));
      return this;
    }

    public Builder sound(int sound) {
      this.sound = sound;
      return this;
    }

    public LiveNotificationView build() {
      LiveNotificationView view = new LiveNotificationView(context, type);
      view.setImgUrl(imgUrl);
      view.setTitle(title);
      view.setSound(sound);

      if (type != ERROR) {
        int count = 0;
        for (LiveNotificationActionView.Action action : actionList) {
          view.addAction(action, (count == (actionList.size() - 1)));
          count++;
        }
      } else {
        Timber.w("type error");
      }
      return view;
    }
  }

  ///////////////////////
  //    OBSERVABLES    //
  ///////////////////////

  public Observable<LiveNotificationActionView.Action> onClickAction() {
    return onClickAction;
  }
}
