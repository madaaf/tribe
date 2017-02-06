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
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.base.AddAnimationAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.AddAnimationViewHolder;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarLiveView;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 11/29/16.
 */
public class RecipientListAdapterDelegate extends AddAnimationAdapterDelegate<List<Object>> {

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
    RecyclerView.ViewHolder vh = new RecipientListViewHolder(
        layoutInflater.inflate(R.layout.item_recipient_list, parent, false));

    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    RecipientListViewHolder vh = (RecipientListViewHolder) holder;
    Recipient recipient = (Recipient) items.get(position);

    if (animations.containsKey(holder)) {
      animations.get(holder).cancel();
    }

    if (recipient.isLive()) vh.avatar.setType(AvatarLiveView.LIVE);
    else if (recipient.isOnline()) vh.avatar.setType(AvatarLiveView.CONNECTED);
    else vh.avatar.setType(AvatarLiveView.NONE);

    vh.txtName.setText(recipient.getDisplayName());

    if (recipient instanceof Membership) {
      Membership membership = (Membership) recipient;
      int size = membership.getGroup().getMembers() == null ? 0 : membership.getGroup().getMembers().size();
      vh.txtDetails.setText(size + " " + (size > 1 ? context.getString(R.string.group_members) : context.getString(R.string.group_member)));
      setLive(vh);
    } else {
      Friendship friendship = (Friendship) recipient;
      if (friendship.getStatus().equals(FriendshipRealm.BLOCKED) || friendship.getStatus().equals(FriendshipRealm.HIDDEN)) {
        setUnblock(vh);
      } else {
        setLive(vh);
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
      vh.btnAdd.setOnClickListener(v -> {
        setLive(vh);
        onUnblock.onNext(vh.itemView);
      });
    } else {
      vh.btnAdd.setOnClickListener(v -> {
        setUnblock(vh);
        onHangLive.onNext(vh.itemView);
      });
    }
  }

  private void setLive(RecipientListViewHolder vh) {
    setClicks(vh, false);
    vh.txtAction.setText(R.string.action_hang_live);
    vh.btnAdd.setBackgroundResource(R.drawable.shape_rect_rounded_100_red);
  }

  private void setUnblock(RecipientListViewHolder vh) {
    setClicks(vh, true);
    vh.txtAction.setText(R.string.action_unblock);
    vh.btnAdd.setBackgroundResource(R.drawable.shape_rect_rounded_100_grey);
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
