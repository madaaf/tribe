package com.tribe.app.data.network.entity;

import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Invite;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tiago on 27/01/2017.
 */

public class SubscriptionResponse {

  private List<UserRealm> userUpdatedList;
  private List<GroupRealm> groupUpdatedList;
  private List<MembershipRealm> membershipCreatedList;
  private List<MembershipRealm> membershipRemovedList;
  private List<FriendshipRealm> friendshipCreatedList;
  private List<FriendshipRealm> friendshipUpdatedList;
  private List<FriendshipRealm> friendshipRemovedList;
  private Invite inviteCreated;
  private Invite inviteRemoved;
  private Map<String, Boolean> onlineMap;
  private Map<String, Boolean> liveMap;

  public SubscriptionResponse() {
    userUpdatedList = new ArrayList<>();
    groupUpdatedList = new ArrayList<>();
    membershipCreatedList = new ArrayList<>();
    membershipRemovedList = new ArrayList<>();
    friendshipCreatedList = new ArrayList<>();
    friendshipUpdatedList = new ArrayList<>();
    friendshipRemovedList = new ArrayList<>();
    onlineMap = new HashMap<>();
    liveMap = new HashMap<>();
  }

  public List<GroupRealm> getGroupUpdatedList() {
    return groupUpdatedList;
  }

  public void setGroupUpdatedList(List<GroupRealm> groupUpdatedList) {
    this.groupUpdatedList.clear();
    this.groupUpdatedList.addAll(groupUpdatedList);
  }

  public List<UserRealm> getUserUpdatedList() {
    return userUpdatedList;
  }

  public void setUserUpdatedList(List<UserRealm> userUpdatedList) {
    this.userUpdatedList.clear();
    this.userUpdatedList.addAll(userUpdatedList);
  }

  public void setLiveMap(Map<String, Boolean> liveMap) {
    this.liveMap.clear();
    this.liveMap.putAll(liveMap);
  }

  public Map<String, Boolean> getLiveMap() {
    return liveMap;
  }

  public void setOnlineMap(Map<String, Boolean> onlineMap) {
    this.onlineMap.clear();
    this.onlineMap.putAll(onlineMap);
  }

  public Map<String, Boolean> getOnlineMap() {
    return onlineMap;
  }

  public List<MembershipRealm> getMembershipCreatedList() {
    return membershipCreatedList;
  }

  public List<MembershipRealm> getMembershipRemovedList() {
    return membershipRemovedList;
  }

  public void setMembershipCreatedList(List<MembershipRealm> membershipCreatedList) {
    this.membershipCreatedList.clear();
    this.membershipCreatedList = membershipCreatedList;
  }

  public void setMembershipRemovedList(List<MembershipRealm> membershipRemovedList) {
    this.membershipRemovedList.clear();
    this.membershipRemovedList = membershipRemovedList;
  }

  public List<FriendshipRealm> getFriendshipCreatedList() {
    return friendshipCreatedList;
  }

  public void setFriendshipCreatedList(List<FriendshipRealm> friendshipCreatedList) {
    this.friendshipCreatedList.clear();
    this.friendshipCreatedList = friendshipCreatedList;
  }

  public List<FriendshipRealm> getFriendshipRemovedList() {
    return friendshipRemovedList;
  }

  public void setFriendshipRemovedList(List<FriendshipRealm> friendshipRemovedList) {
    this.friendshipRemovedList.clear();
    this.friendshipRemovedList = friendshipRemovedList;
  }

  public List<FriendshipRealm> getFriendshipUpdatedList() {
    return friendshipUpdatedList;
  }

  public void setFriendshipUpdatedList(List<FriendshipRealm> friendshipUpdatedList) {
    this.friendshipUpdatedList.clear();
    this.friendshipUpdatedList = friendshipUpdatedList;
  }

  public void setInviteCreated(Invite inviteCreated) {
    this.inviteCreated = inviteCreated;
  }

  public Invite getInviteCreated() {
    return inviteCreated;
  }

  public void setInviteRemoved(Invite inviteRemoved) {
    this.inviteRemoved = inviteRemoved;
  }

  public Invite getInviteRemoved() {
    return inviteRemoved;
  }
}
