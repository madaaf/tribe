package com.tribe.app.data.cache;

import android.content.Context;

import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactFBRealm;
import com.tribe.app.data.realm.SearchResultRealm;

import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by tiago on 06/05/2016.
 */
public class ContactCacheImpl implements ContactCache {

    private Context context;
    private Realm realm;
    private RealmResults<ContactABRealm> contacts;
    private RealmResults<ContactABRealm> contactsByValue;
    private RealmResults<SearchResultRealm> searchResult;

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
    public void insertSearchResult(SearchResultRealm searchResult) {
        Realm obsRealm = Realm.getDefaultInstance();

        try {
            obsRealm.beginTransaction();
            obsRealm.delete(SearchResultRealm.class);
            obsRealm.copyToRealm(searchResult);
            obsRealm.commitTransaction();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            obsRealm.close();
        }
    }

    @Override
    public void deleteSearchResults() {
        Realm obsRealm = Realm.getDefaultInstance();

        try {
            obsRealm.beginTransaction();
            obsRealm.delete(SearchResultRealm.class);
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
                contacts = realm.where(ContactABRealm.class).findAllSorted(new String[] {"howManyFriends", "name"}, new Sort[] {Sort.DESCENDING, Sort.ASCENDING});
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
    public Observable<List<ContactABRealm>> findContactsByValue(String value) {
        return Observable.create(new Observable.OnSubscribe<List<ContactABRealm>>() {
            @Override
            public void call(final Subscriber<? super List<ContactABRealm>> subscriber) {
                contactsByValue = realm.where(ContactABRealm.class)
                        .beginGroup()
                            .equalTo("userList.username", value)
                            .or()
                            .beginsWith("name", value)
                        .endGroup()
                        .findAllSorted(new String[] {"name"}, new Sort[] {Sort.ASCENDING});

                contactsByValue.removeChangeListeners();
                contactsByValue.addChangeListener(element -> {
                    if (element != null) {
                        subscriber.onNext(realm.copyFromRealm(element));
                    }
                });

                if (contactsByValue != null && contactsByValue.size() > 0)
                    subscriber.onNext(realm.copyFromRealm(contactsByValue));
            }
        });
    }

    @Override
    public Observable<SearchResultRealm> findContactByUsername(String value) {
        return Observable.create(new Observable.OnSubscribe<SearchResultRealm>() {
            @Override
            public void call(final Subscriber<? super SearchResultRealm> subscriber) {
                searchResult = realm.where(SearchResultRealm.class).findAll();

                searchResult.removeChangeListeners();
                searchResult.addChangeListener(element -> {
                    if (element != null && element.size() > 0) {
                        subscriber.onNext(searchResult.size() > 0 ? realm.copyFromRealm(searchResult.get(0)) : null);
                    }
                });

                if (searchResult != null)
                    subscriber.onNext(searchResult.size() > 0 ? realm.copyFromRealm(searchResult.get(0)) : null);
            }
        });
    }

    @Override
    public boolean isCached(int userId) {
        return false;
    }
}
