package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.ButtonPoints;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.delegate.contact.ButtonPointsAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.ContactsGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.SeparatorAdapterDelegate;
import com.tribe.app.presentation.view.utils.ScoreUtils;
import com.tribe.app.presentation.view.widget.ButtonPointsView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2016.
 */
public class ContactsGridAdapter extends RecyclerView.Adapter {

    // DELEGATES
    protected RxAdapterDelegatesManager delegatesManager;

    // VARIABLES
    private List<Object> items;
    private User currentUser;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public ContactsGridAdapter(Context context, User currentUser) {
        items = new ArrayList<>();

        this.currentUser = currentUser;

        delegatesManager = new RxAdapterDelegatesManager();
        delegatesManager.addDelegate(new ContactsGridAdapterDelegate(context));
        delegatesManager.addDelegate(new ButtonPointsAdapterDelegate(context));
        delegatesManager.addDelegate(new SeparatorAdapterDelegate(context));

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

    public void setItems(List<Contact> items) {
        this.items.clear();
        this.items.addAll(items);

        ButtonPoints shareProfile = new ButtonPoints(ButtonPointsView.PROFILE, R.string.contacts_share_profile_button,
                R.string.contacts_share_profile_description, ScoreUtils.Point.SHARE_PROFILE.getPoints());
        shareProfile.setUrlImg(currentUser.getProfilePicture());

        ButtonPoints syncFB = new ButtonPoints(ButtonPointsView.FB_ACTIVE, R.string.contacts_section_facebook_sync_title,
                R.string.contacts_section_facebook_sync_description, ScoreUtils.Point.INVITE_FACEBOOK.getPoints());
        syncFB.setDrawable(R.drawable.picto_facebook_logo);

        this.items.add(new String());
        this.items.add(shareProfile);
        this.items.add(new String());
        this.items.add(syncFB);
        this.items.add(new String());

        this.notifyDataSetChanged();
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
}