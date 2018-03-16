package com.tribe.app.presentation.view.widget.notifications;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.TextViewFont;
import timber.log.Timber;

/**
 * Created by madaaflak on 11/04/2017.
 */

public class PermissionNotificationView extends LifeNotification {

  @BindView(R.id.btnAction1) TextViewFont btnAction1;
  @BindView(R.id.btnAction2) TextViewFont btnAction2;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private boolean cameraEnabledState = false;
  private boolean microEnabledState = false;
  private RxPermissions rxPermissions;

  public PermissionNotificationView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public PermissionNotificationView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  private void initView(Context context) {
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_permissions_notification, this, true);
    unbinder = ButterKnife.bind(this);

    initParams();
    initBtn1(cameraEnabledState);
    initBtn2(microEnabledState);
  }

  private void initParams() {
    rxPermissions = new RxPermissions((Activity) getContext());
    cameraEnabledState = PermissionUtils.hasPermissionsCameraOnly(rxPermissions);
    microEnabledState = PermissionUtils.hasPermissionsMicroOnly(rxPermissions);
  }

  boolean onClickCamera = false;
  boolean onClickMicro = false;

  @OnClick(R.id.btnAction1) void onClickCameraEnable() {
    onClickCamera = true;
    if (!cameraEnabledState) {
      cameraEnabledState = true;
    }
    subscriptions.add(
        rxPermissions.requestEach(PermissionUtils.PERM_CAMERA).subscribe(permission -> {
          if (permission.granted) {
            initBtn1(PermissionUtils.hasPermissionsCameraOnly(rxPermissions));
          } else if (permission.shouldShowRequestPermissionRationale) {
            Timber.d("Denied camera permission without ask never again");
          } else {
            Timber.d("Denied camera permission and ask never again");
            if (!stateManager.shouldDisplay(StateManager.NEVER_ASK_AGAIN_CAMERA_PERMISSION)) {
              navigator.navigateToSettingApp(getContext());
              hideView();
            }
            stateManager.addTutorialKey(StateManager.NEVER_ASK_AGAIN_CAMERA_PERMISSION);
          }
          Bundle bundle = new Bundle();
          bundle.putBoolean(TagManagerUtils.USER_CAMERA_ENABLED,
              PermissionUtils.hasPermissionsCameraOnly(rxPermissions));
          tagManager.setProperty(bundle);
          finish();
        }));
  }

  @OnClick(R.id.btnAction2) void onClickMicroEnable() {
    onClickMicro = true;
    if (!microEnabledState) {
      microEnabledState = true;
    }

    subscriptions.add(
        rxPermissions.requestEach(PermissionUtils.RECORD_AUDIO).subscribe(permission -> {
          if (permission.granted) {
            initBtn2(PermissionUtils.hasPermissionsMicroOnly(rxPermissions));
          } else if (permission.shouldShowRequestPermissionRationale) {
            Timber.d("Denied micro permission without ask never again");
          } else {
            Timber.d("Denied micro permission and ask never again");
            if (!stateManager.shouldDisplay(StateManager.NEVER_ASK_AGAIN_MICRO_PERMISSION)) {
              navigator.navigateToSettingApp(getContext());
              hideView();
            }
            stateManager.addTutorialKey(StateManager.NEVER_ASK_AGAIN_MICRO_PERMISSION);
          }
          Bundle bundle = new Bundle();
          bundle.putBoolean(TagManagerUtils.USER_MICROPHONE_ENABLED,
              PermissionUtils.hasPermissionsMicroOnly(rxPermissions));
          tagManager.setProperty(bundle);
          finish();
        }));
  }

  private void finish() {
    Handler mHandler = new Handler();
    if (onClickMicro && onClickMicro) {
      if (PermissionUtils.hasPermissionsCameraOnly(rxPermissions)
          && PermissionUtils.hasPermissionsMicroOnly(rxPermissions)) {
        hideView();
        mHandler.postDelayed(() -> onAcceptedPermission.onNext(true), 300);
      }else {
        mHandler.postDelayed(() -> onAcceptedPermission.onNext(false), 300);
      }
    }
   /* */
  }

  private void initBtn1(Boolean cameraEnabled) {
    if (cameraEnabled) {
      btnAction1.setText(R.string.camera_microphone_popup_camera_enabled);
      btnAction1.setBackgroundResource(R.drawable.shape_btn_rect_blue13_corner);
      btnAction1.setTextColor(Color.WHITE);
    } else {
      btnAction1.setText(R.string.camera_microphone_popup_enable_camera);
      btnAction1.setBackgroundResource(R.drawable.shape_btn_rect_with_greyborder_corner);
      btnAction1.setTextColor(Color.BLACK);
    }
  }

  private void initBtn2(Boolean microEnabled) {
    if (microEnabled) {
      btnAction2.setText(R.string.camera_microphone_popup_microphone_enabled);
      btnAction2.setBackgroundResource(R.drawable.shape_btn_rect_blue13_corner);
      btnAction2.setTextColor(Color.WHITE);
    } else {
      btnAction2.setText(R.string.camera_microphone_popup_enable_microphone);
      btnAction2.setBackgroundResource(R.drawable.shape_btn_rect_with_greyborder_corner);
      btnAction2.setTextColor(Color.BLACK);
    }
  }
}
