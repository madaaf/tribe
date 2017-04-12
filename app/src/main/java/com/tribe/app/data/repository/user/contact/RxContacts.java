package com.tribe.app.data.repository.user.contact;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.view.utils.PhoneUtils;
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
  }

  private Observable<ContactABRealm> contactsObservable;

  /**
   * Run ContentResolver query and emit results to the Observable
   */
  public Observable<ContactABRealm> getContacts() {
    if (contactsObservable == null) {
      contactsObservable = Observable.create((Subscriber<? super ContactABRealm> subscriber) -> {
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

  private void emitFast(Subscriber<? super ContactABRealm> subscriber) {
    if (addressBook.get()) {
      long timeStart = System.nanoTime();
      Cursor c = helper.getFastContactsCursor();
      int count = c.getCount();
      int contactCount = 0;

      if (count != 0) {
        c.moveToNext();
        ContactABRealm contact;
        while (c.getPosition() < count) {
          contact = helper.fetchContactFast(c);
          if (contact != null) {
            contactCount++;

            if (!subscriber.isUnsubscribed()) {
              subscriber.onNext(contact);
            } else {
              break;
            }

            if (ContactsHelper.DEBUG) {
              Log.i("emit fast",
                  contact.toString() + " is subscribed=" + !subscriber.isUnsubscribed());
            }
          }
        }
      }

      c.close();

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
