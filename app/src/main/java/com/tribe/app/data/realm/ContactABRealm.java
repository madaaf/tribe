package com.tribe.app.data.realm;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import java.util.List;

/**
 * Created by tiago on 05/09/2016.
 */
public class ContactABRealm extends RealmObject implements ContactInterface {

  @PrimaryKey private String id;

  private String name;
  private RealmList<PhoneRealm> phones;
  private long lastTimeContacted;
  private int version;
  private RealmList<UserRealm> userList;
  private int howManyFriends;
  private boolean isNew = false;

  public ContactABRealm() {

  }

  @Override public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public RealmList<PhoneRealm> getPhones() {
    return phones;
  }

  public void setPhones(RealmList<PhoneRealm> phones) {
    this.phones = phones;
  }

  public long getLastTimeContacted() {
    return lastTimeContacted;
  }

  public void setLastTimeContacted(long lastTimeContacted) {
    this.lastTimeContacted = lastTimeContacted;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  @Override public boolean isNew() {
    return isNew;
  }

  @Override public void setNew(boolean aNew) {
    isNew = aNew;
  }

  @Override public void setHowManyFriends(int howManyFriends) {
    this.howManyFriends = howManyFriends;
  }

  @Override public int getHowManyFriends() {
    return howManyFriends;
  }

  @Override public void addUser(UserRealm userRealm) {
    if (this.userList == null) this.userList = new RealmList<>();

    boolean shouldAdd = true;

    for (UserRealm userExisting : userList) {
      if (userExisting.equals(userRealm)) {
        shouldAdd = false;
        break;
      }
    }

    if (shouldAdd) this.userList.add(userRealm);
  }

  @Override public void setPhone(String phone) {
    if (phones == null) {
      phones = new RealmList<>();
    } else if (phones.size() > 0) {
      phones.get(0).setPhone(phone);
      return;
    }

    PhoneRealm phoneRealm = new PhoneRealm();
    phoneRealm.setPhone(phone);
    phones.add(phoneRealm);
  }

  @Override public void setUserList(RealmList<UserRealm> userList) {
    this.userList = userList;
  }

  @Override public List<UserRealm> getUsers() {
    return userList;
  }
}
