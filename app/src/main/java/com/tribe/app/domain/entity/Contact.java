package com.tribe.app.domain.entity;

import com.tribe.app.presentation.view.adapter.decorator.BaseSectionItemDecoration;
import com.tribe.app.presentation.view.adapter.interfaces.HomeAdapterInterface;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 02/09/2016.
 */
public class Contact implements Comparable<Contact>, HomeAdapterInterface {
  public static final String FACEBOOK_ID = "FACEBOOK_ID";
  public static final String ADDRESS_BOOK_ID = "ADDRESS_BOOK_ID";

  protected String id;
  protected String name;
  protected List<User> userList;
  protected int howManyFriends;
  protected List<String> commonFriendsNameList;
  protected boolean isNew;

  public Contact(String id) {
    this.id = id;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getId() != null ? getId().hashCode() : 0);
    return result;
  }

  @Override public int compareTo(Contact another) {
    return name != null && another.name != null ? name.compareToIgnoreCase(another.name) : -1;
  }

  public String getId() {
    return id;
  }

  @Override public boolean isOnline() {
    return (userList != null && userList.size() > 0) ? userList.get(0).isOnline() : false;
  }

  @Override public boolean isLive() {
    return false;
  }

  @Override public UserPlaying isPlaying() {
    return null;
  }

  @Override public String getDisplayName() {
    return name;
  }

  @Override public boolean isRead() {
    return true;
  }

  @Override public String getProfilePicture() {
    return (userList != null && userList.size() > 0) ? userList.get(0).getProfilePicture() : null;
  }

  @Override public Date getLastSeenAt() {
    return (userList != null && userList.size() > 0) ? userList.get(0).getLastSeenAt() : null;
  }

  @Override public int getHomeSectionType() {
    return BaseSectionItemDecoration.SEARCH_INVITES_TO_SEND;
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

  public List<User> getUserList() {
    return userList;
  }

  public void setUserList(List<User> userList) {
    this.userList = userList;
  }

  public int getHowManyFriends() {
    return howManyFriends;
  }

  public void setHowManyFriends(int howManyFriends) {
    this.howManyFriends = howManyFriends;
  }

  public void setcommonFriendsNameList(List<String> commonFriendsNameList) {
    this.commonFriendsNameList = commonFriendsNameList;
  }

  public List<String> getcommonFriendsNameList() {
    return commonFriendsNameList;
  }

  public boolean isNew() {
    return isNew;
  }

  public void setNew(boolean aNew) {
    isNew = aNew;
  }
}
