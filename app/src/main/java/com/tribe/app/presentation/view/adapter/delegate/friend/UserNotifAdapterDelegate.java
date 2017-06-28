package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 11/29/16.
 */
public class UserNotifAdapterDelegate extends RxAdapterDelegate<List<Object>> {

  protected LayoutInflater layoutInflater;
  private Context context;
  private Animation anim;

  // OBSERVABLES
  private PublishSubject<View> onClickAdd = PublishSubject.create();
  private PublishSubject<View> clickMore = PublishSubject.create();

  public UserNotifAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    anim = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.rotate90);
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
    UserNotifViewHolder vh = new UserNotifViewHolder(
        layoutInflater.inflate(R.layout.item_base_list_notif, parent, false));

    vh.btnMore.setOnClickListener(v -> clickMore.onNext(vh.itemView));
    vh.layoutAddFriend.setOnClickListener(v -> onClickAdd.onNext(vh.itemView));

    // vh.btnAdd.setOnClickListener(v -> onClickAdd.onNext(vh.itemView));
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    UserNotifViewHolder vh = (UserNotifViewHolder) holder;
    User user = (User) items.get(position);

    vh.txtName.setText(user.getDisplayName());
    vh.txtDescription.setText(user.getUsername());
    vh.viewAvatar.setHasShadow(false);
    vh.viewAvatar.load("");
    vh.txtAction.setText(context.getString(R.string.action_add_friend));
    vh.iconAdd.setImageResource(R.drawable.add_icon_bg);
    vh.iconAdd.startAnimation(anim);
   /* vh.btnMore.setOnClickListener(v -> clickMore.onNext(vh.itemView));
    vh.iconAdd.startAnimation(anim);*/
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {

  }

  static class UserNotifViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.txtName) TextViewFont txtName;

    @BindView(R.id.txtUsername) TextViewFont txtDescription;

    @BindView(R.id.btnAdd) View btnAdd;

    @BindView(R.id.viewAvatar) AvatarView viewAvatar;

    @BindView(R.id.btnMore) ImageView btnMore;

    @BindView(R.id.iconAdd) ImageView iconAdd;

    @BindView(R.id.txtAction) TextViewFont txtAction;

    @BindView(R.id.layoutAddFriend) FrameLayout layoutAddFriend;

    @BindView(R.id.AddBtnBg) FrameLayout addBtnBg;

    public UserNotifViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<View> onClickAdd() {
    return onClickAdd;
  }

  public Observable<View> clickMore() {
    return clickMore;
  }
}
