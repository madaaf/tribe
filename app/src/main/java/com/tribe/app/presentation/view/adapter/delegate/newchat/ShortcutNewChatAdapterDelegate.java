package com.tribe.app.presentation.view.adapter.delegate.newchat;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 09/04/2017
 */
public class ShortcutNewChatAdapterDelegate
    extends RxAdapterDelegate<List<LiveInviteAdapterSectionInterface>> {

  @Inject ScreenUtils screenUtils;

  protected LayoutInflater layoutInflater;
  protected Context context;
  private int width = 0;

  private PublishSubject<View> onClick = PublishSubject.create();

  public ShortcutNewChatAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<LiveInviteAdapterSectionInterface> items,
      int position) {
    return items.get(position) instanceof Shortcut &&
        !items.get(position).getId().equals(Shortcut.ID_EMPTY);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    ShortcutInviteViewHolder shortcutInviteViewHolder =
        new ShortcutInviteViewHolder(layoutInflater.inflate(R.layout.item_shortcut, parent, false));
    shortcutInviteViewHolder.itemView.setOnClickListener(
        view -> onClick.onNext(shortcutInviteViewHolder.itemView));
    return shortcutInviteViewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull List<LiveInviteAdapterSectionInterface> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    ShortcutInviteViewHolder vh = (ShortcutInviteViewHolder) holder;
    Shortcut shortcut = (Shortcut) items.get(position);
    bind(vh, shortcut);
  }

  @Override public void onBindViewHolder(@NonNull List<LiveInviteAdapterSectionInterface> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    ShortcutInviteViewHolder vh = (ShortcutInviteViewHolder) holder;
    Shortcut shortcut = (Shortcut) items.get(position);
    bind(vh, shortcut);
  }

  private void bind(ShortcutInviteViewHolder vh, Shortcut shortcut) {
    User user = shortcut.getSingleFriend();
    vh.viewNewAvatar.load(shortcut);
    vh.txtName.setText(user.getDisplayName());

    if (shortcut.isSelected()) {
      vh.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_background_profile_info));
      vh.imgSelected.setVisibility(View.VISIBLE);
    } else {
      vh.itemView.setBackgroundColor(Color.WHITE);
      vh.imgSelected.setVisibility(View.GONE);
    }
  }

  static class ShortcutInviteViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.imgSelected) ImageView imgSelected;

    @BindView(R.id.viewNewAvatar) NewAvatarView viewNewAvatar;

    @BindView(R.id.txtName) TextViewFont txtName;

    public ShortcutInviteViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<View> onClick() {
    return onClick;
  }
}
