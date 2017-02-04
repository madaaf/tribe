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
import com.tribe.app.domain.entity.Contact;
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

/**
 * Created by tiago on 11/29/16.
 */
public class UserListAdapterDelegate extends AddAnimationAdapterDelegate<List<Object>> {

  @Inject User user;

  // VARIABLES
  private int avatarSize;

  // RX SUBSCRIPTIONS / SUBJECTS

  public UserListAdapterDelegate(Context context) {
    super(context);
    avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    if (items.get(position) instanceof User) {
      User user = (User) items.get(position);
      return !user.getId().equals(User.ID_EMPTY);
    } else {
      return false;
    }
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    RecyclerView.ViewHolder vh = new UserListViewHolder(
        layoutInflater.inflate(R.layout.item_user_list, parent, false));

    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    UserListViewHolder vh = (UserListViewHolder) holder;
    User user =
        items.get(position) instanceof Contact ? ((Contact) items.get(position)).getUserList()
            .get(0) : (User) items.get(position);

    if (animations.containsKey(holder)) {
      animations.get(holder).cancel();
    }

    vh.btnAdd.setVisibility(
        (!user.isInvisibleMode() && !user.isFriend())
            ? View.VISIBLE : View.GONE);
    vh.imgGhost.setVisibility(user.isInvisibleMode() ? View.VISIBLE : View.GONE);

    setFriendLabel(vh, user.isFriend());

    if (user.isAnimateAdd()) {
      animateAddSuccessful(vh, R.string.action_member_added);
      user.setAnimateAdd(false);
    } else {
      vh.txtAction.setAlpha(1);
      vh.progressBarAdd.setAlpha(0);

      if (!user.isFriend()) {
        setAddFriendStyle(vh);
      }
    }

    vh.txtName.setText(user.getDisplayName());

    if (!StringUtils.isEmpty(user.getUsername())) {
      vh.txtUsername.setText("@" + user.getUsername());
    } else {
      vh.txtUsername.setText("");
    }

    if (!StringUtils.isEmpty(user.getProfilePicture())) {
      Glide.with(context)
          .load(user.getProfilePicture())
          .thumbnail(0.25f)
          .error(R.drawable.picto_placeholder_avatar)
          .placeholder(R.drawable.picto_placeholder_avatar)
          .override(avatarSize, avatarSize)
          .bitmapTransform(new CropCircleTransformation(context))
          .crossFade()
          .into(vh.imgAvatar);
    }

    if (!user.isNewFriend() && !user.isFriend() && !user.isInvisibleMode()) {
      vh.btnAdd.setOnClickListener(v -> {
        user.setAnimateAdd(true);
        user.setNewFriend(true);
        onClick(vh);
      });
    } else if (user.isNewFriend()) {
      vh.btnAdd.setOnClickListener(v -> {
        user.setNewFriend(false);
        user.setFriend(false);
        onClick(vh);
      });
    } else if (!user.isNewFriend()) {
      vh.btnAdd.setOnClickListener(null);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
  }

  private void setAddFriendStyle(UserListViewHolder vh) {
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

  private void setFriendLabel(UserListViewHolder vh,
      boolean isFriend) {
    int visibility = isFriend ? View.VISIBLE : View.GONE;
    vh.txtFriend.setVisibility(visibility);
    vh.txtBubble.setVisibility(visibility);
  }

  static class UserListViewHolder extends AddAnimationViewHolder {

    @BindView(R.id.imgAvatar) ImageView imgAvatar;

    @BindView(R.id.txtName) TextViewFont txtName;

    @BindView(R.id.txtUsername) TextViewFont txtUsername;

    @BindView(R.id.imgGhost) ImageView imgGhost;

    @BindView(R.id.txtBubble) TextViewFont txtBubble;

    @BindView(R.id.txtFriend) TextViewFont txtFriend;

    public UserListViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
