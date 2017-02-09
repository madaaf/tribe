package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.view.LocalPeerView;
import com.tribe.tribelivesdk.view.PeerView;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveLocalView extends FrameLayout {

  private static final int DURATION = 300;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewPeerLocal)
  LocalPeerView viewPeerLocal;

  @BindView(R.id.btnCameraEnable)
  ImageView btnCameraEnable;

  @BindView(R.id.btnCameraDisable)
  ImageView btnCameraDisable;

  @BindView(R.id.btnCameraSwitch)
  ImageView btnCameraSwitch;

  @BindView(R.id.layoutCameraControls)
  ViewGroup layoutCameraControls;

  // VARIABLES
  private Unbinder unbinder;
  private boolean hiddenControls = false;
  private boolean cameraEnabled = true;

  // OBSERVABLES
  private PublishSubject<Boolean> onEnableCamera = PublishSubject.create();
  private PublishSubject<Void> onSwitchCamera = PublishSubject.create();

  public LiveLocalView(Context context) {
    super(context);
    init();
  }

  public LiveLocalView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveLocalView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public void init() {
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_local, this);
    unbinder = ButterKnife.bind(this);

    viewPeerLocal.initEnableCameraSubscription(onEnableCamera);
    viewPeerLocal.initSwitchCameraSubscription(onSwitchCamera);
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
        .build().inject(this);
  }

  /////////////////
  //   CLICKS    //
  /////////////////

  @OnClick(R.id.btnCameraEnable)
  void clickEnableCamera() {
    if (!hiddenControls) {
      cameraEnabled = true;
      onEnableCamera.onNext(cameraEnabled);
      alpha(btnCameraSwitch, 1);
      animateEnableCamera(cameraEnabled);
    }
  }

  @OnClick(R.id.btnCameraDisable)
  void clickDisableCamera() {
    if (!hiddenControls) {
      cameraEnabled = false;
      onEnableCamera.onNext(cameraEnabled);
      alpha(btnCameraSwitch, 0);
      animateEnableCamera(cameraEnabled);
    }
  }

  @OnClick(R.id.btnCameraSwitch)
  void clickCameraSwitch() {
    if (!hiddenControls) {
      rotateSwitchCamera();
      onSwitchCamera.onNext(null);
    }
  }

  ////////////////
  // ANIMATIONS //
  ////////////////

  private void animateEnableCamera(boolean enabled) {
    if (enabled) {
      alpha(btnCameraEnable, 0);
      alpha(btnCameraDisable, 1);
    } else {
      alpha(btnCameraDisable, 0);
      alpha(btnCameraEnable, 1);
    }
  }

  private void alpha(View v, int alpha) {
    v.animate().alpha(alpha).setDuration(DURATION).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationStart(Animator animation) {
        v.setVisibility(alpha == 1 ? View.VISIBLE : View.GONE);
      }

      @Override public void onAnimationEnd(Animator animation) {
        v.setVisibility(alpha == 0 ? View.GONE : View.VISIBLE);
        v.animate().setListener(null).start();
      }
    }).start();
  }

  private void scale(View v, int scale) {
    v.animate().scaleX(scale).scaleY(scale).setDuration(DURATION).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationStart(Animator animation) {
        v.setVisibility(scale == 1 ? View.VISIBLE : View.GONE);
      }

      @Override public void onAnimationEnd(Animator animation) {
        v.setVisibility(scale == 0 ? View.GONE : View.VISIBLE);
        v.animate().setListener(null).start();
      }
    }).start();
  }

  private void rotateSwitchCamera() {
    btnCameraSwitch.animate().rotation(btnCameraSwitch.getRotation() == 0 ? 180 : 0).setDuration(DURATION).start();
  }

  /////////////////
  //   PUBLIC    //
  /////////////////

  public void dispose() {
    viewPeerLocal.dispose();
  }

  public LocalPeerView getLocalPeerView() {
    return viewPeerLocal;
  }

  public void hideControls(boolean hiddenControls) {
    int scale = hiddenControls ? 0 : 1;
    this.hiddenControls = hiddenControls;

    scale(layoutCameraControls, scale);
    scale(btnCameraSwitch, scale);
  }

  //////////////////
  //  OBSERVABLES //
  //////////////////

  public Observable<Boolean> onEnableCamera() {
    return onEnableCamera;
  }

  public Observable<Void> onSwitchCamera() {
    return onSwitchCamera;
  }
}
