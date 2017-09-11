package com.tribe.app.data.cache;

import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactFBRealm;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import java.util.Collection;
import java.util.List;
import javax.inject.Singleton;
import rx.Observable;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton public interface ContactCache {

  boolean isCached(int userId);

  void insertAddressBook(List<ContactABRealm> contactList);

  void insertFBContactList(List<ContactFBRealm> contactList);

  void insertSearchResult(SearchResultRealm searchResultRealm);

  void changeSearchResult(String username, ShortcutRealm shortcutRealm);

  void updateHowManyFriends(Collection<ContactABRealm> contactABList);

  /**
   * Should only be called from a main thread interactor (UseCaseDisk)
   * it is NOT thread safe due to the Realm thread's complexity
   * Updates the UI through subscribers when changes are made to any ContactABRealm object
   */
  Observable<List<ContactABRealm>> contacts();

  /**
   * Should only be called from a main thread interactor (UseCaseDisk)
   * it is NOT thread safe due to the Realm thread's complexity
   * Updates the UI through subscribers when changes are made to any ContactFBRealm object
   */
  Observable<List<ContactFBRealm>> contactsFB();

  /**
   * Should only be called from a main thread interactor (UseCaseDisk)
   * it is NOT thread safe due to the Realm thread's complexity
   * Updates the UI through subscribers when changes are made to any ContactFBRealm object
   */
  Observable<List<ContactInterface>> contactsOnApp();

  /**
   * This contacts can be called anywhere, it is thread safe due to the Realm thread's complexity
   */
  Observable<List<ContactABRealm>> contactsThreadSafe();

  /**
   * Should only be called from a main thread interactor (UseCaseDisk)
   * it is NOT thread safe due to the Realm thread's complexity
   */
  Observable<List<ContactABRealm>> contactsToInvite();

  Observable<List<ContactABRealm>> findContactsByValue(String value);

  Observable<SearchResultRealm> findContactByUsername(String username);

  void deleteContactsFB();

  void deleteContactsAB();

  void updateFromDB(List<ContactInterface> contactList);

  void removeNewStatus();
}
