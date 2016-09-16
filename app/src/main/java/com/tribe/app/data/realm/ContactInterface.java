package com.tribe.app.data.realm;

import java.util.List;

/**
 * Created by tiago on 09/05/2016.
 */
public interface ContactInterface {

    public void setHowManyFriends(int friends);
    public int getHowManyFriends();
    public void addUser(UserRealm userRealm);
    public List<UserRealm> getUsers();
}
