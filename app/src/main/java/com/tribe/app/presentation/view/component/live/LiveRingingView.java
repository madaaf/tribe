package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 09/22/17.
 */
public class LiveRingingView extends RelativeLayout {

  private static final int DURATION_INIT = 300;
  private static final int DURATION_DELTA = 75;
  private static final float SCALE_INIT = 0.8f;
  private static final float ALPHA_CAM_WHITE_INIT = 0.5f;

  private static final int[] cameraDrawables = {
      R.drawable.picto_camera_1, R.drawable.picto_camera_2, R.drawable.picto_camera_3,
      R.drawable.picto_camera_4, R.drawable.picto_camera_0
  };

  @Inject ScreenUtils screenUtils;
  @Inject User currentUser;

  @BindView(R.id.layoutCameras) FrameLayout layoutCameras;
  @BindView(R.id.txtRinging) TextViewFont txtRinging;

  // VARIABLES
  private Unbinder unbinder;
  private List<View> views;
  private boolean ringing;
  private Live live;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LiveRingingView(Context context) {
    super(context);
    init();
  }

  public LiveRingingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveRingingView(Context context, AttributeSet attrs, int defStyleAttr) {
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
    setBackground(null);
    setClipToPadding(false);

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_chat_ringing, this);
    unbinder = ButterKnife.bind(this);

    initViews();
  }

  private void initResources() {

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

  @OnClick(R.id.layoutCameras) void clickTest() {
    if (!ringing) {
      activateRinging();
    } else {
      disableRinging();
    }

    ringing = !ringing;
  }

  private void initViews() {
    views = new ArrayList<>();
    ImageView imageView;

    for (int drawableId : cameraDrawables) {
      imageView = new ImageView(getContext());
      FrameLayout.LayoutParams params =
          new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
              FrameLayout.LayoutParams.WRAP_CONTENT);
      params.gravity = Gravity.CENTER;
      imageView.setImageResource(drawableId);

      if (drawableId == R.drawable.picto_camera_0) {
        imageView.setAlpha(ALPHA_CAM_WHITE_INIT);
      } else {
        imageView.setAlpha(0f);
      }

      imageView.setScaleX(SCALE_INIT);
      imageView.setScaleY(SCALE_INIT);

      views.add(imageView);
      layoutCameras.addView(imageView, params);
    }
  }

  private void activateRinging() {
    txtRinging.animate()
        .setDuration(DURATION_INIT)
        .alpha(1)
        .setInterpolator(new DecelerateInterpolator());

    for (int i = views.size() - 1; i >= 0; i--) {
      View view = views.get(i);
      view.animate()
          .setDuration(DURATION_INIT + (views.size() - i) * DURATION_DELTA)
          .scaleX(1f)
          .scaleY(1f)
          .rotation(-360)
          .alpha(1)
          .setInterpolator(new DecelerateInterpolator())
          .start();
    }
  }

  private void disableRinging() {
    txtRinging.animate()
        .setDuration(DURATION_INIT)
        .alpha(0)
        .setInterpolator(new DecelerateInterpolator());

    for (int i = 0; i < views.size(); i++) {
      View view = views.get(i);
      view.animate()
          .setDuration(DURATION_INIT + i * DURATION_DELTA)
          .scaleX(SCALE_INIT)
          .scaleY(SCALE_INIT)
          .rotation(0)
          .alpha(i == views.size() - 1 ? ALPHA_CAM_WHITE_INIT : 0)
          .setInterpolator(new DecelerateInterpolator())
          .start();
    }
  }

  private void setShortcut(Shortcut shortcut) {
    if (shortcut != null) {
      String name = "";

      if (shortcut != null && shortcut.isSingle()) {
        name = shortcut.getSingleFriend().getDisplayName();
      } else if (shortcut != null && !StringUtils.isEmpty(shortcut.getName())) {
        name = shortcut.getName();
      } else {
        name = getResources().getString(R.string.shortcut_members_count,
            shortcut.getMembersIds().size());
      }

      txtRinging.setText(getResources().getString(R.string.live_members_ringing) + " " + name);
    }
  }

  private void setRoom(Room room) {
    String name = "";
    Shortcut shortcut = room.getShortcut();

    int nbUsersWithoutMe = room.nbUsersTotalWithoutMe(currentUser.getId());

    if (shortcut != null && shortcut.isSingle() && nbUsersWithoutMe <= 1) {
      name = shortcut.getSingleFriend().getDisplayName();
    } else if (shortcut != null && !StringUtils.isEmpty(shortcut.getName())) {
      name = shortcut.getName();
    } else if (room != null) {
      name = getResources().getString(R.string.shortcut_members_count, nbUsersWithoutMe);
    }

    txtRinging.setText(getResources().getString(R.string.live_members_ringing) + " " + name);
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void setLive(Live live) {
    this.live = live;

    setShortcut(live.getShortcut());
    subscriptions.add(live.onRoomUpdated().subscribe(room -> setRoom(room)));
  }

  public void applyTranslationX(float x) {
    setTranslationX(x);
    //layoutCameras.setTranslationX(-x / 2);
  }

  public void startRinging() {
    subscriptions.add(Observable.interval(2, TimeUnit.SECONDS)
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .onBackpressureDrop()
        .subscribe(aLong -> {
          if (aLong % 2 == 0) {
            activateRinging();
          } else {
            disableRinging();
          }
        }));
  }

  public void stopRinging() {
    for (int i = 0; i < views.size(); i++) {
      View view = views.get(i);
      view.clearAnimation();
    }

    subscriptions.clear();
  }

  public void hide() {
    if (getVisibility() == View.GONE) return;
    animate().alpha(0)
        .setDuration(DURATION_INIT)
        .setInterpolator(new DecelerateInterpolator())
        .start();
  }

  public void show() {
    if (getVisibility() == View.GONE) return;
    animate().alpha(1)
        .setDuration(DURATION_INIT)
        .setInterpolator(new DecelerateInterpolator())
        .start();
  }

  public void dispose() {
    stopRinging();
    layoutCameras.removeAllViews();
  }
}
