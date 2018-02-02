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
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import com.tribe.app.presentation.view.adapter.model.ShareTypeModel;
import com.tribe.app.presentation.view.component.live.TileInviteView;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 09/04/2017
 */
public class ShareAdapterDelegate
    extends RxAdapterDelegate<List<LiveInviteAdapterSectionInterface>> {

  // VARIABLES
  protected LayoutInflater layoutInflater;
  protected Context context;
  private int width = 0;

  // OBSERVABLES
  private PublishSubject<String> onClick = PublishSubject.create();

  public ShareAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<LiveInviteAdapterSectionInterface> items,
      int position) {
    return items.get(position) instanceof ShareTypeModel;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    ShareViewHolder shareViewHolder =
        new ShareViewHolder(layoutInflater.inflate(R.layout.item_invite_share_link, parent, false));
    return shareViewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull List<LiveInviteAdapterSectionInterface> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    ShareViewHolder vh = (ShareViewHolder) holder;
    ShareTypeModel shareTypeModel = (ShareTypeModel) items.get(position);
    vh.viewTile.updateWidth(width);
    vh.viewTile.setPosition(position);
    vh.viewTile.setDrawable(shareTypeModel.getResourceDrawableId());

    vh.itemView.setOnClickListener(v -> onClick.onNext(shareTypeModel.getId()));
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

  static class ShareViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.viewTile) TileInviteView viewTile;

    public ShareViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<String> onClick() {
    return onClick;
  }
}
