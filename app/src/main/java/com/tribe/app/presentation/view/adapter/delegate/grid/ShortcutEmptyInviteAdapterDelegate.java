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
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import com.tribe.app.presentation.view.component.live.TileInviteView;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 10/05/2017
 */
public class ShortcutEmptyInviteAdapterDelegate
    extends RxAdapterDelegate<List<LiveInviteAdapterSectionInterface>> {

  protected LayoutInflater layoutInflater;
  protected Context context;
  protected int width;

  // OBSERVABLES
  private PublishSubject<View> onClick = PublishSubject.create();

  public ShortcutEmptyInviteAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<LiveInviteAdapterSectionInterface> items,
      int position) {
    if (items.get(position) instanceof Shortcut) {
      return items.get(position).getId().equals(Recipient.ID_EMPTY);
    }

    return false;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    ShortcutInviteViewHolder shortcutInviteViewHolder = new ShortcutInviteViewHolder(
        layoutInflater.inflate(R.layout.item_empty_shortcut_invite, parent, false));
    shortcutInviteViewHolder.viewTile.initClicks();
    subscriptions.add(shortcutInviteViewHolder.viewTile.onClick().map(v -> shortcutInviteViewHolder.itemView).subscribe(onClick));
    return shortcutInviteViewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull List<LiveInviteAdapterSectionInterface> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    ShortcutInviteViewHolder vh = (ShortcutInviteViewHolder) holder;
    vh.viewTile.updateWidth(width);
    vh.viewTile.setPosition(position);
    vh.viewTile.setUser(null);
  }

  @Override public void onBindViewHolder(@NonNull List<LiveInviteAdapterSectionInterface> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    onBindViewHolder(items, position, holder);
  }

  /**
   * PUBLIC
   */

  public void updateWidth(int width) {
    this.width = width;
  }

  static class ShortcutInviteViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.viewTile) TileInviteView viewTile;

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
