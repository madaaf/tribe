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
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.List;

/**
 * Created by tiago on 18/05/2016.
 */
public class ShortcutEmptyListAdapterDelegate extends RxAdapterDelegate<List<Recipient>> {

  private LayoutInflater layoutInflater;
  private Context context;
  private ScreenUtils screenUtils;

  public ShortcutEmptyListAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.screenUtils =
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent()
            .screenUtils();
  }

  @Override public boolean isForViewType(@NonNull List<Recipient> items, int position) {
    return items.get(position).getId().equals(Recipient.ID_EMPTY);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    return new EmptyGridViewHolder(layoutInflater.inflate(R.layout.item_list_empty, parent, false));
  }

  @Override public void onBindViewHolder(@NonNull List<Recipient> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
  }

  @Override public void onBindViewHolder(@NonNull List<Recipient> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
  }

  static class EmptyGridViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.layoutContent) public ViewGroup layoutContent;

    public EmptyGridViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
