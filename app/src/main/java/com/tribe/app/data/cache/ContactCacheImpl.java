package com.tribe.app.data.cache;

import android.content.Context;

import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactFBRealm;

import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by tiago on 06/05/2016.
 */
public class ContactCacheImpl implements ContactCache {

    private Context context;
    private Realm realm;
    private RealmResults<ContactABRealm> contacts;

    @Inject
    public ContactCacheImpl(Context context, Realm realm) {
        this.context = context;
        this.realm = realm;
    }

    @Override
    public void insertAddressBook(List<ContactABRealm> contactList) {
        Realm obsRealm = Realm.getDefaultInstance();

        try {
            obsRealm.beginTransaction();
            obsRealm.delete(ContactABRealm.class);
            obsRealm.copyToRealmOrUpdate(contactList);
            obsRealm.commitTransaction();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            obsRealm.close();
        }
    }

    @Override
    public void insertFBContactList(List<ContactFBRealm> contactList) {
        Realm obsRealm = Realm.getDefaultInstance();

        try {
            obsRealm.beginTransaction();
            obsRealm.delete(ContactFBRealm.class);
            obsRealm.copyToRealmOrUpdate(contactList);
            obsRealm.commitTransaction();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            obsRealm.close();
        }
    }

    @Override
    public Observable<List<ContactABRealm>> contacts() {
        return Observable.create(new Observable.OnSubscribe<List<ContactABRealm>>() {
            @Override
            public void call(final Subscriber<? super List<ContactABRealm>> subscriber) {
                contacts = realm.where(ContactABRealm.class).findAll();
                contacts.removeChangeListeners();
                contacts.addChangeListener(element -> {
                    if (element != null) {
                        subscriber.onNext(realm.copyFromRealm(element));
                    }
                });

                if (contacts != null)
                    subscriber.onNext(realm.copyFromRealm(contacts));
            }
        });
    }

    @Override
    public boolean isCached(int userId) {
        return false;
    }
}
