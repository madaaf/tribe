package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.model.TribeGuest;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/16/18.
 */
public class LiveRowViewScores extends LinearLayout {

  @BindView(R.id.viewAvatar) ImageView imgAvatar;

  @BindView(R.id.txtScore) TextViewFont txtScore;

  @BindView(R.id.txtEmoji) TextViewFont txtEmoji;

  // VARIABLES
  private Unbinder unbinder;
  private TribeGuest guest;

  // RESOURCES
  private int avatarSize, roundedCorners;

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
    avatarSize = getContext().getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
    roundedCorners =
        getContext().getResources().getDimensionPixelSize(R.dimen.avatar_live_rounded_corners);
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
    Glide.with(getContext())
        .load(guest.getPicture())
        .thumbnail(0.25f)
        .placeholder(R.drawable.picto_avatar_placeholder)
        .error(R.drawable.picto_avatar_placeholder)
        .centerCrop()
        .bitmapTransform(new RoundedCornersTransformation(getContext(), roundedCorners, 0))
        .into(imgAvatar);
  }

  public TribeGuest getGuest() {
    return guest;
  }
}
