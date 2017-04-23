package com.tribe.app.data.repository.user.contact;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

@Singleton public class RxContacts {

  private User user;
  private boolean withPhones;
  private Sorter sorter;
  private Filter[] filter;
  private PhoneUtils phoneUtils;
  private ContactsHelper helper;
  private Preference<Boolean> addressBook;
  private Map<String, ContactABRealm> mapContact;

  @Inject
  public RxContacts(Context context, @Named("userThreadSafe") User user, PhoneUtils phoneUtils,
      @AddressBook Preference<Boolean> addressBook) {
    this.user = user;
    this.phoneUtils = phoneUtils;
    withPhones = true;
    sorter = Sorter.LAST_TIME_CONTACTED;
    filter = new Filter[] { Filter.HAS_PHONE };
    helper = new ContactsHelper(context, phoneUtils);
    this.addressBook = addressBook;
    this.mapContact = new HashMap<>();
  }

  private Observable<List<ContactABRealm>> contactsObservable;

  /**
   * Run ContentResolver query and emit results to the Observable
   */
  public Observable<List<ContactABRealm>> getContacts() {
    if (contactsObservable == null) {
      contactsObservable =
          Observable.create((Subscriber<? super List<ContactABRealm>> subscriber) -> {
            //emit(null, withPhones, sorter, filter, subscriber);
            emitFast(subscriber);
          }).onBackpressureBuffer().serialize();
    }

    return contactsObservable;
  }

  /**
   * Experimental!
   * Faster query. Additional conditions doesn't work (withEmails, withPhotos, filters, sorters).
   * Use Rx filters instead
   */
  public Observable<Contact> getContactsFast() {
    return Observable.create((Subscriber<? super Contact> subscriber) -> {
      //emitFast(subscriber);
    }).onBackpressureBuffer().serialize();
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private void emit(String query, boolean withPhones, Sorter sorter, Filter[] filter,
      Subscriber<? super ContactABRealm> subscriber) {
    if (addressBook.get()) {
      long timeStart = System.nanoTime();
      int count = 0;

      Cursor c = helper.getContactsCursor(query, sorter, filter);
      while (c.moveToNext()) {
        ContactABRealm contact = helper.fetchContact(c, withPhones);
        if (contact != null) {
          count++;
          if (!subscriber.isUnsubscribed()) {
            subscriber.onNext(contact);
          } else {
            break;
          }
        }

        if (ContactsHelper.DEBUG) {
          Log.i("emit", contact.toString() + " is subscribed=" + !subscriber.isUnsubscribed());
        }
      }
      c.close();

      long timeEnd = System.nanoTime();
      Timber.d(
          "Total parsing contact of " + count + " / " + (timeEnd - timeStart) / 1000000.0f + " ms");
    }

    subscriber.onCompleted();
  }

  private void emitFast(Subscriber<? super List<ContactABRealm>> subscriber) {
    if (addressBook.get()) {
      long timeStart = System.nanoTime();
      Cursor c = helper.getFastContactsCursor();
      int count = c.getCount();
      int contactCount = 0;

      if (count != 0) {
        ContactABRealm contact;
        while (c.moveToNext()) {
          String contactId =
              String.valueOf(c.getLong(c.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID)));
          contact = mapContact.get(contactId);

          if (contact == null) {
            contact = new ContactABRealm();
            contact.setId(contactId);
            mapContact.put(contactId, contact);
            contactCount++;
          }

          helper.fetchContactFast(c, contact);

          if (ContactsHelper.DEBUG) {
            Log.i("emit fast",
                contact.toString() + " is subscribed=" + !subscriber.isUnsubscribed());
          }
        }
      }

      c.close();

      if (mapContact.size() > 0) {
        List<ContactABRealm> contactABRealmList = new ArrayList<>();
        for (ContactABRealm contact : mapContact.values()) {
          if (contact.hasAPhone()) {
            contactABRealmList.add(contact);
          }
        }

        if (!contactABRealmList.isEmpty()) subscriber.onNext(contactABRealmList);
        subscriber.onCompleted();
      }

      long timeEnd = System.nanoTime();
      Timber.d("Total parsing contact of "
          + contactCount
          + " / "
          + (timeEnd - timeStart) / 1000000.0f
          + " ms");
    }

    subscriber.onCompleted();
  }
}
