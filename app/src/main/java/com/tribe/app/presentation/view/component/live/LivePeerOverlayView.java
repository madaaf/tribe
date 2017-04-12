package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.component.VisualizerView;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.MediaConfigurationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.model.TribeSession;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class LivePeerOverlayView extends FrameLayout {

  private static final int DURATION = 300;
  private static final float AVATAR_SCALE = 1.1f;
  private static final float OVERSHOOT_AVATAR = 2f;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.imgIcon) ImageView imgIcon;

  @BindView(R.id.bgDisabledFull) View bgDisabledFull;

  @BindView(R.id.viewLowConnection) View viewLowConnection;

  @BindView(R.id.avatar) AvatarView avatar;

  @BindView(R.id.bgDisabledPartial) View bgDisabledPartial;

  @BindView(R.id.txtName) TextViewFont txtName;

  @BindView(R.id.txtState) TextViewFont txtState;

  @BindView(R.id.viewVisualizer) VisualizerView visualizerView;

  // VARIABLES
  private Unbinder unbinder;
  private TribeGuest guest;
  private TribePeerMediaConfiguration mediaConfiguration;

  // RESOURCES
  private int avatarSize;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LivePeerOverlayView(Context context) {
    super(context);
    init();
  }

  public LivePeerOverlayView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LivePeerOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (subscriptions != null) subscriptions.clear();
  }

  private void init() {
    initDependencyInjector();
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_peer_overlay, this);
    unbinder = ButterKnife.bind(this);

    UIUtils.changeSizeOfView(bgDisabledPartial,
        avatarSize - (int) Math.floor(avatarSize * avatar.getShadowRatio()));

    setBackground(null);
  }

  private void initResources() {
    avatarSize = getContext().getResources().getDimensionPixelSize(R.dimen.avatar_size_big_shadow);
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

  private void loadAvatar() {
    if (guest == null) return;

    avatar.load(guest.getPicture());
  }

  private void setName() {
    if (guest == null) return;

    txtName.setText(guest.getDisplayName());
  }

  ////////////
  // PUBLIC //
  ////////////

  public void dispose() {
    visualizerView.release();
    if (unbinder != null) unbinder.unbind();
  }

  public void setGuest(TribeGuest tribeGuest) {
    mediaConfiguration =
        new TribePeerMediaConfiguration(new TribeSession(tribeGuest.getId(), tribeGuest.getId()));
    guest = tribeGuest;
    loadAvatar();
    setName();
  }

  public void initMediaConfiguration(TribePeerMediaConfiguration mediaConfiguration) {
    this.mediaConfiguration = new TribePeerMediaConfiguration(mediaConfiguration);
  }

  public void setMediaConfiguration(TribePeerMediaConfiguration mediaConfiguration) {
    boolean shouldAnimateAvatar =
        this.mediaConfiguration.isVideoEnabled() && !mediaConfiguration.isVideoEnabled();
    this.mediaConfiguration.update(mediaConfiguration);

    int icon = MediaConfigurationUtils.getStateResource(mediaConfiguration);

    final boolean isViewVisible = !mediaConfiguration.isVideoEnabled()
        || mediaConfiguration.isLowConnection()
        || !mediaConfiguration.isAudioEnabled();

    if (shouldAnimateAvatar) {
      txtName.setAlpha(0f);
      txtName.animate()
          .alpha(1)
          .setDuration(DURATION)
          .setStartDelay(UIUtils.DURATION_REVEAL)
          .setInterpolator(new DecelerateInterpolator())
          .start();

      txtState.setAlpha(0f);
      txtState.animate()
          .alpha(1)
          .setDuration(DURATION)
          .setStartDelay(UIUtils.DURATION_REVEAL)
          .setInterpolator(new DecelerateInterpolator())
          .start();

      avatar.setAlpha(0f);
      avatar.setScaleX(AVATAR_SCALE);
      avatar.setScaleY(AVATAR_SCALE);
      avatar.animate()
          .scaleX(1)
          .scaleY(1)
          .alpha(1)
          .setDuration(DURATION)
          .setStartDelay(UIUtils.DURATION_REVEAL)
          .setInterpolator(new OvershootInterpolator(OVERSHOOT_AVATAR))
          .start();
    }

    if (mediaConfiguration.isVideoEnabled()) {
      if (!shouldAnimateAvatar) {
        avatar.setAlpha(0f);
        txtName.setAlpha(0f);
        txtState.setAlpha(0f);
      }

      visualizerView.hide(false);

      if (mediaConfiguration.isLowConnection()) {
        AnimationUtils.fadeIn(viewLowConnection, DURATION);
      } else {
        AnimationUtils.fadeOut(viewLowConnection, DURATION);
      }
    } else {
      txtState.setText(MediaConfigurationUtils.getStateLabel(getContext(), mediaConfiguration));
      AnimationUtils.fadeOut(viewLowConnection, DURATION);

      if (mediaConfiguration.isAudioEnabled()) {
        visualizerView.show();
      } else {
        visualizerView.hide(true);
      }
    }

    if (icon == -1) {
      imgIcon.setVisibility(View.GONE);
      AnimationUtils.fadeOut(bgDisabledFull, DURATION);
      AnimationUtils.fadeOut(bgDisabledPartial, DURATION);
    } else {
      imgIcon.setVisibility(View.VISIBLE);
      imgIcon.setImageResource(icon);

      if (mediaConfiguration.isVideoEnabled()) {
        AnimationUtils.fadeIn(bgDisabledFull, DURATION);
        AnimationUtils.fadeOut(bgDisabledPartial, DURATION);
      } else {
        AnimationUtils.fadeOut(bgDisabledFull, DURATION);
        AnimationUtils.fadeIn(bgDisabledPartial, DURATION);
      }
    }

    if (!isViewVisible) {
      subscriptions.add(Observable.timer(DURATION, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aLong -> setVisibility(View.GONE)));
    } else {
      setVisibility(View.VISIBLE);
    }
  }
}
