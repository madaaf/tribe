package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import com.tribe.app.presentation.view.widget.header.LiveInviteViewHeader;
import java.util.List;

/**
 * Created by tiago on 09/04/2017
 */
public class UserRoomAdapterDelegate
    extends RxAdapterDelegate<List<LiveInviteViewHeader.LiveInviteAdapterSectionInterface>> {

  protected LayoutInflater layoutInflater;
  protected Context context;

  // RX SUBSCRIPTIONS / SUBJECTS

  public UserRoomAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(
      @NonNull List<LiveInviteViewHeader.LiveInviteAdapterSectionInterface> items, int position) {
    return items.get(position) instanceof User && items.get(position).getId() != Recipient.ID_HEADER;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    UserRoomViewHolder userRoomViewHolder =
        new UserRoomViewHolder(layoutInflater.inflate(R.layout.item_user_room, parent, false));
    return userRoomViewHolder;
  }

  @Override public void onBindViewHolder(
      @NonNull List<LiveInviteViewHeader.LiveInviteAdapterSectionInterface> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    UserRoomViewHolder vh = (UserRoomViewHolder) holder;
    User user = (User) items.get(position);
    vh.txtName.setText(user.getDisplayName());
    vh.viewNewAvatar.setType(user.isOnline() ? NewAvatarView.ONLINE : NewAvatarView.NORMAL);
    vh.viewNewAvatar.load(user.getProfilePicture());
  }

  @Override public void onBindViewHolder(
      @NonNull List<LiveInviteViewHeader.LiveInviteAdapterSectionInterface> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

  }

  static class UserRoomViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.viewNewAvatar) NewAvatarView viewNewAvatar;
    @BindView(R.id.txtName) TextViewFont txtName;
    @BindView(R.id.txtStatus) TextViewFont txtStatus;

    public UserRoomViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
