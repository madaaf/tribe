package com.tribe.app.presentation.view.widget.notifications;

import android.content.Context;
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
import com.f2prateek.rx.preferences.BuildConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.tribe.app.R;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.presentation.mvp.view.FBInfoMVPView;
import com.tribe.app.presentation.view.utils.Constants;
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
  private FirebaseRemoteConfig firebaseRemoteConfig;
  private Unbinder unbinder;

  public FBCallRouletteNotificationView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public FBCallRouletteNotificationView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
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

  private void initView(Context context) {
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_facebook_callroulette, this, true);
    unbinder = ButterKnife.bind(this);
    diceView.setVisibility(VISIBLE);
    diceView.startDiceAnimation();
    initRemoteConfig();

    Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake_with_duration);
    setOnTouchListener((v, event) -> {
      txtAction.startAnimation(shake);
      return false;
    });
  }

  private void initRemoteConfig() {
    firebaseRemoteConfig = firebaseRemoteConfig.getInstance();
    FirebaseRemoteConfigSettings configSettings =
        new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build();
    firebaseRemoteConfig.setConfigSettings(configSettings);
    firebaseRemoteConfig.fetch().addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        firebaseRemoteConfig.activateFetched();
        setLabelFromFirebase();
      }
    });
  }

  private void setLabelFromFirebase() {
    String txt1 = firebaseRemoteConfig.getString(Constants.wording_unlock_roll_the_dice_title);
    txtLabel.setText(txt1);
    String txt2 =
        firebaseRemoteConfig.getString(Constants.wording_unlock_roll_the_dice_description);
    txtSubLabel.setText(txt2);
    String txt3 =
        firebaseRemoteConfig.getString(Constants.wording_unlock_roll_the_dice_facebook_action);
    txtAction.setText(txt3);
    String txt4 = firebaseRemoteConfig.getString(Constants.wording_unlock_roll_the_dice_disclaimer);
    txtBottom.setText(txt4);
  }

  @Override public void loadFacebookInfos(FacebookEntity facebookEntity) {
  }

  @Override public void successFacebookLogin() {
    Timber.d("successFacebookLogin");
    onFacebookSuccess.onNext(null);
    super.hideView();
  }

  @Override public void errorFacebookLogin() {
    Timber.e("errorFacebookLogin");
  }
}
