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

@Singleton
public class RxContacts {

    private User user;
    private int countryCode;
    private boolean withPhones;
    private Sorter sorter;
    private Filter[] filter;
    private PhoneUtils phoneUtils;
    private ContactsHelper helper;
    private Preference<Boolean> addressBook;

    @Inject
    public RxContacts(Context context, @Named("userThreadSafe") User user, PhoneUtils phoneUtils, @AddressBook Preference<Boolean> addressBook) {
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
     * @return
     */
    public Observable<ContactABRealm> getContacts() {
        this.countryCode = phoneUtils.getCountryCode(user.getPhone());
        helper.setCountryCode(countryCode);

        if (contactsObservable == null)
            contactsObservable = Observable.create((Subscriber<? super ContactABRealm> subscriber) -> {
                emit(null, withPhones, sorter, filter, subscriber);
            }).onBackpressureBuffer().serialize();

        return contactsObservable;
    }

    /**
     * Experimental!
     * Faster query. Additional conditions doesn't work (withEmails, withPhotos, filters, sorters). Use Rx filters instead
     * @return
     */
    public Observable<Contact> getContactsFast() {
        return Observable.create((Subscriber<? super Contact> subscriber) -> {
            emitFast(subscriber);
        }).onBackpressureBuffer().serialize();
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void emit(String query, boolean withPhones, Sorter sorter, Filter[] filter, Subscriber<? super ContactABRealm> subscriber) {
        if (addressBook.get()) {
            Cursor c = helper.getContactsCursor(query, sorter, filter);
            while (c.moveToNext()) {
                ContactABRealm contact = helper.fetchContact(c, withPhones);
                if (contact != null) {
                    if (!subscriber.isUnsubscribed())
                        subscriber.onNext(contact);
                    else
                        break;
                }

                if (ContactsHelper.DEBUG)
                    Log.i("emit", contact.toString() + " is subscribed=" + !subscriber.isUnsubscribed());
            }
            c.close();
        }

        subscriber.onCompleted();
    }

    private void emitFast(Subscriber<? super Contact> subscriber) {
        Cursor c = helper.getFastContactsCursor();
        int count = c.getCount();
        if (count != 0) {
            c.moveToNext();
            Contact contact;
            while (c.getPosition() < count) {
                contact = helper.fetchContactFast(c);
                if (!subscriber.isUnsubscribed())
                    subscriber.onNext(contact);
                else
                    break;

                if (ContactsHelper.DEBUG)
                    Log.i("emit fast", contact.toString() + " is subscribed=" + !subscriber.isUnsubscribed());
            }
        }
        c.close();

        subscriber.onCompleted();
    }
}
