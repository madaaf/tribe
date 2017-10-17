package com.tribe.app.presentation.view.adapter.delegate.contact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.ContactAB;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.ContactToInviteViewHolder;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 01/02/2017.
 */
public class ContactToInviteAdapterDelegate extends RxAdapterDelegate<List<Object>> {

  private Context context;
  private LayoutInflater layoutInflater;

  // OBSERVABLES
  private PublishSubject<View> onInvite = PublishSubject.create();

  public ContactToInviteAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    ContactToInviteViewHolder vh = new ContactToInviteViewHolder(
        layoutInflater.inflate(R.layout.item_contact_to_invite, parent, false));
    vh.btnInvite.setOnClickListener(view -> onInvite.onNext(vh.itemView));
    return vh;
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return (items.get(position) instanceof Contact);
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
    bind((ContactToInviteViewHolder) holder, (Contact) items.get(position));
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    bind((ContactToInviteViewHolder) holder, (Contact) items.get(position));
  }

  public void bind(ContactToInviteViewHolder vh, Contact contact) {
    vh.txtName.setText(contact.getName());
    vh.txtDetails.setText(context.getString(R.string.contacts_section_addressbook_friends_in_app,
        contact.getHowManyFriends()));

    if (contact instanceof ContactAB) {
      vh.btnInvite.setImageResource(R.drawable.picto_invite);
    } else {
      vh.btnInvite.setImageResource(R.drawable.picto_messenger);
    }
  }

  /**
   * OBSERVABLES
   */

  public Observable<View> onInvite() {
    return onInvite;
  }
}
