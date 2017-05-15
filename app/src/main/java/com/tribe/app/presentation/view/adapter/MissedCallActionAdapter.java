package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.presentation.view.adapter.delegate.friend.MissedCallActionDelegate;
import com.tribe.app.presentation.view.notification.MissedCallAction;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;

/**
 * Created by madaaflak on 28/04/2017.
 */

public class MissedCallActionAdapter extends RecyclerView.Adapter {

  protected RxAdapterDelegatesManager<List<Object>> delegatesManager;
  private MissedCallActionDelegate missedCallActionDelegate;

  private List<Object> items;
  private Context context;

  public MissedCallActionAdapter(Context context) {
    this.context = context;

    delegatesManager = new RxAdapterDelegatesManager<>();
    missedCallActionDelegate = new MissedCallActionDelegate(context);

    delegatesManager.addDelegate(missedCallActionDelegate);

    items = new ArrayList<>();
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

  public Object getItemAtPosition(int position) {
    if (items.size() > 0 && position < items.size()) {
      return items.get(position);
    } else {
      return null;
    }
  }

  public void setItems(List<MissedCallAction> items) {
    this.items.clear();
    this.items.addAll(items);
    this.notifyDataSetChanged();
  }

  // OBSERVABLES

  public Observable<View> onHangLive() {
    return missedCallActionDelegate.onHangLive();
  }
}
