package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.model.TribeGuest;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/16/18.
 */
public class LiveRowViewScores extends LinearLayout {

  @BindView(R.id.viewAvatar) AvatarView viewAvatar;

  @BindView(R.id.txtScore) TextViewFont txtScore;

  @BindView(R.id.txtEmoji) TextViewFont txtEmoji;

  // VARIABLES
  private Unbinder unbinder;
  private TribeGuest guest;

  // RESOURCES
  private int avatarSize;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LiveRowViewScores(Context context) {
    super(context);
    init();
  }

  public LiveRowViewScores(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveRowViewScores(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_row_live_scores, this);
    unbinder = ButterKnife.bind(this);

    setOrientation(HORIZONTAL);
    setGravity(Gravity.CENTER);
  }

  private void initResources() {
    avatarSize = getContext().getResources().getDimensionPixelSize(R.dimen.avatar_size_chat);
  }

  ////////////
  // PUBLIC //
  ////////////

  public void dispose() {
    if (subscriptions != null) subscriptions.clear();
    if (unbinder != null) unbinder.unbind();
  }

  public void updateScores(Pair<Integer, String> pair) {
    this.txtEmoji.setText(pair.second);
    this.txtScore.setText("" + pair.first);
  }

  public void setGuest(TribeGuest tribeGuest) {
    this.guest = tribeGuest;
  }

  public void show() {
    viewAvatar.load(guest.getPicture());
  }

  public TribeGuest getGuest() {
    return guest;
  }
}
