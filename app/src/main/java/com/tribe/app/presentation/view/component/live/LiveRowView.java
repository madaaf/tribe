package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.view.PeerView;
import com.tribe.tribelivesdk.view.RemotePeerView;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveRowView extends FrameLayout {

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewWaiting) LiveWaitingView viewWaiting;

  @BindView(R.id.viewAudio) LiveAudioView viewAudio;

  @BindView(R.id.layoutStream) ViewGroup layoutStream;

  // VARIABLES
  private Unbinder unbinder;
  private RemotePeerView remotePeerView;
  private TribeGuest guest;
  private int color;
  private boolean isWaiting = false;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

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
      viewAudio.setGuest(guest);
    }

    viewWaiting.setColor(color);

    if (remotePeerView != null) viewWaiting.setVisibility(View.GONE);
  }

  public void dispose() {
    viewWaiting.dispose();
    subscriptions.clear();
  }

  public void setColor(int color) {
    this.color = color;
    viewWaiting.setColor(color);
  }

  public void setGuest(TribeGuest guest) {
    this.guest = guest;
    viewWaiting.setGuest(guest);
    viewAudio.setGuest(guest);
  }

  public void setRoomType(@LiveRoomView.TribeRoomViewType int type) {
    viewWaiting.setRoomType(type);
  }

  public void setPeerView(PeerView peerView) {
    remotePeerView = (RemotePeerView) peerView;
    remotePeerView.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            layoutStream.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            UIUtils.showReveal(layoutStream, new AnimatorListenerAdapter() {
              @Override public void onAnimationEnd(Animator animation) {
                viewWaiting.setVisibility(View.GONE);
              }

              @Override public void onAnimationStart(Animator animation) {
                layoutStream.setVisibility(View.VISIBLE);
              }
            });
          }
        });

    subscriptions.add(this.remotePeerView.onMediaConfiguration()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tribePeerMediaConfiguration -> {
          if (tribePeerMediaConfiguration.isVideoEnabled()) {
            UIUtils.showReveal(remotePeerView, new AnimatorListenerAdapter() {
              @Override public void onAnimationEnd(Animator animation) {
                viewAudio.setVisibility(View.GONE);
              }

              @Override public void onAnimationStart(Animator animation) {
                remotePeerView.setVisibility(View.VISIBLE);
              }
            });
          } else {
            UIUtils.hideReveal(remotePeerView, new AnimatorListenerAdapter() {
              @Override public void onAnimationStart(Animator animation) {
                viewAudio.setVisibility(View.VISIBLE);
              }

              @Override public void onAnimationEnd(Animator animation) {
                remotePeerView.setVisibility(View.GONE);
              }
            });
          }
        }));


    isWaiting = false;
    viewWaiting.incomingPeer();

    subscriptions.add(Observable.timer(2000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          ViewGroup.LayoutParams params =
              new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.MATCH_PARENT);
          layoutStream.addView(remotePeerView, params);
        }));
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

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Void> onShouldJoinRoom() {
    return viewWaiting.onShouldJoinRoom().distinct().doOnNext(aVoid -> viewWaiting.startPulse());
  }

  public Observable<TribeGuest> onShouldRemoveGuest() {
    return viewWaiting.onShouldRemoveGuest();
  }
}
