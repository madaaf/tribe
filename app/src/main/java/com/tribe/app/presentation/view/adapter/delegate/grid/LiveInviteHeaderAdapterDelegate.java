package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import com.tribe.app.presentation.view.adapter.model.Header;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 10/05/2017
 */
public class LiveInviteHeaderAdapterDelegate
    extends RxAdapterDelegate<List<LiveInviteAdapterSectionInterface>> {

  protected LayoutInflater layoutInflater;
  protected Context context;

  // OBSERVABLES
  protected PublishSubject<View> onClickEdit = PublishSubject.create();

  public LiveInviteHeaderAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<LiveInviteAdapterSectionInterface> items,
      int position) {
    if (items.get(position) instanceof Header) {
      return !items.get(position).getId().equals(Header.HEADER_ONLINE) &&
          !items.get(position).getId().equals(Header.HEADER_RECENT);
    }

    return false;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    LiveInviteHeaderViewHolder liveInviteViewHolder = new LiveInviteHeaderViewHolder(
        layoutInflater.inflate(R.layout.item_live_invite_header, parent, false));
    return liveInviteViewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull List<LiveInviteAdapterSectionInterface> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    Header header = (Header) items.get(position);
    LiveInviteHeaderViewHolder vh = (LiveInviteHeaderViewHolder) holder;
    vh.imgPicto.setImageResource(header.getResourceDrawableId());
    vh.txtLabel.setText(header.getResourceTxtId());
    if (header.getId().equals(Header.HEADER_NAME)) {
      vh.imgPicto.setOnClickListener(v -> onClickEdit.onNext(vh.itemView));
    }
  }

  @Override public void onBindViewHolder(@NonNull List<LiveInviteAdapterSectionInterface> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
  }

  /**
   * PUBLIC
   */

  static class LiveInviteHeaderViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.imgPicto) ImageView imgPicto;

    @BindView(R.id.txtLabel) TextViewFont txtLabel;

    public LiveInviteHeaderViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<View> onClickEdit() {
    return onClickEdit;
  }
}
