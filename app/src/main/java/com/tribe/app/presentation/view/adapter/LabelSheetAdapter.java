package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.presentation.view.adapter.delegate.tribe.LabelSheetAdapterDelegate;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 18/05/2016.
 */
public class LabelSheetAdapter extends RecyclerView.Adapter {

  private RxAdapterDelegatesManager<List<LabelType>> delegatesManager;
  private List<LabelType> items;
  private LabelSheetAdapterDelegate labelSheetAdapterDelegate;

  @Inject public LabelSheetAdapter(Context context, List<LabelType> labelTypeList) {
    delegatesManager = new RxAdapterDelegatesManager<>();
    labelSheetAdapterDelegate = new LabelSheetAdapterDelegate(context);
    delegatesManager.addDelegate(labelSheetAdapterDelegate);
    items = new ArrayList<>(labelTypeList);
  }

  @Override public int getItemViewType(int position) {
    return delegatesManager.getItemViewType(items, position);
  }

  @Override public long getItemId(int position) {
    LabelType labelType = getItemAtPosition(position);
    return labelType.hashCode();
  }

  @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return delegatesManager.onCreateViewHolder(parent, viewType);
  }

  @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    delegatesManager.onBindViewHolder(items, position, holder);
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public LabelType getItemAtPosition(int position) {
    if (items.size() > 0 && position < items.size()) {
      return items.get(position);
    } else {
      return null;
    }
  }

  public void releaseSubscriptions() {
    delegatesManager.releaseSubscriptions();
  }

  public void setItems(List<LabelType> labelTypeList) {
    items.clear();
    items.addAll(labelTypeList);
    notifyDataSetChanged();
  }

  public Observable<View> clickLabelItem() {
    return labelSheetAdapterDelegate.clickLabelItem();
  }
}
