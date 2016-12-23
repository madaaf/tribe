package com.tribe.app.data.realm;

import java.util.List;

import io.realm.RealmList;

/**
 * Created by tiago on 09/05/2016.
 */
public interface ContactInterface {

    public void setHowManyFriends(int friends);
    public int getHowManyFriends();
    public void addUser(UserRealm userRealm);
    public void setUserList(RealmList<UserRealm> userRealmList);
    public List<UserRealm> getUsers();
}
