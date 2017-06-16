package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import java.util.List;

/**
 * Created by tiago on 18/05/2016.
 */
public class EmptyGridAdapterDelegate extends RxAdapterDelegate<List<Recipient>> {

  private LayoutInflater layoutInflater;
  private Context context;
  private ScreenUtils screenUtils;
  private boolean shouldRoundCorners = false;
  private boolean isInviteLive;

  public EmptyGridAdapterDelegate(Context context, boolean shouldRoundCorners,
      boolean isInviteLive) {
    this.context = context;
    this.isInviteLive = isInviteLive;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.screenUtils =
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent()
            .screenUtils();
    this.shouldRoundCorners = shouldRoundCorners;
  }

  @Override public boolean isForViewType(@NonNull List<Recipient> items, int position) {
    return items.get(position).getSubId().equals(Recipient.ID_EMPTY);//SOEF
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    return new EmptyGridViewHolder(layoutInflater.inflate(R.layout.item_empty_grid, parent, false));
  }

  @Override public void onBindViewHolder(@NonNull List<Recipient> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    EmptyGridViewHolder vh = (EmptyGridViewHolder) holder;
    if (isInviteLive) {
      setPlaceHolderColor(items, holder, position);
    } else {
      UIUtils.setBackgroundGrid(screenUtils, vh.layoutContent, position, shouldRoundCorners);
    }
  }

  @Override public void onBindViewHolder(@NonNull List<Recipient> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    EmptyGridViewHolder vh = (EmptyGridViewHolder) holder;
    if (isInviteLive) {
      setPlaceHolderColor(items, holder, position);
    } else {
      UIUtils.setBackgroundGrid(screenUtils, vh.layoutContent, position, shouldRoundCorners);
    }
  }

  private void setPlaceHolderColor(List<Recipient> items, RecyclerView.ViewHolder holder,
      int position) {
    if ((position % 2) == 0) {
      holder.itemView.setBackgroundColor(
          ContextCompat.getColor(context, R.color.black_dark_placeholder));
    } else {
      holder.itemView.setBackgroundColor(
          ContextCompat.getColor(context, R.color.black_light_placeholder));
    }
  }

  static class EmptyGridViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.layoutContent) public ViewGroup layoutContent;

    public EmptyGridViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
