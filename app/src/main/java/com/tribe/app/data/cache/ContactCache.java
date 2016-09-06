package com.tribe.app.data.cache;

import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactFBRealm;

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
    public Observable<List<ContactABRealm>> contacts();
}
