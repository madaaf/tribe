package com.tribe.app.data.cache;

import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactFBRealm;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.SearchResultRealm;

import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import rx.Observable;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton
public interface ContactCache {

    public boolean isCached(int userId);
    public void insertAddressBook(List<ContactABRealm> contactList);
    public void insertFBContactList(List<ContactFBRealm> contactList);
    public void insertSearchResult(SearchResultRealm searchResultRealm);
    public void changeSearchResult(String username, FriendshipRealm friendshipRealm);
    public void updateHowManyFriends(Collection<ContactABRealm> contactABList);

    /**
     * This contacts should only be called from a main thread interactor (UseCaseDisk)
     * it is NOT thread safe due to the Realm thread's complexity
     * Updates the UI through subscribers when changes are made to any ContactABRealm object
     * @return
     */
    public Observable<List<ContactABRealm>> contacts();


    /**
     * This contacts can be called anywhere, it is thread safe due to the Realm thread's complexity
     * @return
     */
    public Observable<List<ContactABRealm>> contactsThreadSafe();

    public Observable<List<ContactABRealm>> findContactsByValue(String value);
    public Observable<SearchResultRealm> findContactByUsername(String username);
}
