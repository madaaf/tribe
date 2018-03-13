package com.tribe.app.data.realm;

import io.realm.RealmList;
import java.util.List;

/**
 * Created by tiago on 09/05/2016.
 */
public interface ContactInterface {

  String getId();

  void setHowManyFriends(int friends);

  int getHowManyFriends();

  void addUser(UserRealm userRealm);

  void setPhone(String phone);

  void setUserList(RealmList<UserRealm> userRealmList);

  List<UserRealm> getUsers();

  void setNew(boolean isNew);

  boolean isNew();

  void setFriends(RealmList<String> friends);
}
