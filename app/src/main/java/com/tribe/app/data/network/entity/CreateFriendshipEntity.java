package com.tribe.app.data.network.entity;

import com.tribe.app.data.realm.FriendshipRealm;
import java.util.List;

/**
 * Created by tiago on 05/09/2016.
 */
public class CreateFriendshipEntity {

  private List<FriendshipRealm> newFriendshipList;

  public List<FriendshipRealm> getNewFriendshipList() {
    return newFriendshipList;
  }

  public void setNewFriendshipList(List<FriendshipRealm> newFriendshipList) {
    this.newFriendshipList = newFriendshipList;
  }
}
