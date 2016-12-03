package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.delegate.contact.ButtonPointsAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.ContactsGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.ContactsHeaderAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.SearchResultGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.SeparatorAdapterDelegate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2016.
 */
public class ContactAdapter extends RecyclerView.Adapter {

    // DELEGATES
    protected RxAdapterDelegatesManager delegatesManager;
    private ButtonPointsAdapterDelegate buttonPointsAdapterDelegate;
    private SearchResultGridAdapterDelegate searchResultGridAdapterDelegate;
    private ContactsGridAdapterDelegate contactsGridAdapterDelegate;

    // VARIABLES
    private List<Object> items;
    private User currentUser;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public ContactAdapter(Context context, User currentUser) {
        items = new ArrayList<>();

        this.currentUser = currentUser;

        delegatesManager = new RxAdapterDelegatesManager();

        buttonPointsAdapterDelegate = new ButtonPointsAdapterDelegate(context);
        delegatesManager.addDelegate(buttonPointsAdapterDelegate);

        searchResultGridAdapterDelegate = new SearchResultGridAdapterDelegate(context);
        delegatesManager.addDelegate(searchResultGridAdapterDelegate);

        contactsGridAdapterDelegate = new ContactsGridAdapterDelegate(context);
        delegatesManager.addDelegate(contactsGridAdapterDelegate);
        delegatesManager.addDelegate(new SeparatorAdapterDelegate(context));
        delegatesManager.addDelegate(new ContactsHeaderAdapterDelegate(context));

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        Object obj = getItemAtPosition(position);
        return obj.hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        delegatesManager.onBindViewHolder(items, position, holder);
    }

    public void releaseSubscriptions() {
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        delegatesManager.releaseSubscriptions();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public Object getItemAtPosition(int position) {
        if (items.size() > 0 && position < items.size()) {
            return items.get(position);
        } else {
            return null;
        }
    }

    public List<Object> getItems() {
        return items;
    }

    public void updateSearch(SearchResult searchResult, List<Contact> contactList) {
        this.items.clear();
        this.items.add(R.string.search_usernames);
        this.items.add(searchResult);
        this.items.add(new String());
        if (contactList != null && contactList.size() > 0) {
            this.items.add(R.string.contacts_section_search_friends);
            this.items.addAll(contactList);
        }
        this.notifyDataSetChanged();
    }

    // OBSERVABLES
    public Observable<View> onClickAdd() {
        return searchResultGridAdapterDelegate.clickAdd();
    }
}
