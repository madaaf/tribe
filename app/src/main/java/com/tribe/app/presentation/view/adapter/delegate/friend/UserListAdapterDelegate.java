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
import com.tribe.app.R;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.base.AddAnimationAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.AddAnimationViewHolder;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by tiago on 11/29/16.
 */
public class UserListAdapterDelegate extends AddAnimationAdapterDelegate<List<Object>> {

  @Inject User user;

  @Inject ScreenUtils screenUtils;

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
    UserListViewHolder vh =
        new UserListViewHolder(layoutInflater.inflate(R.layout.item_user_list, parent, false));

    vh.gradientDrawable = new GradientDrawable();
    vh.gradientDrawable.setShape(GradientDrawable.RECTANGLE);
    vh.gradientDrawable.setCornerRadius(screenUtils.dpToPx(100));
    vh.btnAdd.setBackground(vh.gradientDrawable);

    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    UserListViewHolder vh = (UserListViewHolder) holder;
    User user =
        items.get(position) instanceof Contact ? ((Contact) items.get(position)).getUserList()
            .get(0) : (User) items.get(position);

    vh.btnAdd.setVisibility((!user.isInvisibleMode()) ? View.VISIBLE : View.GONE);
    vh.imgGhost.setVisibility(user.isInvisibleMode() ? View.VISIBLE : View.GONE);

    setFriendLabel(vh, user.isFriend());

    if (user.isAnimateAdd()) {
      animateAdd(vh, !user.isNewFriend());
      user.setAnimateAdd(false);
    } else {
      if (!user.isFriend()) {
        setAddFriendStyle(vh);
      } else {
        setAddedFriendStyle(vh);
      }
    }

    vh.txtName.setText(user.getDisplayName());

    if (!StringUtils.isEmpty(user.getUsername())) {
      vh.txtUsername.setText("@" + user.getUsername());
    } else {
      vh.txtUsername.setText("");
    }

    if (!StringUtils.isEmpty(user.getProfilePicture())) {
      GlideUtils.load(context, user.getProfilePicture(), avatarSize, vh.imgAvatar);
    }

    if (!user.isFriend() && !user.isInvisibleMode()) {
      vh.btnAdd.setOnClickListener(v -> {
        user.setAnimateAdd(true);
        clickAdd.onNext(vh.itemView);
      });
    } else if (user.isFriend()) {
      vh.btnAdd.setOnClickListener(null);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
  }

  private void setAddFriendStyle(UserListViewHolder vh) {
    vh.txtAction.setText(R.string.action_add_friend);
    measure(vh);
    vh.gradientDrawable.setColor(ContextCompat.getColor(context, R.color.blue_new));
    TextViewCompat.setTextAppearance(vh.txtAction, R.style.Body_Two_White);
    setAppearance(vh.txtAction);
  }

  private void setAddedFriendStyle(UserListViewHolder vh) {
    vh.txtAction.setText(R.string.action_member_added);
    measure(vh);
    vh.gradientDrawable.setColor(ContextCompat.getColor(context, R.color.blue_new_opacity_10));
    TextViewCompat.setTextAppearance(vh.txtAction, R.style.Body_Two_BlueNew);
    setAppearance(vh.txtAction);
  }

  private void measure(UserListViewHolder vh) {
    vh.txtAction.measure(0, 0);
    UIUtils.changeWidthOfView(vh.btnAdd, vh.txtAction.getMeasuredWidth() + (2 * marginSmall));
  }

  private void animateAdd(UserListViewHolder vh, boolean reverse) {
    AnimatorSet animatorSet = new AnimatorSet();

    vh.txtAction.setText(reverse ? R.string.action_add_friend : R.string.action_member_added);
    vh.txtAction.measure(0, 0);

    Animator animator = AnimationUtils.getWidthAnimator(vh.btnAdd, vh.btnAdd.getWidth(),
        vh.txtAction.getMeasuredWidth() + (2 * marginSmall));

    animatorSet.setDuration(DURATION);
    animatorSet.setInterpolator(new DecelerateInterpolator());
    animatorSet.play(animator);
    animatorSet.start();

    if (reverse) {
      AnimationUtils.animateTextColor(vh.txtAction,
          ContextCompat.getColor(context, R.color.blue_new), Color.WHITE, DURATION);
      AnimationUtils.animateBGColor(vh.btnAdd,
          ContextCompat.getColor(context, R.color.blue_new_opacity_10),
          ContextCompat.getColor(context, R.color.blue_new), DURATION);
    } else {
      AnimationUtils.animateTextColor(vh.txtAction, Color.WHITE,
          ContextCompat.getColor(context, R.color.blue_new), DURATION);
      AnimationUtils.animateBGColor(vh.btnAdd, ContextCompat.getColor(context, R.color.blue_new),
          ContextCompat.getColor(context, R.color.blue_new_opacity_10), DURATION);
    }
  }

  private void setAppearance(TextViewFont txt) {
    txt.setCustomFont(context, "Roboto-Bold.ttf");
  }

  private void setFriendLabel(UserListViewHolder vh, boolean isFriend) {
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

    GradientDrawable gradientDrawable;

    public UserListViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
