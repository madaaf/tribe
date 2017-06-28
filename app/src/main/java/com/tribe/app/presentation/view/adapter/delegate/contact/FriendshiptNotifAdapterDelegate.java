package com.tribe.app.presentation.view.adapter.delegate.contact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public class FriendshiptNotifAdapterDelegate extends RxAdapterDelegate<List<Object>> {

  protected LayoutInflater layoutInflater;
  private Context context;

  // OBSERVABLES
  private PublishSubject<View> clickMore = PublishSubject.create();

  public FriendshiptNotifAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return items.get(position) instanceof Recipient;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    FriendshipNotifViewHolder vh = new FriendshipNotifViewHolder(
        layoutInflater.inflate(R.layout.item_base_list_notif, parent, false));

    vh.btnMore.setOnClickListener(v -> clickMore.onNext(vh.itemView));

    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    FriendshipNotifViewHolder vh = (FriendshipNotifViewHolder) holder;
    Friendship friendship = (Friendship) items.get(position);

    vh.txtName.setText(friendship.getDisplayName());
    vh.txtDescription.setText(friendship.getUsername());
    vh.viewAvatar.setHasShadow(false);
    vh.viewAvatar.load("");
    vh.txtAction.setText(context.getString(R.string.action_friend_added));
    vh.iconAdd.setImageResource(R.drawable.added_icon_bg);
    vh.addBtnBg.setBackground(null);
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {

  }

  public Observable<View> clickMore() {
    return clickMore;
  }

  static class FriendshipNotifViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.txtName) TextViewFont txtName;

    @BindView(R.id.txtUsername) TextViewFont txtDescription;

    @BindView(R.id.btnAdd) View btnAdd;

    @BindView(R.id.viewAvatar) AvatarView viewAvatar;

    @BindView(R.id.btnMore) ImageView btnMore;

    @BindView(R.id.iconAdd) ImageView iconAdd;

    @BindView(R.id.txtAction) TextViewFont txtAction;

    @BindView(R.id.layoutAddFriend) FrameLayout layoutAddFriend;

    @BindView(R.id.AddBtnBg) FrameLayout addBtnBg;

    public FriendshipNotifViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
