package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.presentation.view.adapter.delegate.contact.ContactsHeaderAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.friend.MemberListAdapterDelegate;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;

/**
 * Created by tiago on 11/29/16.
 */
public class MemberListAdapter extends RecyclerView.Adapter {

  protected RxAdapterDelegatesManager<List<Object>> delegatesManager;
  private MemberListAdapterDelegate memberListAdapterDelegate;

  private List<Object> items;
  private Context context;

  public MemberListAdapter(Context context) {
    this.context = context;

    delegatesManager = new RxAdapterDelegatesManager<>();

    memberListAdapterDelegate = new MemberListAdapterDelegate(context);
    delegatesManager.addDelegate(memberListAdapterDelegate);

    delegatesManager.addDelegate(new ContactsHeaderAdapterDelegate(context));

    items = new ArrayList<>();

    setHasStableIds(true);
  }

  @Override public int getItemViewType(int position) {
    return delegatesManager.getItemViewType(items, position);
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

  @Override public long getItemId(int position) {
    if (getItemAtPosition(position) instanceof GroupMember) {
      GroupMember obj = (GroupMember) getItemAtPosition(position);
      return obj.hashCode();
    } else {
      return position;
    }
  }

  public void releaseSubscriptions() {
    delegatesManager.releaseSubscriptions();
  }

  public void setItems(List<GroupMember> items) {
    this.items.clear();
    this.items.add(refactorMembers(items));
    this.items.addAll(items);
    this.notifyDataSetChanged();
  }

  public Object getItemAtPosition(int position) {
    if (items.size() > 0 && position < items.size()) {
      return items.get(position);
    } else {
      return null;
    }
  }

  public List<GroupMember> getItems() {
    List<GroupMember> members = new ArrayList<>();
    for (Object obj : items) {
      if (obj instanceof GroupMember) members.add((GroupMember) obj);
    }
    return members;
  }

  private String refactorMembers(List<GroupMember> items) {
    return items.size() + " " + (items.size() > 1 ? context.getResources()
        .getString(R.string.group_members)
        : context.getResources().getString(R.string.group_member));
  }

  // OBSERVABLES

  public Observable<View> clickAdd() {
    return memberListAdapterDelegate.clickAdd();
  }

  public Observable<View> longClick() {
    return memberListAdapterDelegate.onLongClick();
  }

  public Observable<View> onHangLive() {
    return memberListAdapterDelegate.onHangLive();
  }
}
