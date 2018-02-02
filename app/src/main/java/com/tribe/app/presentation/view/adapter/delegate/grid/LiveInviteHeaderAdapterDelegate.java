package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
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
import com.tribe.app.presentation.view.adapter.model.HeaderModel;
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
  private int margin;

  // OBSERVABLES
  protected PublishSubject<View> onClickEdit = PublishSubject.create();

  public LiveInviteHeaderAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    this.margin = context.getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
  }

  @Override public boolean isForViewType(@NonNull List<LiveInviteAdapterSectionInterface> items,
      int position) {
    if (items.get(position) instanceof HeaderModel) {
      return !items.get(position).getId().equals(HeaderModel.HEADER_ONLINE) &&
          !items.get(position).getId().equals(HeaderModel.HEADER_RECENT);
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
    HeaderModel header = (HeaderModel) items.get(position);
    LiveInviteHeaderViewHolder vh = (LiveInviteHeaderViewHolder) holder;
    vh.imgPicto.setImageResource(header.getResourceDrawableId());
    vh.txtLabel.setText(header.getResourceTxtId());
    vh.txtLabel.setGravity(header.getGravity());

    if (header.getGravity() != Gravity.CENTER) {
      ViewGroup.MarginLayoutParams params =
          (ViewGroup.MarginLayoutParams) vh.txtLabel.getLayoutParams();
      params.setMarginStart(margin);
      vh.txtLabel.setLayoutParams(params);
    } else {
      ViewGroup.MarginLayoutParams params =
          (ViewGroup.MarginLayoutParams) vh.txtLabel.getLayoutParams();
      params.setMarginStart(0);
      vh.txtLabel.setLayoutParams(params);
    }

    if (header.getId().equals(HeaderModel.HEADER_NAME)) {
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
