package com.tribe.app.data.cache;

import android.content.Context;

import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactFBRealm;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.SearchResultRealm;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import io.realm.Case;
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
        } catch (IllegalStateException ex) {
            if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
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
        } catch (IllegalStateException ex) {
            if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
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

            SearchResultRealm searchResultRealm = obsRealm.where(SearchResultRealm.class).findFirst();

            if (searchResultRealm == null) {
                obsRealm.copyToRealmOrUpdate(searchResult);
            } else {
                searchResultRealm.setDisplayName(searchResult.getDisplayName());
                searchResultRealm.setPicture(searchResult.getPicture());
                searchResultRealm.setUsername(searchResult.getUsername());
                searchResultRealm.setId(searchResult.getId());
                searchResultRealm.setInvisibleMode(searchResult.isInvisibleMode());
                if (searchResult.getFriendshipRealm() != null)
                    searchResultRealm.setFriendshipRealm(obsRealm.where(FriendshipRealm.class).equalTo("id", searchResult.getFriendshipRealm().getId()).findFirst());
                else
                    searchResultRealm.setFriendshipRealm(null);
                searchResultRealm.setSearchDone(searchResult.isSearchDone());
            }

            obsRealm.commitTransaction();
        } catch (IllegalStateException ex) {
            if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
            ex.printStackTrace();
        } finally {
            obsRealm.close();
        }
    }

    @Override
    public void changeSearchResult(String username, FriendshipRealm friendshipRealm) {
        Realm obsRealm = Realm.getDefaultInstance();
        SearchResultRealm resultRealm = obsRealm.where(SearchResultRealm.class).equalTo("username", username).findFirst();
        FriendshipRealm friendshipManaged = obsRealm.where(FriendshipRealm.class).equalTo("id", friendshipRealm.getId()).findFirst();

        try {
            if (resultRealm != null) {
                obsRealm.beginTransaction();
                friendshipManaged.setBlocked(friendshipRealm.isBlocked());
                friendshipManaged.setStatus(friendshipRealm.getStatus());
                resultRealm.setFriendshipRealm(friendshipManaged);
                obsRealm.commitTransaction();
            }
        } catch (IllegalStateException ex) {
            if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
            ex.printStackTrace();
        } finally {
            obsRealm.close();
        }
    }

    @Override
    public Observable<List<ContactABRealm>> contacts() {
        return realm.where(ContactABRealm.class)
                .findAllSorted(new String[] {"howManyFriends", "name"}, new Sort[] {Sort.DESCENDING, Sort.ASCENDING})
                .asObservable()
                .filter(contactABRealms -> contactABRealms.isLoaded())
                .map(contactABRealms -> realm.copyFromRealm(contactABRealms));
    }

    @Override
    public Observable<List<ContactFBRealm>> contactsFB() {
        return realm.where(ContactFBRealm.class)
                    .findAllSorted(new String[] {"name"}, new Sort[] {Sort.ASCENDING})
                    .asObservable()
                    .filter(contactFBRealms -> contactFBRealms.isLoaded())
                    .map(contactFBRealms -> realm.copyFromRealm(contactFBRealms));
    }

    @Override
    public Observable<List<ContactABRealm>> contactsThreadSafe() {
        return Observable.create(new Observable.OnSubscribe<List<ContactABRealm>>() {
            @Override
            public void call(final Subscriber<? super List<ContactABRealm>> subscriber) {
                Realm realmObs = Realm.getDefaultInstance();
                RealmResults<ContactABRealm> contactABRealmList = realmObs.where(ContactABRealm.class).findAllSorted(new String[] {"name"}, new Sort[] {Sort.ASCENDING});
                if (contactABRealmList != null)
                    subscriber.onNext(realmObs.copyFromRealm(contactABRealmList));
                realmObs.close();
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
                            .beginsWith("name", value, Case.INSENSITIVE)
                        .endGroup()
                        .findAllSorted(new String[] {"howManyFriends", "name"}, new Sort[] {Sort.DESCENDING, Sort.ASCENDING});

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
    public void updateHowManyFriends(Collection<ContactABRealm> contactABList) {
        Realm obsRealm = Realm.getDefaultInstance();

        try {
            obsRealm.beginTransaction();
            for (ContactABRealm contactABRealm : contactABList) {
                ContactABRealm contactDB = obsRealm.where(ContactABRealm.class).equalTo("id", contactABRealm.getId()).findFirst();
                contactDB.setHowManyFriends(contactABRealm.getHowManyFriends());
            }
            obsRealm.commitTransaction();
        } catch (IllegalStateException ex) {
            if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
            ex.printStackTrace();
        } finally {
            obsRealm.close();
        }
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
    public void deleteContactsAB() {
        Realm obsRealm = Realm.getDefaultInstance();

        try {
            obsRealm.executeTransaction(realm1 -> {
                realm1.delete(ContactABRealm.class);
            });
        } finally {
            obsRealm.close();
        }
    }

    @Override
    public void deleteContactsFB() {
        Realm obsRealm = Realm.getDefaultInstance();

        try {
            obsRealm.executeTransaction(realm1 -> {
                realm1.delete(ContactFBRealm.class);
            });
        } finally {
            obsRealm.close();
        }
    }

    @Override
    public boolean isCached(int userId) {
        return false;
    }
}
