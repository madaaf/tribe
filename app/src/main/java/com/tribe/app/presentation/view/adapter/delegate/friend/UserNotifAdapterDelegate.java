package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.delegate.base.BaseNotifAdapterDelegate;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 11/29/16.
 */
public class UserNotifAdapterDelegate extends BaseNotifAdapterDelegate {

  // OBSERVABLES
  //private PublishSubject<View> onClickAdd = PublishSubject.create();
  private PublishSubject<View> clickMore = PublishSubject.create();

  public UserNotifAdapterDelegate(Context context) {
    super(context);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    if (items.get(position) instanceof User) {
      User user = (User) items.get(position);
      return !user.getId().equals(User.ID_EMPTY);
    } else {
      return false;
    }
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    return onCreateViewHolderForUnfriend(parent);
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    onBindViewHolderForUnfriend(items, position, holder);
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {

  }
  /*

  public Observable<View> onClickAdd() {
    return onClickAdd;
  }
  */

  public Observable<View> onClickAdd() {
    return onClickAdd;
  }

  public Observable<View> clickMore() {
    return clickMore;
  }
}
