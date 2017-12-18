package com.tribe.app.presentation.view.adapter.delegate.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.model.LoadMoreModel;
import java.util.List;

/**
 * Created by tiago on 12/08/17.
 */
public class LoadMoreAdapterDelegate extends RxAdapterDelegate<List<Object>> {

  // RX SUBSCRIPTIONS / SUBJECTS
  // VARIABLES
  protected Context context;
  protected LayoutInflater layoutInflater;

  public LoadMoreAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return items.get(position) instanceof LoadMoreModel;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    final LoadMoreViewHolder vh =
        new LoadMoreViewHolder(layoutInflater.inflate(R.layout.item_progress_bar, parent, false));
    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
    onBindViewHolder(items, position, holder);
  }

  static class LoadMoreViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.progressView) CircularProgressView circularProgressView;

    public LoadMoreViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
