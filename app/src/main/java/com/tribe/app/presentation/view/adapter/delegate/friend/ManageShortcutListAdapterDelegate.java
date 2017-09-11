package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 05/11/2017.
 */
public class ManageShortcutListAdapterDelegate extends RxAdapterDelegate<List<Shortcut>> {

  protected LayoutInflater layoutInflater;
  protected Context context;

  // RX SUBSCRIPTIONS / SUBJECTS
  private PublishSubject<View> onClickMute = PublishSubject.create();
  private PublishSubject<View> onClickRemove = PublishSubject.create();

  public ManageShortcutListAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<Shortcut> items, int position) {
    return items.get(position) instanceof Shortcut;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    ManageFriendshipViewHolder manageFriendshipViewHolder = new ManageFriendshipViewHolder(
        layoutInflater.inflate(R.layout.item_manage_friendship, parent, false));

    return manageFriendshipViewHolder;
  }

  @Override public void onBindViewHolder(@NonNull List<Shortcut> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    ManageFriendshipViewHolder vh = (ManageFriendshipViewHolder) holder;
    //Friendship fr = items.get(position);
    //vh.viewAvatar.setType(
    //    fr.isLive() ? AvatarView.LIVE : (fr.isOnline() ? AvatarView.ONLINE : AvatarView.REGULAR));
    //vh.viewAvatar.load(fr);
    //vh.txtName.setText(fr.getDisplayName());
    //vh.txtUsername.setText(StringUtils.isEmpty(fr.getUsername()) ? "" : fr.getUsername());
    //vh.switchMute.setChecked(fr.isMute());

    vh.switchMute.setOnClickListener(v -> onClickMute.onNext(vh.itemView));

    vh.btnRemove.setOnClickListener(v -> onClickRemove.onNext(vh.itemView));
  }

  @Override public void onBindViewHolder(@NonNull List<Shortcut> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

  }

  public Observable<View> onClickMute() {
    return onClickMute;
  }

  public Observable<View> onClickRemove() {
    return onClickRemove;
  }

  static class ManageFriendshipViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.viewAvatar) public AvatarView viewAvatar;

    @BindView(R.id.txtName) public TextViewFont txtName;

    @BindView(R.id.txtUsername) public TextViewFont txtUsername;

    @BindView(R.id.switchMute) public SwitchCompat switchMute;

    @BindView(R.id.btnRemove) public View btnRemove;

    public ManageFriendshipViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
