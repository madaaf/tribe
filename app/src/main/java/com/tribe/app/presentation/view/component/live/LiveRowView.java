package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
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

  @Inject ScreenUtils screenUtils;

  @Inject User user;

  @BindView(R.id.viewPeerOverlay) LivePeerOverlayView viewPeerOverlay;

  @BindView(R.id.layoutStream) ViewGroup layoutStream;

  @BindView(R.id.viewBackground) View backgroundView;

  // VARIABLES
  private Unbinder unbinder;
  private RemotePeerView remotePeerView;
  private TribeGuest guest;
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
      viewPeerOverlay.setGuest(guest);
    }
  }

  public void guestAppear() {
    setAddBtn(guest);
  }

  public void setAddBtn(TribeGuest guest) {
    for (Shortcut shortcut : user.getShortcutList()) {
      User friend = shortcut.getSingleFriend();
      if (guest.getId().endsWith(friend.getId())) {
        if (shortcut.getStatus().equals(ShortcutRealm.HIDDEN) ||
            shortcut.getStatus().equals(ShortcutRealm.BLOCKED)) {
          guest.setFriend(false);
        } else {
          guest.setFriend(true);
        }
        break;
      } else {
        guest.setFriend(false);
      }
    }
  }

  public void dispose() {
    subscriptions.clear();
  }

  public void setGuest(TribeGuest guest) {
    this.guest = guest;
    viewPeerOverlay.setGuest(guest);
  }

  public void setPeerView(PeerView peerView) {
    if (peerView == null) {
      isWaiting = false;
    } else {
      remotePeerView = (RemotePeerView) peerView;

      subscriptions.add(this.remotePeerView.onNotificationRemoteJoined()
          .observeOn(AndroidSchedulers.mainThread())
          .doOnNext(peerView1 -> layoutStream.setVisibility(View.VISIBLE))
          .subscribe(s -> UIUtils.showReveal(layoutStream, true, null)));

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

      ViewGroup.LayoutParams params =
          new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT);
      if (remotePeerView.getParent() == null) layoutStream.addView(remotePeerView, params);
    }
  }

  public TribeGuest getGuest() {
    return guest;
  }

  public boolean isWaiting() {
    return isWaiting;
  }

  public void setAlphaOnBackground(float alphaOnBackground) {
    backgroundView.setAlpha(alphaOnBackground);
    backgroundView.setVisibility(VISIBLE);

    if (alphaOnBackground == 1) {
      backgroundView.setVisibility(GONE);
    }
  }

  @OnClick(R.id.layoutStream) void onClickStream(View v) {
    if (guest != null) onClick.onNext(guest);
    if (guest.getId().equals(Recipient.ID_CALL_ROULETTE)) {
      onRollTheDice.onNext(null);
    }
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Void> onRollTheDice() {
    return onRollTheDice;
  }

  public Observable<TribeGuest> onClick() {
    return onClick;
  }
}
