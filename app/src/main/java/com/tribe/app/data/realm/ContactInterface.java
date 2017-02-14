package com.tribe.app.data.realm;

import io.realm.RealmList;
import java.util.List;

/**
 * Created by tiago on 09/05/2016.
 */
public interface ContactInterface {

  void setHowManyFriends(int friends);

  int getHowManyFriends();

  void addUser(UserRealm userRealm);

  void setUserList(RealmList<UserRealm> userRealmList);

  List<UserRealm> getUsers();
}
