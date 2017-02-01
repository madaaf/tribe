package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;
import java.util.Objects;
import org.w3c.dom.Text;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 11/22/16.
 */
public class FriendMemberAdapterDelegate extends RxAdapterDelegate<List<Object>> {

  private static final int DURATION_SCALE = 350;
  private static final float OVERSHOOT = 0.45f;

  // RX SUBSCRIPTIONS / SUBJECTS
  private PublishSubject<View> clickAdd = PublishSubject.create();

  // VARIABLES
  private int avatarSize;
  private Context context;
  private LayoutInflater layoutInflater;

  public FriendMemberAdapterDelegate(Context context) {
    this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return items.get(position) instanceof GroupMember;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    RecyclerView.ViewHolder vh = new FriendMemberViewHolder(
        layoutInflater.inflate(R.layout.item_friend_member, parent, false));
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    FriendMemberViewHolder vh = (FriendMemberViewHolder) holder;
    GroupMember groupMember = (GroupMember) items.get(position);

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

    if (groupMember.isMember()) {
      setAddedInGroupStyle(vh);
    } else {
      setAddInGroupStyle(vh);
    }

    if (!groupMember.isOgMember()) {
      vh.btnAdd.setOnClickListener(v -> {
        if (groupMember.isMember()) {
          groupMember.setMember(false);
        } else {
          groupMember.setMember(true);
        }

        clickAdd.onNext(vh.itemView);
      });
    } else {
      vh.itemView.setOnClickListener(null);
    }
  }

  private void setAddInGroupStyle(FriendMemberViewHolder vh) {
    vh.txtAction.setText(R.string.group_add_members_in_group);
    vh.btnAdd.setBackgroundResource(R.drawable.shape_rect_rounded_100_violet);
    TextViewCompat.setTextAppearance(vh.txtAction, R.style.Body_Two_White);
    vh.txtAction.setCustomFont(context, "Roboto-Bold.ttf");
  }

  private void setAddedInGroupStyle(FriendMemberViewHolder vh) {
    vh.txtAction.setText(R.string.group_add_members_added);
    vh.btnAdd.setBackgroundResource(R.drawable.shape_rect_rounded_100_violet_10);
    TextViewCompat.setTextAppearance(vh.txtAction, R.style.Body_Two_Purple);
    vh.txtAction.setCustomFont(context, "Roboto-Bold.ttf");
  }

  private void setFriendLabel(FriendMemberViewHolder vh, boolean isFriend) {
    int visibility = isFriend ? View.VISIBLE : View.GONE;
    vh.txtFriend.setVisibility(visibility);
    vh.txtBubble.setVisibility(visibility);
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
  }

  public Observable<View> clickAdd() {
    return clickAdd;
  }

  static class FriendMemberViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.imgAvatar) ImageView imgAvatar;

    @BindView(R.id.txtName) TextViewFont txtName;

    @BindView(R.id.txtUsername) TextViewFont txtUsername;

    @BindView(R.id.txtFriend) TextViewFont txtFriend;

    @BindView(R.id.txtBubble) TextViewFont txtBubble;

    @BindView(R.id.btnAdd) View btnAdd;

    @BindView(R.id.txtAction) TextViewFont txtAction;

    public FriendMemberViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
