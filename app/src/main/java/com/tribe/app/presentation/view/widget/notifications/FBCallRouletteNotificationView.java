package com.tribe.app.presentation.view.widget.notifications;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.facebook.AccessToken;
import com.tribe.app.R;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.presentation.mvp.view.FBInfoMVPView;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.RemoteConfigManager;
import com.tribe.app.presentation.view.widget.DiceView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import timber.log.Timber;

/**
 * Created by madaaflak on 04/04/2017.
 */

public class FBCallRouletteNotificationView extends LifeNotification implements FBInfoMVPView {

  @BindView(R.id.fbCallRouletteView) LinearLayout fbCallRouletteView;
  @BindView(R.id.diceLayoutRoomView) DiceView diceView;
  @BindView(R.id.txtTitle) TextViewFont txtLabel;
  @BindView(R.id.txtSubTitle) TextViewFont txtSubLabel;
  @BindView(R.id.txtBottom) TextViewFont txtBottom;
  @BindView(R.id.txtAction) TextViewFont txtAction;
  @BindView(R.id.btnAction) FrameLayout btnAction;

  private LayoutInflater inflater;

  // VARIABLES
  private RemoteConfigManager remoteConfigManager;
  private Unbinder unbinder;
  private String unlockRollTheDiceSenderId;

  public FBCallRouletteNotificationView(@NonNull Context context,
      String unlockRollTheDiceSenderId) {
    super(context);
    initView(context, unlockRollTheDiceSenderId);
  }

  public FBCallRouletteNotificationView(@NonNull Context context, @Nullable AttributeSet attrs,
      String unlockRollTheDiceSenderId) {
    super(context, attrs);
    initView(context, unlockRollTheDiceSenderId);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    facebookPresenter.onViewAttached(this);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    facebookPresenter.onViewDetached();
  }

  ///////////////////
  //    ON CLICK   //
  ///////////////////

  @OnClick(R.id.btnAction) void onClickUnlockWithFb() {
    facebookPresenter.loginFacebook();
  }

  ///////////////////
  //    PRIVATE    //
  ///////////////////

  private void initView(Context context, String unlockRollTheDiceSenderId) {
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_facebook_callroulette, this, true);
    unbinder = ButterKnife.bind(this);
    diceView.setVisibility(VISIBLE);
    diceView.startDiceAnimation();
    initRemoteConfig();
    this.unlockRollTheDiceSenderId = unlockRollTheDiceSenderId;
    Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake_with_duration);
    setOnTouchListener((v, event) -> {
      txtAction.startAnimation(shake);
      return false;
    });
  }

  private void initRemoteConfig() {
    remoteConfigManager = RemoteConfigManager.getInstance(getContext());
    setLabelFromFirebase();
  }

  private void setLabelFromFirebase() {
    String txt1 = remoteConfigManager.getString(Constants.wording_unlock_roll_the_dice_title, "");
    if (!txt1.isEmpty()) txtLabel.setText(txt1);
    String txt2 =
        remoteConfigManager.getString(Constants.wording_unlock_roll_the_dice_description, "");
    if (!txt2.isEmpty()) txtSubLabel.setText(txt2);
    String txt3 =
        remoteConfigManager.getString(Constants.wording_unlock_roll_the_dice_facebook_action, "");
    if (!txt3.isEmpty()) txtAction.setText(txt3);
    String txt4 =
        remoteConfigManager.getString(Constants.wording_unlock_roll_the_dice_disclaimer, "");
    if (!txt4.isEmpty()) txtBottom.setText(txt4);
  }

  @Override public void loadFacebookInfos(FacebookEntity facebookEntity) {
    Timber.d("loadFacebookInfos " + facebookEntity.getId());
    facebookPresenter.updateUser(user.getUsername(), user.getDisplayName(), null,
        facebookEntity.getId(), AccessToken.getCurrentAccessToken());
  }

  @Override public void successFacebookLogin() {
    Timber.d("successFacebookLogin");
    facebookPresenter.loadFacebookInfos();
    onFacebookSuccess.onNext(unlockRollTheDiceSenderId);

    Bundle properties = new Bundle();
    properties.putString(TagManagerUtils.FB_ACTION, TagManagerUtils.FB_ACTION_SUCCESS);
    tagManager.trackEvent(TagManagerUtils.FacebookGate, properties);
    hideView();
  }

  @Override public void errorFacebookLogin() {
    Bundle properties = new Bundle();
    properties.putString(TagManagerUtils.FB_ACTION, TagManagerUtils.FB_ACTION_FAILED);
    tagManager.trackEvent(TagManagerUtils.FacebookGate, properties);
    Timber.e("errorFacebookLogin");
  }
}
