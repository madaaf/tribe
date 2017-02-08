package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.base.AddAnimationAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.AddAnimationViewHolder;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by tiago on 11/22/16.
 */
public class FriendMemberAdapterDelegate extends AddAnimationAdapterDelegate<List<Object>> {

  private static final int DURATION_SCALE = 350;
  private static final float OVERSHOOT = 0.45f;

  @Inject
  ScreenUtils screenUtils;

  // RX SUBSCRIPTIONS / SUBJECTS

  // VARIABLES
  private int avatarSize;

  public FriendMemberAdapterDelegate(Context context) {
    super(context);
    this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return items.get(position) instanceof GroupMember;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    FriendMemberViewHolder vh = new FriendMemberViewHolder(
        layoutInflater.inflate(R.layout.item_friend_member, parent, false));
    vh.gradientDrawable = new GradientDrawable();
    vh.gradientDrawable.setShape(GradientDrawable.RECTANGLE);
    vh.gradientDrawable.setCornerRadius(screenUtils.dpToPx(100));
    vh.btnAdd.setBackground(vh.gradientDrawable);
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    FriendMemberViewHolder vh = (FriendMemberViewHolder) holder;
    GroupMember groupMember = (GroupMember) items.get(position);

    if (animations.containsKey(holder)) {
      animations.get(holder).cancel();
    }

    if (!StringUtils.isEmpty(groupMember.getUser().getProfilePicture())) {
      Glide.with(context)
          .load(groupMember.getUser().getProfilePicture())
          .placeholder(R.drawable.picto_placeholder_avatar)
          .error(R.drawable.picto_placeholder_avatar)
          .thumbnail(0.25f)
          .override(avatarSize, avatarSize)
          .bitmapTransform(new CropCircleTransformation(context))
          .crossFade()
          .into(vh.imgAvatar);
    }

    vh.txtName.setText(groupMember.getUser().getDisplayName());

    setFriendLabel(vh, groupMember.isFriend());

    if (!StringUtils.isEmpty(groupMember.getUser().getUsername())) {
      vh.txtUsername.setText("@" + groupMember.getUser().getUsername());
    } else {
      vh.txtUsername.setText("");
    }

    vh.txtFriend.setVisibility(groupMember.isFriend() ? View.VISIBLE : View.GONE);

    if (groupMember.isAnimateAdd()) {
      groupMember.setAnimateAdd(false);
      animateAdd(vh, !groupMember.isMember());
    } else {
      if (groupMember.isMember()) {
        setAddedInGroupStyle(vh);
      } else {
        setAddInGroupStyle(vh);
      }
    }

    if (!groupMember.isOgMember()) {
      vh.btnAdd.setOnClickListener(v -> onClick(groupMember, vh));
      vh.itemView.setOnClickListener(v -> onClick(groupMember, vh));
    } else {
      vh.itemView.setOnClickListener(null);
    }
  }

  private void onClick(GroupMember groupMember, FriendMemberViewHolder vh) {
    if (groupMember.isMember()) {
      groupMember.setMember(false);
    } else {
      groupMember.setMember(true);
    }

    groupMember.setAnimateAdd(true);
    clickAdd.onNext(vh.itemView);
  }

  private void animateAdd(FriendMemberViewHolder vh, boolean reverse) {
    AnimatorSet animatorSet = new AnimatorSet();

    vh.txtAction.setText(reverse ? R.string.group_add_members_in_group : R.string.action_member_added);
    vh.txtAction.measure(0, 0);

    Animator animator = AnimationUtils.getWidthAnimator(vh.btnAdd, vh.btnAdd.getWidth(),
        vh.txtAction.getMeasuredWidth() + (2 * marginSmall));

    animatorSet.setDuration(DURATION);
    animatorSet.setInterpolator(new DecelerateInterpolator());
    animatorSet.play(animator);
    animatorSet.start();

    if (reverse) {
      AnimationUtils.animateTextColor(vh.txtAction, ContextCompat.getColor(context, R.color.violet), Color.WHITE, DURATION);
      AnimationUtils.animateBGColor(vh.btnAdd, ContextCompat.getColor(context, R.color.violet_opacity_10),
          ContextCompat.getColor(context, R.color.violet), DURATION);
    } else {
      AnimationUtils.animateTextColor(vh.txtAction, Color.WHITE, ContextCompat.getColor(context, R.color.violet), DURATION);
      AnimationUtils.animateBGColor(vh.btnAdd, ContextCompat.getColor(context, R.color.violet),
          ContextCompat.getColor(context, R.color.violet_opacity_10), DURATION);
    }
  }

  private void setAddInGroupStyle(FriendMemberViewHolder vh) {
    vh.txtAction.setText(R.string.group_add_members_in_group);
    TextViewCompat.setTextAppearance(vh.txtAction, R.style.Body_Two_White);
    vh.txtAction.setCustomFont(context, "Roboto-Bold.ttf");
    vh.gradientDrawable.setColor(ContextCompat.getColor(context, R.color.violet));
  }

  private void setAddedInGroupStyle(FriendMemberViewHolder vh) {
    vh.txtAction.setText(R.string.group_add_members_added);
    TextViewCompat.setTextAppearance(vh.txtAction, R.style.Body_Two_Purple);
    vh.txtAction.setCustomFont(context, "Roboto-Bold.ttf");
    vh.gradientDrawable.setColor(ContextCompat.getColor(context, R.color.violet_opacity_10));
  }

  private void setFriendLabel(FriendMemberViewHolder vh, boolean isFriend) {
    int visibility = isFriend ? View.VISIBLE : View.GONE;
    vh.txtFriend.setVisibility(visibility);
    vh.txtBubble.setVisibility(visibility);
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
  }

  static class FriendMemberViewHolder extends AddAnimationViewHolder {

    @BindView(R.id.imgAvatar) ImageView imgAvatar;

    @BindView(R.id.txtName) TextViewFont txtName;

    @BindView(R.id.txtUsername) TextViewFont txtUsername;

    @BindView(R.id.txtFriend) TextViewFont txtFriend;

    @BindView(R.id.txtBubble) TextViewFont txtBubble;

    GradientDrawable gradientDrawable;

    public FriendMemberViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
