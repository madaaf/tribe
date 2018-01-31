package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import com.tribe.app.presentation.view.adapter.model.HeaderModel;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;

/**
 * Created by tiago on 10/05/2017
 */
public class LiveInviteSubHeaderAdapterDelegate
    extends RxAdapterDelegate<List<LiveInviteAdapterSectionInterface>> {

  protected LayoutInflater layoutInflater;
  protected Context context;

  public LiveInviteSubHeaderAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @Override public boolean isForViewType(@NonNull List<LiveInviteAdapterSectionInterface> items,
      int position) {
    if (items.get(position) instanceof HeaderModel) {
      return items.get(position).getId().equals(HeaderModel.HEADER_ONLINE) ||
          items.get(position).getId().equals(HeaderModel.HEADER_RECENT);
    }

    return false;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    LiveInviteHeaderViewHolder liveInviteViewHolder = new LiveInviteHeaderViewHolder(
        layoutInflater.inflate(R.layout.item_live_invite_sub_header, parent, false));
    return liveInviteViewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull List<LiveInviteAdapterSectionInterface> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    HeaderModel header = (HeaderModel) items.get(position);
    LiveInviteHeaderViewHolder vh = (LiveInviteHeaderViewHolder) holder;

    if (header.getId().equals(HeaderModel.HEADER_RECENT)) {
      TextViewCompat.setTextAppearance(vh.txtLabel, R.style.Body_Two_BlackRecent);
    } else {
      TextViewCompat.setTextAppearance(vh.txtLabel, R.style.Body_Two_BlueNew);
    }

    vh.txtLabel.setCustomFont(context, FontUtils.PROXIMA_BOLD);

    vh.imgPicto.setImageResource(header.getResourceDrawableId());
    vh.txtLabel.setText(header.getResourceTxtId());
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
}
