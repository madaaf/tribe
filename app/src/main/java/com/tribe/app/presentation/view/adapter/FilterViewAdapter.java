package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.FilterEntity;
import com.tribe.app.presentation.view.adapter.delegate.filterview.IconAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.filterview.LetterAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2016.
 */
public class FilterViewAdapter extends RecyclerView.Adapter {

  // DELEGATES
  private RxAdapterDelegatesManager<List<FilterEntity>> delegatesManager;
  private LetterAdapterDelegate letterAdapterDelegate;

  private List<FilterEntity> items;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Inject public FilterViewAdapter(Context context) {
    delegatesManager = new RxAdapterDelegatesManager<>();
    delegatesManager.addDelegate(new IconAdapterDelegate(context));

    letterAdapterDelegate = new LetterAdapterDelegate(context);
    delegatesManager.addDelegate(letterAdapterDelegate);

    items = new ArrayList<>();

    setHasStableIds(true);
  }

  @Override public int getItemViewType(int position) {
    return delegatesManager.getItemViewType(items, position);
  }

  @Override public long getItemId(int position) {
    FilterEntity filterEntity = getPTSEntity(position);
    return filterEntity.hashCode();
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

  public void releaseSubscriptions() {
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    delegatesManager.releaseSubscriptions();
  }

  public void setItems(List<FilterEntity> filterEntityList) {
    items.clear();
    items.addAll(filterEntityList);
    notifyDataSetChanged();
  }

  public FilterEntity getPTSEntity(int position) {
    return items.get(position);
  }

  ///////////////////////
  //    OBSERVABLES    //
  ///////////////////////
  public Observable<TextViewFont> onClickLetter() {
    return letterAdapterDelegate.onClickLetter();
  }
}
