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
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.view.PeerView;
import com.tribe.tribelivesdk.view.RemotePeerView;
import javax.inject.Inject;
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

  // VARIABLES
  private Unbinder unbinder;
  private RemotePeerView remotePeerView;
  private TribeGuest guest;
  private int color;

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
    subscriptions.clear();
  }

  public void setColor(int color) {
    this.color = color;
    if (viewWaiting != null) viewWaiting.setColor(color);
  }

  public void setGuest(TribeGuest guest) {
    this.guest = guest;
    if (viewWaiting != null) viewWaiting.setGuest(guest);
    if (viewAudio != null) viewAudio.setGuest(guest);
  }

  public void setRoomType(@LiveRoomView.TribeRoomViewType int type) {
    if (viewWaiting != null) viewWaiting.setRoomType(type);
  }

  public void setPeerView(PeerView peerView) {
    this.remotePeerView = (RemotePeerView) peerView;

    subscriptions.add(this.remotePeerView.onMediaConfiguration()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tribePeerMediaConfiguration -> {
          if (tribePeerMediaConfiguration.isVideoEnabled()) {
            UIUtils.showReveal(remotePeerView, new AnimatorListenerAdapter() {
              @Override public void onAnimationEnd(Animator animation) {
                Timber.d("onAnimationEnd");
                viewAudio.setVisibility(View.GONE);
              }

              @Override public void onAnimationStart(Animator animation) {
                remotePeerView.setVisibility(View.VISIBLE);
              }
            });
          } else {
            UIUtils.hideReveal(remotePeerView, new AnimatorListenerAdapter() {
              @Override public void onAnimationStart(Animator animation) {
                Timber.d("onAnimationStart");
                viewAudio.setVisibility(View.VISIBLE);
              }

              @Override public void onAnimationEnd(Animator animation) {
                remotePeerView.setVisibility(View.GONE);
              }
            });
          }
        }));

    if (viewWaiting != null) {
      viewWaiting.stopPulse();
      viewWaiting.setVisibility(View.GONE);
    }

    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
    addView(this.remotePeerView, params);
  }

  public TribeGuest getGuest() {
    return guest;
  }

  public void startPulse() {
    if (viewWaiting != null) viewWaiting.startPulse();
  }
}
