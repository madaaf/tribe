package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.contact.ContactsHeaderAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.friend.UserListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.friend.UserListEmptyAdapterDelegate;
import com.tribe.app.presentation.view.adapter.filter.UserListFilter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 12/19/16.
 */
public class UserListAdapter extends RecyclerView.Adapter {

    protected RxAdapterDelegatesManager<List<Object>> delegatesManager;
    private UserListAdapterDelegate userListAdapterDelegate;

    private List<Object> items;
    private List<Object> itemsFiltered;
    private boolean hasFilter = false;
    private UserListFilter filter;

    @Inject
    public UserListAdapter(Context context) {
        delegatesManager = new RxAdapterDelegatesManager<>();

        userListAdapterDelegate = new UserListAdapterDelegate(context);
        delegatesManager.addDelegate(userListAdapterDelegate);
        delegatesManager.addDelegate(new ContactsHeaderAdapterDelegate(context));
        delegatesManager.addDelegate(new UserListEmptyAdapterDelegate(context));

        items = new ArrayList<>();
        itemsFiltered = new ArrayList<>();
        filter = new UserListFilter(items, this);

        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(itemsFiltered, position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        delegatesManager.onBindViewHolder(itemsFiltered, position, holder);
    }

    @Override
    public int getItemCount() {
        return itemsFiltered.size();
    }

    @Override
    public long getItemId(int position) {
        Object obj = getItemAtPosition(position);
        if (obj instanceof User) return ((User) obj).hashCode();
        return obj.hashCode();
    }

    public void releaseSubscriptions() {
        delegatesManager.releaseSubscriptions();
    }

    public void setItems(List<Object> items) {
        hasFilter = false;
        this.items.clear();
        computeHeaders(items, this.items);
        this.items.addAll(items);

        if (!hasFilter) {
            this.itemsFiltered.clear();
            this.itemsFiltered.addAll(this.items);
        }

        this.notifyDataSetChanged();
    }

    public Object getItemAtPosition(int position) {
        if (itemsFiltered.size() > 0 && position < itemsFiltered.size()) {
            return itemsFiltered.get(position);
        } else {
            return null;
        }
    }

    public List<Object> getItems() {
        return itemsFiltered;
    }

    public void setFilteredItems(List<Object> items) {
        hasFilter = true;
        this.itemsFiltered.clear();
        computeHeaders(items, this.itemsFiltered);
        this.itemsFiltered.addAll(items);
    }

    public void filterList(String text) {
        if (!StringUtils.isEmpty(text)) {
            filter.filter(text);
        } else {
            this.itemsFiltered.clear();
            this.itemsFiltered.addAll(this.items);
            notifyDataSetChanged();
        }
    }

    public void updateUser(User user) {
        if (itemsFiltered != null && itemsFiltered.size() > 0) {
            int position = -1;
            for (Object o : itemsFiltered) {
                if (o instanceof User) {
                    User userB = (User) o;
                    if (userB.getId().equals(user.getId())) {
                        userB.setNewFriend(user.isNewFriend());
                        position = itemsFiltered.indexOf(userB);
                    }
                }
            }

            if (position != -1) notifyItemChanged(position);
        }
    }

    private void computeHeaders(List<Object> from, List<Object> to) {
        to.add(from == null || from.size() == 0 ? R.string.search_no_friends_to_add : R.string.search_add_friends);
        if (from == null || from.size() == 0) {
            User user = new User(User.ID_EMPTY);
            to.add(user);
        }
    }

    // OBSERVABLES
    public Observable<View> clickAdd() {
        return userListAdapterDelegate.clickAdd();
    }
}
