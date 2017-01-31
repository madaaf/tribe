package com.tribe.app.presentation.view.adapter.delegate.tribe;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public class LabelSheetAdapterDelegate extends RxAdapterDelegate<List<LabelType>> {

  protected LayoutInflater layoutInflater;

  // RX SUBSCRIPTIONS / SUBJECTS
  private final PublishSubject<View> clickLabelItem = PublishSubject.create();

  public LabelSheetAdapterDelegate(Context context) {
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public boolean isForViewType(@NonNull List<LabelType> items, int position) {
    return true;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    LabelSheetViewHolder vh = new LabelSheetViewHolder(
        layoutInflater.inflate(R.layout.item_sheet_pending_tribe, parent, false));

    subscriptions.add(RxView.clicks(vh.txtView)
        .takeUntil(RxView.detaches(parent))
        .map(view -> vh.txtView)
        .subscribe(clickLabelItem));

    return vh;
  }

  @Override public void onBindViewHolder(@NonNull List<LabelType> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    LabelSheetViewHolder vh = (LabelSheetViewHolder) holder;
    LabelType labelType = items.get(position);

    vh.txtView.setText(labelType.getLabel());
    vh.txtView.setTag(R.id.tag_position, position);
  }

  @Override public void onBindViewHolder(@NonNull List<LabelType> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

  }

  public Observable<View> clickLabelItem() {
    return clickLabelItem;
  }

  static class LabelSheetViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.txtView) public TextViewFont txtView;

    public LabelSheetViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
