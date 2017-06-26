package com.tribe.app.presentation.view.widget.notifications;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.presentation.mvp.view.FBInfoMVPView;
import com.tribe.app.presentation.view.widget.DiceView;
import timber.log.Timber;

/**
 * Created by madaaflak on 04/04/2017.
 */

public class FBCallRouletteNotificationView extends LifeNotification implements FBInfoMVPView {

  @BindView(R.id.fbCallRouletteView) LinearLayout fbCallRouletteView;
  //  @BindView(R.id.blockView) FrameLayout blockView;
  @BindView(R.id.diceLayoutRoomView) DiceView diceView;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private boolean blockUser = true;

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

/*    setOnTouchListener((v, event) -> {
      blockView.setVisibility(VISIBLE);
      blockView.animate().alpha(1).setDuration(1000).start();
      return true;
    });*/
  }

  @Override public void loadFacebookInfos(FacebookEntity facebookEntity) {
    Timber.e("SOEF FACEBOOK ENTITY " + facebookEntity.getName() + " " + facebookEntity.getId());
  }

  @Override public void successFacebookLogin() {
    Timber.e("SOEF successFacebookLogin ENTITY ");
    blockUser = false;
    onFacebookSuccess.onNext(null);
    super.hideView();
  }

  @Override public void errorFacebookLogin() {
    Timber.e("SOEF errorFacebookLogin ENTITY ");
  }
}
