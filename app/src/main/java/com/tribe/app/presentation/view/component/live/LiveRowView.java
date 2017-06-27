package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.view.PeerView;
import com.tribe.tribelivesdk.view.RemotePeerView;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveRowView extends FrameLayout {

  private static final int DURATION = 500;
  private static double TENSION = 400;
  private static double DAMPER = 10;

  @Inject ScreenUtils screenUtils;

  @Inject User user;

  @BindView(R.id.viewWaiting) LiveWaitingView viewWaiting;

  @BindView(R.id.viewPeerOverlay) LivePeerOverlayView viewPeerOverlay;

  @BindView(R.id.layoutStream) ViewGroup layoutStream;

  @BindView(R.id.viewBackground) View backgroundView;

  @BindView(R.id.addFriend) ImageView btnAddFriend;

  // VARIABLES
  private Unbinder unbinder;
  private RemotePeerView remotePeerView;
  private TribeGuest guest;
  private int color;
  private boolean isWaiting = false;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<TribeGuest> onClick = PublishSubject.create();
  private PublishSubject<Void> onRollTheDice = PublishSubject.create();

  public LiveRowView(Context context) {
    super(context);
    init();
  }

  public LiveRowView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveRowView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public void init() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    LayoutInflater.from(getContext()).inflate(R.layout.view_row_live, this);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    if (guest != null) {
      viewWaiting.setGuest(guest);
      viewPeerOverlay.setGuest(guest);
    }

    viewWaiting.setColor(color);

    if (remotePeerView != null) viewWaiting.setVisibility(View.GONE);
  }

  public void guestAppear() {
    setAddBtn(guest);
  }

  public void setAddBtn(TribeGuest guest) {
    for (Friendship friendship : user.getFriendships()) {
      User friend = friendship.getFriend();
      if (guest.getId().endsWith(friend.getId())) {
        if (friendship.getStatus().equals(FriendshipRealm.HIDDEN) || friendship.getStatus()
            .equals(FriendshipRealm.BLOCKED)) {
          guest.setFriend(false);
        } else {
          guest.setFriend(true);
        }
        break;
      } else {
        guest.setFriend(false);
      }
    }

    if (guest.isFriend() || guest.isExternal()) {
      btnAddFriend.setVisibility(GONE);
    } else {
      animateAddBtn();
    }
  }

  public void dispose() {
    viewWaiting.dispose();
    subscriptions.clear();
  }

  public void setColor(int color) {
    if (color == Color.BLACK || color == 0) color = PaletteGrid.getRandomColorExcluding(color);
    this.color = color;
    viewWaiting.setColor(color);
  }

  public int getColor() {
    return color;
  }

  public void setGuest(TribeGuest guest) {
    this.guest = guest;
    viewWaiting.setGuest(guest);
    viewPeerOverlay.setGuest(guest);
  }

  public void setRoomType(@LiveRoomView.TribeRoomViewType int type) {
    viewWaiting.setRoomType(type);
  }

  public void setPeerView(PeerView peerView) {
    remotePeerView = (RemotePeerView) peerView;

    subscriptions.add(this.remotePeerView.onNotificationRemoteJoined()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(s -> UIUtils.showReveal(layoutStream, true, new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            viewWaiting.stopPulse();
            viewWaiting.setVisibility(View.GONE);
          }

          @Override public void onAnimationStart(Animator animation) {
            layoutStream.setVisibility(View.VISIBLE);
          }
        })));

    subscriptions.add(this.remotePeerView.onMediaConfiguration()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tribePeerMediaConfiguration -> {
          if (!tribePeerMediaConfiguration.isVideoEnabled()) {
            UIUtils.hideReveal(layoutStream, true, new AnimatorListenerAdapter() {
              @Override public void onAnimationEnd(Animator animation) {
                if (animation != null) animation.removeAllListeners();
                layoutStream.setVisibility(View.GONE);
              }
            });
          } else {
            UIUtils.showReveal(layoutStream, true, new AnimatorListenerAdapter() {
              @Override public void onAnimationStart(Animator animation) {
                layoutStream.setVisibility(View.VISIBLE);
              }

              @Override public void onAnimationEnd(Animator animation) {
                if (animation != null) animation.removeAllListeners();
              }
            });
          }

          viewPeerOverlay.setMediaConfiguration(tribePeerMediaConfiguration);
        }));

    isWaiting = false;
    viewWaiting.incomingPeer();

    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    if (remotePeerView.getParent() == null) layoutStream.addView(remotePeerView, params);
  }

  public TribeGuest getGuest() {
    return guest;
  }

  public boolean isWaiting() {
    return isWaiting;
  }

  public boolean isGroup() {
    return guest.isGroup();
  }

  public boolean isInvite() {
    return guest.isInvite();
  }

  public void prepareForDrop() {
    viewWaiting.prepareForDrop();
  }

  public void setAlphaOnBackground(float alphaOnBackground) {
    backgroundView.setAlpha(alphaOnBackground);
    backgroundView.setVisibility(VISIBLE);

    if (alphaOnBackground == 1) {
      backgroundView.setVisibility(GONE);
    }
  }

  public void showGuest(boolean hasCountDown) {
    viewWaiting.showGuest();
    if (hasCountDown) viewWaiting.startCountdown();
    isWaiting = true;
  }

  public void startPulse() {
    viewWaiting.startPulse();
    isWaiting = true;
  }

  public void buzz() {
    viewWaiting.buzz();
  }

  public AvatarView avatar() {
    return viewWaiting.avatar();
  }

  @OnClick(R.id.layoutStream) void onClickStream(View v) {
    if (guest != null) onClick.onNext(guest);
    if (guest.getId().equals(Recipient.ID_CALL_ROULETTE)) {
      onRollTheDice.onNext(null);
    }
  }

  @OnClick(R.id.addFriend) void addFriendClick() {
    onClick.onNext(guest);
    hideAddBtn();
  }

  private void animateAddBtn() {
    btnAddFriend.setScaleX(0);
    btnAddFriend.setScaleY(0);

    new Handler().postDelayed(() -> {
      btnAddFriend.setVisibility(VISIBLE);
      SpringSystem springSystem = SpringSystem.create();
      Spring spring = springSystem.createSpring();
      SpringConfig config = new SpringConfig(TENSION, DAMPER);
      spring.setSpringConfig(config);
      spring.addListener(new SimpleSpringListener() {
        @Override public void onSpringUpdate(Spring spring) {
          float value = (float) spring.getCurrentValue();
          btnAddFriend.setScaleX(value);
          btnAddFriend.setScaleY(value);
        }
      });
      spring.setEndValue(1);
    }, 1000);

    //btnAddFriend.animate().scaleX(1f).scaleY(1f).setDuration(VISIBLE * 2);
  }

  private void hideAddBtn() {
    btnAddFriend.animate().scaleX(0).scaleY(0).setDuration(DURATION);
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Void> onShouldJoinRoom() {
    return viewWaiting.onShouldJoinRoom().distinct().doOnNext(aVoid -> viewWaiting.startPulse());
  }

  public Observable<Void> onNotifyStepDone() {
    return viewWaiting.onNotifyStepDone().distinct();
  }

  public Observable<TribeGuest> onShouldRemoveGuest() {
    return viewWaiting.onShouldRemoveGuest();
  }

  public Observable<TribeGuest> onClick() {
    return onClick;
  }

  public Observable<Void> onRollTheDice() {
    return onRollTheDice;
  }
}
