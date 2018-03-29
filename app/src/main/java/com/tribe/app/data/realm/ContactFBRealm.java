package com.tribe.app.data.realm;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 05/09/2016.
 */
public class ContactFBRealm extends RealmObject implements ContactInterface {

  @PrimaryKey private String id;

  private String name;
  private String profilePicture;
  private RealmList<UserRealm> userList;
  private int howManyFriends = 0;
  private boolean isNew = false;
  private boolean hasApp = false;
  private RealmList<StringRealm> commonFriends;
  private RealmList<StringRealm> commonFriendsNameList;

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

  public String getProfilePicture() {
    return profilePicture;
  }

  public void setProfilePicture(String profilePicture) {
    this.profilePicture = profilePicture;
  }

  public RealmList<UserRealm> getUserList() {
    return userList;
  }

  @Override public boolean isNew() {
    return isNew;
  }

  @Override public void setCommonFriends(List<String> list) {
    this.commonFriends = new RealmList<>();
    if (list != null) {
      for (String friend : list) {
        StringRealm stringRealm = new StringRealm();
        stringRealm.setContent(friend);
        commonFriends.add(stringRealm);
      }
    }
  }

  @Override public void setCommonFriendsNameList(List<String> list) {
    this.commonFriendsNameList = new RealmList<>();
    if (list != null) {
      for (String friend : list) {
        StringRealm stringRealm = new StringRealm();
        stringRealm.setContent(friend);
        commonFriendsNameList.add(stringRealm);
      }
    }
  }

  @Override public void setNew(boolean aNew) {
    isNew = aNew;
  }

  @Override public void setUserList(RealmList<UserRealm> userList) {
    this.userList = userList;
  }

  @Override public int getHowManyFriends() {
    return howManyFriends;
  }

  public List<String> getcommonFriendsNameList() {
    List<String> list = new ArrayList<>();
    if (commonFriendsNameList != null) {
      for (StringRealm stringRealm : commonFriendsNameList) {
        list.add(stringRealm.getContent());
      }
    }
    return list;
  }
  @Override public void setHowManyFriends(int howManyFriends) {
    this.howManyFriends = howManyFriends;
  }

  public boolean hasApp() {
    return hasApp;
  }

  public void setHasApp(boolean hasApp) {
    this.hasApp = hasApp;
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

  }

  @Override public List<UserRealm> getUsers() {
    return userList;
  }
}
