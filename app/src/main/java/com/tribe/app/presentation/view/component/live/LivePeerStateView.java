package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import javax.inject.Inject;

/**
 * Created by tiago on 01/22/17.
 */
public class LivePeerStateView extends FrameLayout {

  private static final int DURATION = 300;

  @Inject ScreenUtils screenUtils;

  @Inject PaletteGrid paletteGrid;

  @BindView(R.id.avatar) AvatarView avatar;

  @BindView(R.id.txtName) TextViewFont txtName;

  @BindView(R.id.txtState) TextViewFont txtState;

  @BindView(R.id.imgIcon) ImageView imgIcon;

  @BindView(R.id.bgDisabledFull) View bgDisabledFull;

  @BindView(R.id.bgDisabledPartial) View bgDisabledPartial;

  @BindView(R.id.layoutNoVideo) ViewGroup layoutNoVideo;

  @BindView(R.id.layoutVideo) ViewGroup layoutVideo;

  // VARIABLES
  private Unbinder unbinder;
  private TribeGuest guest;
  private TribePeerMediaConfiguration mediaConfiguration;

  // RESOURCES
  private int avatarSize;

  // OBSERVABLES

  public LivePeerStateView(Context context) {
    super(context);
    init();
  }

  public LivePeerStateView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LivePeerStateView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    initDependencyInjector();
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_peer_state, this);
    unbinder = ButterKnife.bind(this);

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {

        if (getMeasuredHeight() > 0) {
          getViewTreeObserver().removeOnGlobalLayoutListener(this);
          refactorHeighOfLabels();
        }
      }
    });

    setBackground(null);
    layoutNoVideo.setBackgroundColor(PaletteGrid.getRandomColorExcluding(Color.BLACK));
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (getMeasuredHeight() != 0 && getMeasuredHeight() != getHeight()) {
      refactorHeighOfLabels();
    }
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

  private void refactorHeighOfLabels() {
    int heightName =
        (int) (getMeasuredHeight() - avatar.getMeasuredHeight() * (1 - avatar.getShadowRatio()))
            >> 1;
    int heightState = heightName;
    UIUtils.changeHeightOfView(txtName, heightName);
    UIUtils.changeHeightOfView(txtState, heightState);
  }

  private void loadAvatar() {
    if (guest == null) return;

    avatar.load(guest.getPicture());
  }

  private void setName() {
    if (guest == null) return;

    txtName.setText(guest.getDisplayName());
  }

  private String getStateLabel() {
    if (mediaConfiguration == null
        || mediaConfiguration.getType() == null
        || mediaConfiguration.getType().equals(TribePeerMediaConfiguration.NONE)) {
      return "";
    }

    if (mediaConfiguration.getType().equals(TribePeerMediaConfiguration.USER_UPDATE)) {
      return getContext().getString(R.string.live_placeholder_camera_paused);
    } else if (mediaConfiguration.getType().equals(TribePeerMediaConfiguration.APP_IN_BACKGROUND)) {
      return getContext().getString(R.string.live_placeholder_app_in_background);
    } else if (mediaConfiguration.getType().equals(TribePeerMediaConfiguration.FPS_DROP)) {
      return getContext().getString(R.string.live_placeholder_fps_drops);
    } else if (mediaConfiguration.getType().equals(TribePeerMediaConfiguration.IN_CALL)) {
      return getContext().getString(R.string.live_placeholder_in_call);
    } else {
      return getContext().getString(R.string.live_placeholder_low_bandwidth);
    }
  }

  private int getStateResource() {
    if (mediaConfiguration == null
        || mediaConfiguration.getType() == null
        || mediaConfiguration.getType().equals(TribePeerMediaConfiguration.NONE)) {
      return -1;
    }

    if (mediaConfiguration.getType().equals(TribePeerMediaConfiguration.USER_UPDATE)) {
      if (!mediaConfiguration.isAudioEnabled()) {
        return R.drawable.picto_in_call;
      } else {
        return -1;
      }
    } else if (mediaConfiguration.getType().equals(TribePeerMediaConfiguration.APP_IN_BACKGROUND)
        || mediaConfiguration.getType().equals(TribePeerMediaConfiguration.FPS_DROP)) {
      return -1;
    } else if (mediaConfiguration.getType().equals(TribePeerMediaConfiguration.IN_CALL)) {
      return R.drawable.picto_in_call;
    } else {
      return R.drawable.picto_poor_connection;
    }
  }

  ////////////
  // PUBLIC //
  ////////////

  public void setGuest(TribeGuest tribeGuest) {
    this.guest = tribeGuest;
    loadAvatar();
    setName();
  }

  public void setMediaConfiguration(TribePeerMediaConfiguration mediaConfiguration) {
    this.mediaConfiguration = mediaConfiguration;

    int icon = getStateResource();

    if (mediaConfiguration.isVideoEnabled()) {
      layoutVideo.setVisibility(View.VISIBLE);
      layoutNoVideo.setVisibility(View.GONE);
    } else {
      layoutVideo.setVisibility(View.GONE);
      layoutNoVideo.setVisibility(View.VISIBLE);
      txtState.setText(getStateLabel());
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
        AnimationUtils.fadeIn(bgDisabledPartial, DURATION);
        AnimationUtils.fadeOut(bgDisabledFull, DURATION);
      }
    }
  }
}
