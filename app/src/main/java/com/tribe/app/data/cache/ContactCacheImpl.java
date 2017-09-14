package com.tribe.app.data.cache;

import android.content.Context;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactFBRealm;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tiago on 06/05/2016.
 */
public class ContactCacheImpl implements ContactCache {

  private Context context;
  private Realm realm;

  @Inject public ContactCacheImpl(Context context, Realm realm) {
    this.context = context;
    this.realm = realm;
  }

  @Override public void insertAddressBook(List<ContactABRealm> contactList) {
    Realm obsRealm = Realm.getDefaultInstance();

    try {
      obsRealm.executeTransaction(realm1 -> {
        realm1.insertOrUpdate(contactList);
      });
    } finally {
      obsRealm.close();
    }
  }

  @Override public void insertFBContactList(List<ContactFBRealm> contactList) {
    Realm obsRealm = Realm.getDefaultInstance();

    try {
      obsRealm.executeTransaction(realm1 -> realm1.insertOrUpdate(contactList));
    } finally {
      obsRealm.close();
    }
  }

  @Override public void insertSearchResult(SearchResultRealm searchResult) {
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
        if (searchResult.getShortcutRealm() != null) {
          searchResultRealm.setShortcutRealm(obsRealm.where(ShortcutRealm.class)
              .equalTo("id", searchResult.getShortcutRealm().getId())
              .findFirst());
        } else {
          searchResultRealm.setShortcutRealm(null);
        }
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

  @Override public void changeSearchResult(String username, ShortcutRealm shortcutRealm) {
    Realm obsRealm = Realm.getDefaultInstance();
    SearchResultRealm resultRealm =
        obsRealm.where(SearchResultRealm.class).equalTo("username", username).findFirst();
    ShortcutRealm shortcutManaged =
        obsRealm.where(ShortcutRealm.class).equalTo("id", shortcutRealm.getId()).findFirst();

    try {
      if (resultRealm != null) {
        obsRealm.beginTransaction();
        shortcutManaged.setStatus(shortcutManaged.getStatus());
        resultRealm.setShortcutRealm(shortcutManaged);
        obsRealm.commitTransaction();
      }
    } catch (IllegalStateException ex) {
      if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
      ex.printStackTrace();
    } finally {
      obsRealm.close();
    }
  }

  @Override public Observable<List<ContactABRealm>> contacts() {
    return realm.where(ContactABRealm.class)
        .findAllSorted(new String[] { "howManyFriends", "name" },
            new Sort[] { Sort.DESCENDING, Sort.ASCENDING })
        .asObservable()
        .filter(contactABRealms -> contactABRealms.isLoaded())
        .map(contactABRealms -> realm.copyFromRealm(contactABRealms));
  }

  @Override public Observable<List<ContactFBRealm>> contactsFB() {
    return realm.where(ContactFBRealm.class)
        .findAllSorted(new String[] { "name" }, new Sort[] { Sort.ASCENDING })
        .asObservable()
        .filter(contactFBRealms -> contactFBRealms.isLoaded())
        .map(contactFBRealms -> realm.copyFromRealm(contactFBRealms));
  }

  @Override public Observable<List<ContactInterface>> contactsOnApp() {
    return Observable.combineLatest(realm.where(ContactABRealm.class)
        .isNotEmpty("userList")
        .findAllSorted(new String[] { "name" }, new Sort[] { Sort.ASCENDING })
        .asObservable()
        .map(contactABRealms -> realm.copyFromRealm(contactABRealms))
        .defaultIfEmpty(new ArrayList<>()), realm.where(ContactFBRealm.class)
        .isNotEmpty("userList")
        .findAllSorted(new String[] { "name" }, new Sort[] { Sort.ASCENDING })
        .asObservable()
        .map(contactFBRealms -> realm.copyFromRealm(contactFBRealms))
        .defaultIfEmpty(new ArrayList<>()), (contactABRealms, contactFBRealms) -> {
      List<ContactInterface> ci = new ArrayList<>();
      ci.addAll(contactABRealms);
      ci.addAll(contactFBRealms);
      return ci;
    });
  }

  @Override public Observable<List<ContactABRealm>> contactsThreadSafe() {
    return Observable.create(subscriber -> {
      Realm realmObs = Realm.getDefaultInstance();
      RealmResults<ContactABRealm> contactABRealmList = realmObs.where(ContactABRealm.class)
          .findAllSorted(new String[] { "name" }, new Sort[] { Sort.ASCENDING });
      if (contactABRealmList != null) {
        subscriber.onNext(realmObs.copyFromRealm(contactABRealmList));
      }
      realmObs.close();
    });
  }

  @Override public Observable<List<ContactABRealm>> contactsToInvite() {
    return realm.where(ContactABRealm.class)
        .greaterThanOrEqualTo("howManyFriends", 1)
        .isEmpty("userList")
        .findAllSorted(new String[] { "howManyFriends", "name" },
            new Sort[] { Sort.DESCENDING, Sort.ASCENDING })
        .asObservable()
        .filter(contactABRealms -> contactABRealms.isLoaded())
        .map(contactABRealms -> realm.copyFromRealm(contactABRealms));
  }

  @Override public Observable<List<ContactABRealm>> findContactsByValue(String value) {
    return realm.where(ContactABRealm.class)
        .beginGroup()
        .equalTo("userList.username", value)
        .or()
        .beginsWith("name", value, Case.INSENSITIVE)
        .endGroup()
        .findAllSorted(new String[] { "howManyFriends", "name" },
            new Sort[] { Sort.DESCENDING, Sort.ASCENDING })
        .asObservable()
        .filter(contacts -> contacts.isLoaded())
        .map(contacts -> realm.copyFromRealm(contacts))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }

  @Override public void updateHowManyFriends(Collection<ContactABRealm> contactABList) {
    Realm obsRealm = Realm.getDefaultInstance();

    try {
      obsRealm.beginTransaction();
      for (ContactABRealm contactABRealm : contactABList) {
        ContactABRealm contactDB =
            obsRealm.where(ContactABRealm.class).equalTo("id", contactABRealm.getId()).findFirst();
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

  @Override public Observable<SearchResultRealm> findContactByUsername(String value) {
    return realm.where(SearchResultRealm.class)
        .findAll()
        .asObservable()
        .filter(searchResultRealmList -> searchResultRealmList.isLoaded() &&
            searchResultRealmList.size() > 0)
        .map(searchResultRealmList -> searchResultRealmList.get(0))
        .map(o -> realm.copyFromRealm(o))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }

  @Override public void deleteContactsAB() {
    Realm obsRealm = Realm.getDefaultInstance();

    try {
      obsRealm.executeTransaction(realm1 -> realm1.delete(ContactABRealm.class));
    } finally {
      obsRealm.close();
    }
  }

  @Override public void updateFromDB(List<ContactInterface> contactList) {
    Realm obsRealm = Realm.getDefaultInstance();
    try {
      obsRealm.executeTransaction(realm1 -> {
        for (ContactInterface contact : contactList) {
          if (contact instanceof ContactABRealm) {
            ContactABRealm contactABRealm =
                realm1.where(ContactABRealm.class).equalTo("id", contact.getId()).findFirst();
            if (contactABRealm != null) contact.setNew(contactABRealm.isNew());
          } else if (contact instanceof ContactFBRealm) {
            ContactFBRealm contactFBRealm =
                realm1.where(ContactFBRealm.class).equalTo("id", contact.getId()).findFirst();
            if (contactFBRealm != null) contact.setNew(contactFBRealm.isNew());
          }
        }
      });
    } finally {
      obsRealm.close();
    }
  }

  @Override public void removeNewStatus() {
    Realm obsRealm = Realm.getDefaultInstance();
    try {
      obsRealm.executeTransaction(realm1 -> {
        RealmResults<ContactABRealm> contactABRealmResults =
            realm1.where(ContactABRealm.class).findAll();

        if (contactABRealmResults != null) {
          for (ContactABRealm contactABRealm : contactABRealmResults) {
            if (contactABRealm.isNew()) contactABRealm.setNew(false);
          }
        }

        RealmResults<ContactFBRealm> contactFBRealmResults =
            realm1.where(ContactFBRealm.class).findAll();

        if (contactFBRealmResults != null) {
          for (ContactFBRealm contactFBRealm : contactFBRealmResults) {
            if (contactFBRealm.isNew()) contactFBRealm.setNew(false);
          }
        }
      });
    } finally {
      obsRealm.close();
    }
  }

  @Override public void deleteContactsFB() {
    Realm obsRealm = Realm.getDefaultInstance();

    try {
      obsRealm.executeTransaction(realm1 -> realm1.delete(ContactFBRealm.class));
    } finally {
      obsRealm.close();
    }
  }

  @Override public boolean isCached(int userId) {
    return false;
  }
}
