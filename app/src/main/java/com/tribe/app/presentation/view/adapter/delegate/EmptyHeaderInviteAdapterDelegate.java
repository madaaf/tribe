package com.tribe.app.presentation.view.adapter.delegate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.List;

/**
 * Created by tiago on 09/20/2017.
 */
public class EmptyHeaderInviteAdapterDelegate
    extends RxAdapterDelegate<List<LiveInviteAdapterSectionInterface>> {

  private LayoutInflater layoutInflater;
  private Context context;
  private ScreenUtils screenUtils;

  public EmptyHeaderInviteAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.screenUtils =
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent()
            .screenUtils();
  }

  @Override public boolean isForViewType(@NonNull List<LiveInviteAdapterSectionInterface> items,
      int position) {
    return items.get(position).getId().equals(Recipient.ID_HEADER);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    return new EmptyHeaderGridViewHolder(
        layoutInflater.inflate(R.layout.item_empty_header_grid, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull List<LiveInviteAdapterSectionInterface> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
  }

  @Override public void onBindViewHolder(@NonNull List<LiveInviteAdapterSectionInterface> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

  }

  static class EmptyHeaderGridViewHolder extends RecyclerView.ViewHolder {

    public EmptyHeaderGridViewHolder(View itemView) {
      super(itemView);
    }
  }
}
