package com.tribe.app.presentation.view.adapter.delegate.contact;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tokenautocomplete.CustomTypefaceSpan;
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
  private PublishSubject<Pair> onClickFb = PublishSubject.create();
  private PublishSubject<Pair> onClickAddressBook = PublishSubject.create();

  // VARIABLE
  private Typeface fontBold;
  private TypefaceSpan robotoRegularSpan;

  public ContactToInviteAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

    fontBold = Typeface.createFromAsset(context.getAssets(), "ProximaNovaSoft-Bold.ttf");
    robotoRegularSpan = new CustomTypefaceSpan("", fontBold);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    ContactToInviteViewHolder vh = new ContactToInviteViewHolder(
        layoutInflater.inflate(R.layout.item_contact_to_invite, parent, false));

    vh.itemView.setOnClickListener(view -> onInvite.onNext(vh.itemView));
    vh.btnInvite.setOnClickListener(view -> onInvite.onNext(vh.itemView));
    return vh;
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {

    return (items.get(position) instanceof Contact)
        || (items.get(position) instanceof Contact) && ((Contact) items.get(position)).getId()
        .equals(Contact.FACEBOOK_ID);
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
    bind((ContactToInviteViewHolder) holder, (Contact) items.get(position), position);
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    bind((ContactToInviteViewHolder) holder, (Contact) items.get(position), position);
  }

  public void bind(ContactToInviteViewHolder vh, Contact contact, int position) {
    vh.txtName.setText(contact.getName());
    if (contact.getHowManyFriends() > 0) {
      vh.txtDetails.setVisibility(View.VISIBLE);

      StringBuilder friendsList = new StringBuilder();
      String m1 = context.getString(R.string.contacts_section_addressbook_friends_in_app_prefix);
      String m2 = context.getString(R.string.contacts_section_addressbook_friends_in_app_friends,
          contact.getHowManyFriends());
      String m3 = context.getString(R.string.contacts_section_addressbook_friends_in_app_suffix);
      String m4 = context.getString(R.string.contacts_section_addressbook_friends_in_app_including);

      String m = m1 + " " + m2 + " " + m3 + " ";
      Boolean haveCommonFriend =
          contact.getcommonFriendsNameList() != null && !contact.getcommonFriendsNameList()
              .isEmpty();
      if (haveCommonFriend) {
        for (int i = 0; i < contact.getcommonFriendsNameList().size(); i++) {
          String name = contact.getcommonFriendsNameList().get(i);
          if (i == contact.getcommonFriendsNameList().size() - 1) {
            friendsList.append(name);
          } else {
            friendsList.append(name).append(", ");
          }
        }
        m += m4 + " " + friendsList;
      }

      SpannableStringBuilder finalSpan = new SpannableStringBuilder(m);
      finalSpan.setSpan(robotoRegularSpan, m.indexOf(m2), m.indexOf(m2) + m2.length(),
          Spannable.SPAN_INCLUSIVE_INCLUSIVE);
      if (haveCommonFriend) {
        finalSpan.setSpan(robotoRegularSpan, m.indexOf(friendsList.toString()),
            m.indexOf(friendsList.toString()) + friendsList.length(),
            Spannable.SPAN_INCLUSIVE_INCLUSIVE);
      }

      vh.txtDetails.setText(finalSpan);
    } else {
      vh.txtDetails.setVisibility(View.GONE);
    }

    if (contact instanceof ContactAB) {
      vh.btnInvite.setImageResource(R.drawable.picto_invite);
    } else {
      vh.btnInvite.setImageResource(R.drawable.picto_messenger);
    }

    if (contact.getId().equals(Contact.FACEBOOK_ID)) {
      vh.txtDetails.setVisibility(View.VISIBLE);
      vh.btnInvite.setImageResource(R.drawable.picto_fb);
      vh.txtName.setText(context.getString(R.string.home_sync_facebook_title));
      vh.txtDetails.setText(context.getString(R.string.home_sync_facebook_subtitle));

      vh.itemView.setOnClickListener(view -> {
        onClickFb.onNext(new Pair<>(position, contact));
      });
    } else if (contact.getId().equals(Contact.ADDRESS_BOOK_ID)) {
      vh.txtDetails.setVisibility(View.VISIBLE);
      vh.btnInvite.setImageResource(R.drawable.picto_address_book);
      vh.txtName.setText(context.getString(R.string.home_sync_address_book_title));
      vh.txtDetails.setText(context.getString(R.string.home_sync_address_book_subtitle));
      vh.itemView.setOnClickListener(view -> {
        onClickAddressBook.onNext(new Pair<>(position, contact));
      });
    }
  }

  /**
   * OBSERVABLES
   */

  public Observable<View> onInvite() {
    return onInvite;
  }

  public Observable<Pair> onClickFb() {
    return onClickFb;
  }

  public Observable<Pair> onClickAddressBook() {
    return onClickAddressBook;
  }
}
