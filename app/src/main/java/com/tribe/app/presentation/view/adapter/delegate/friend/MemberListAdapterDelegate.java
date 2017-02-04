package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.base.AddAnimationAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.AddAnimationViewHolder;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by tiago on 11/29/16.
 */
public class MemberListAdapterDelegate extends AddAnimationAdapterDelegate<List<Object>> {

  @Inject User user;

  // VARIABLES
  private int avatarSize;

  // RX SUBSCRIPTIONS / SUBJECTS
  private PublishSubject<View> longClick = PublishSubject.create();
  private PublishSubject<View> onHangLive = PublishSubject.create();

  public MemberListAdapterDelegate(Context context) {
    super(context);
    avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return items.get(position) instanceof GroupMember;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    RecyclerView.ViewHolder vh =
        new GroupMemberViewHolder(layoutInflater.inflate(R.layout.item_member_list, parent, false));

    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    GroupMemberViewHolder vh = (GroupMemberViewHolder) holder;
    GroupMember groupMember = (GroupMember) items.get(position);

    if (animations.containsKey(holder)) {
      animations.get(holder).cancel();
    }

    vh.btnAdd.setVisibility((!user.equals(groupMember.getUser()) && !groupMember.getUser().isInvisibleMode()) ? View.VISIBLE : View.GONE);
    vh.imgGhost.setVisibility(groupMember.getUser().isInvisibleMode() ? View.VISIBLE : View.GONE);

    setFriendLabel(vh, groupMember.isFriend());

    if (groupMember.isAnimateAdd()) {
      animateAddSuccessful(vh, R.string.action_hang_live);
      groupMember.setAnimateAdd(false);
    } else {
      vh.txtAction.setAlpha(1);
      vh.progressBarAdd.setAlpha(0);

      if (!groupMember.isFriend()) {
        setAddFriendStyle(vh);
      } else {
        setHangLiveStyle(vh);
      }
    }

    vh.txtName.setText(groupMember.getUser().getDisplayName());

    if (!StringUtils.isEmpty(groupMember.getUser().getUsername())) {
      vh.txtUsername.setText("@" + groupMember.getUser().getUsername());
    } else {
      vh.txtUsername.setText("");
    }

    if (!StringUtils.isEmpty(groupMember.getUser().getProfilePicture())) {
      Glide.with(context)
          .load(groupMember.getUser().getProfilePicture())
          .thumbnail(0.25f)
          .error(R.drawable.picto_placeholder_avatar)
          .placeholder(R.drawable.picto_placeholder_avatar)
          .override(avatarSize, avatarSize)
          .bitmapTransform(new CropCircleTransformation(context))
          .crossFade()
          .into(vh.imgAvatar);
    }

    if (!groupMember.isFriend() && !groupMember.getUser().isInvisibleMode()) {
      vh.btnAdd.setOnClickListener(v -> {
        groupMember.setAnimateAdd(true);
        onClick(vh);
      });
    } else if (groupMember.isFriend()) {
      vh.btnAdd.setOnClickListener(v -> onHangLive.onNext(vh.itemView));
    }
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {

  }

  private void setHangLiveStyle(GroupMemberViewHolder vh) {
    vh.txtAction.setText(R.string.action_hang_live);
    vh.txtAction.measure(0, 0);
    UIUtils.changeWidthOfView(vh.btnAdd, vh.txtAction.getMeasuredWidth() + (2 * marginSmall));
    vh.btnAdd.setBackgroundResource(R.drawable.shape_rect_rounded_100_red);
    setAppearance(vh.txtAction);
  }

  private void setAddFriendStyle(GroupMemberViewHolder vh) {
    vh.txtAction.setText(R.string.action_add_friend);
    vh.txtAction.measure(0, 0);
    UIUtils.changeWidthOfView(vh.btnAdd, vh.txtAction.getMeasuredWidth() + (2 * marginSmall));
    vh.btnAdd.setBackgroundResource(R.drawable.shape_rect_rounded_100_blue_new);
    setAppearance(vh.txtAction);
  }

  private void setAppearance(TextViewFont txt) {
    TextViewCompat.setTextAppearance(txt, R.style.Body_Two_White);
    txt.setCustomFont(context, "Roboto-Bold.ttf");
  }

  private void setFriendLabel(GroupMemberViewHolder vh, boolean isFriend) {
    int visibility = isFriend ? View.VISIBLE : View.GONE;
    vh.txtFriend.setVisibility(visibility);
    vh.txtBubble.setVisibility(visibility);
  }

  public Observable<View> onLongClick() {
    return longClick;
  }

  public Observable<View> onHangLive() {
    return onHangLive;
  }

  static class GroupMemberViewHolder extends AddAnimationViewHolder {

    @BindView(R.id.imgAvatar) ImageView imgAvatar;

    @BindView(R.id.txtName) TextViewFont txtName;

    @BindView(R.id.txtUsername) TextViewFont txtUsername;

    @BindView(R.id.imgGhost) ImageView imgGhost;

    @BindView(R.id.txtBubble) TextViewFont txtBubble;

    @BindView(R.id.txtFriend) TextViewFont txtFriend;

    public GroupMemberViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
