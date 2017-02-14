package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.base.AddAnimationAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.AddAnimationViewHolder;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarLiveView;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 11/29/16.
 */
public class RecipientListAdapterDelegate extends AddAnimationAdapterDelegate<List<Object>> {

  @Inject ScreenUtils screenUtils;

  // VARIABLES
  private int avatarSize;

  // RX SUBSCRIPTIONS / SUBJECTS
  private PublishSubject<View> onHangLive = PublishSubject.create();
  private PublishSubject<View> onUnblock = PublishSubject.create();

  public RecipientListAdapterDelegate(Context context) {
    super(context);
    avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_smaller);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return items.get(position) instanceof Recipient;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    RecipientListViewHolder vh = new RecipientListViewHolder(
        layoutInflater.inflate(R.layout.item_recipient_list, parent, false));
    vh.gradientDrawable = new GradientDrawable();
    vh.gradientDrawable.setShape(GradientDrawable.RECTANGLE);
    vh.gradientDrawable.setCornerRadius(screenUtils.dpToPx(100));
    vh.btnAdd.setBackground(vh.gradientDrawable);

    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    RecipientListViewHolder vh = (RecipientListViewHolder) holder;
    Recipient recipient = (Recipient) items.get(position);

    if (recipient.isLive()) {
      vh.avatar.setType(AvatarLiveView.LIVE);
    } else if (recipient.isOnline()) {
      vh.avatar.setType(AvatarLiveView.CONNECTED);
    } else {
      vh.avatar.setType(AvatarLiveView.NONE);
    }

    vh.txtName.setText(recipient.getDisplayName());

    if (recipient instanceof Membership) {
      Membership membership = (Membership) recipient;
      int size = membership.getGroup().getMembers() == null ? 0
          : membership.getGroup().getMembers().size();
      vh.txtDetails.setText(size + " " + (size > 1 ? context.getString(R.string.group_members)
          : context.getString(R.string.group_member)));
      setLive(vh);
    } else {
      Friendship friendship = (Friendship) recipient;
      if (friendship.isAnimateAdd()) {
        friendship.setAnimateAdd(false);
        animateAdd(vh, !friendship.isBlockedOrHidden());
      } else {
        if (friendship.isBlocked()) {
          setUnblock(vh);
        } else if (friendship.isHidden()) {
          setUnhide(vh);
        } else {
          setLive(vh);
        }
      }

      vh.txtDetails.setText("@" + recipient.getUsername());
    }

    vh.avatar.load(recipient);
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
  }

  private void setClicks(RecipientListViewHolder vh, boolean isBlockedOrHidden) {
    if (isBlockedOrHidden) {
      vh.btnAdd.setOnClickListener(v -> onUnblock.onNext(vh.itemView));
    } else {
      vh.btnAdd.setOnClickListener(v -> onHangLive.onNext(vh.itemView));
    }
  }

  private void setLive(RecipientListViewHolder vh) {
    setClicks(vh, false);
    vh.txtAction.setText(R.string.action_hang_live);
    vh.gradientDrawable.setColor(ContextCompat.getColor(context, R.color.red));
  }

  private void setUnblock(RecipientListViewHolder vh) {
    setClicks(vh, true);
    vh.txtAction.setText(R.string.action_unblock);
    vh.gradientDrawable.setColor(ContextCompat.getColor(context, R.color.grey_unblock));
  }

  private void setUnhide(RecipientListViewHolder vh) {
    setClicks(vh, true);
    vh.txtAction.setText(R.string.action_unhide);
    vh.gradientDrawable.setColor(ContextCompat.getColor(context, R.color.blue_new));
  }

  private void animateAdd(RecipientListViewHolder vh, boolean reverse) {
    AnimatorSet animatorSet = new AnimatorSet();

    vh.txtAction.setText(reverse ? R.string.action_hang_live : R.string.action_unblock);
    vh.txtAction.measure(0, 0);

    Animator animator = AnimationUtils.getWidthAnimator(vh.btnAdd, vh.btnAdd.getWidth(),
        vh.txtAction.getMeasuredWidth() + (2 * marginSmall));

    animatorSet.setDuration(DURATION);
    animatorSet.setInterpolator(new DecelerateInterpolator());
    animatorSet.play(animator);
    animatorSet.start();

    if (reverse) {
      AnimationUtils.animateBGColor(vh.btnAdd,
          ContextCompat.getColor(context, R.color.grey_unblock),
          ContextCompat.getColor(context, R.color.red), DURATION);
    } else {
      AnimationUtils.animateBGColor(vh.btnAdd, ContextCompat.getColor(context, R.color.red),
          ContextCompat.getColor(context, R.color.grey_unblock), DURATION);
    }
  }

  public Observable<View> onHangLive() {
    return onHangLive;
  }

  public Observable<View> onUnblock() {
    return onUnblock;
  }

  static class RecipientListViewHolder extends AddAnimationViewHolder {

    @BindView(R.id.avatar) AvatarLiveView avatar;

    @BindView(R.id.txtName) TextViewFont txtName;

    @BindView(R.id.txtDetails) TextViewFont txtDetails;

    public RecipientListViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
