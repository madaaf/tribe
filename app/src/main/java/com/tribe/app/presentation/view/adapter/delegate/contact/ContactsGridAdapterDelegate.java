package com.tribe.app.presentation.view.adapter.delegate.contact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public class ContactsGridAdapterDelegate extends RxAdapterDelegate<List<Object>> {

    protected LayoutInflater layoutInflater;
    private Context context;

    // OBSERVABLES
    private PublishSubject<View> onClickInvite = PublishSubject.create();

    public ContactsGridAdapterDelegate(Context context) {
        this.context = context;
        this.layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean isForViewType(@NonNull List<Object> items, int position) {
        if (items.get(position) instanceof Contact) {
            Contact contact = (Contact) items.get(position);
            return (contact.getUserList() == null || contact.getUserList().size() == 0);
        }

        return false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        ContactViewHolder vh =
                new ContactViewHolder(layoutInflater.inflate(R.layout.item_contact, parent, false));

        vh.btnAdd.setOnClickListener(v -> onClickInvite.onNext(vh.itemView));

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Object> items, int position,
                                 @NonNull RecyclerView.ViewHolder holder) {
        ContactViewHolder vh = (ContactViewHolder) holder;
        Contact contact = (Contact) items.get(position);

        vh.txtName.setText(contact.getName());
        vh.txtDescription.setText(
                context.getString(R.string.contacts_section_addressbook_friends_in_app,
                        contact.getHowManyFriends()));
    }

    @Override
    public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
                                 int position, List<Object> payloads) {

    }

    public Observable<View> onClickInvite() {
        return onClickInvite;
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtName)
        TextViewFont txtName;

        @BindView(R.id.txtDescription)
        TextViewFont txtDescription;

        @BindView(R.id.btnAdd)
        View btnAdd;

        public ContactViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
