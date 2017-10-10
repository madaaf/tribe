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
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import com.tribe.app.presentation.view.component.live.LiveInviteView;
import com.tribe.app.presentation.view.component.live.TileInviteView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by tiago on 09/04/2017
 */
public class ShortcutInviteAdapterDelegate
    extends RxAdapterDelegate<List<LiveInviteAdapterSectionInterface>> {

  @Inject ScreenUtils screenUtils;

  protected LayoutInflater layoutInflater;
  protected Context context;
  private int width = 0;

  public ShortcutInviteAdapterDelegate(Context context) {
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
    ShortcutInviteViewHolder shortcutInviteViewHolder = new ShortcutInviteViewHolder(
        layoutInflater.inflate(R.layout.item_shortcut_invite, parent, false));
    return shortcutInviteViewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull List<LiveInviteAdapterSectionInterface> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    ShortcutInviteViewHolder vh = (ShortcutInviteViewHolder) holder;
    Shortcut shortcut = (Shortcut) items.get(position);
    vh.viewTile.updateWidth(width);
    vh.viewTile.setUser(shortcut.getSingleFriend());
  }

  @Override public void onBindViewHolder(@NonNull List<LiveInviteAdapterSectionInterface> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
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
}
