package com.tribe.app.data.cache;

import android.content.Context;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupMemberRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.LocationRealm;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.presentation.utils.StringUtils;
import io.realm.Realm;
import io.realm.RealmList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by tiago on 06/05/2016.
 */
public class UserCacheImpl implements UserCache {

  private Context context;
  private Realm realm;
  private UserRealm results;

  @Inject
  public UserCacheImpl(Context context, Realm realm) {
    this.context = context;
    this.realm = realm;
  }

  public boolean isExpired() {
    return true;
  }

  public boolean isCached(int userId) {
    return false;
  }

  public void put(UserRealm userRealm) {
    Realm obsRealm = Realm.getDefaultInstance();

    try {
      obsRealm.beginTransaction();

      UserRealm userDB =
          obsRealm.where(UserRealm.class).equalTo("id", userRealm.getId()).findFirst();

      if (userDB != null) {
        updateFriendships(obsRealm, userRealm, userDB);
        updateUser(obsRealm, userRealm, userDB);
      } else {
        obsRealm.insertOrUpdate(userRealm);
        userDB = obsRealm.where(UserRealm.class).equalTo("id", userRealm.getId()).findFirst();
        updateFriendships(obsRealm, userRealm, userDB);
      }

      obsRealm.commitTransaction();
    } catch (IllegalStateException ex) {
      if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
      ex.printStackTrace();
    } finally {
      obsRealm.close();
    }
  }

  private void updateUser(Realm copyRealm, UserRealm from, UserRealm to) {
    if (!StringUtils.isEmpty(from.getUsername())) to.setUsername(from.getUsername());
    if (!StringUtils.isEmpty(from.getPhone())) to.setPhone(from.getPhone());
    if (!StringUtils.isEmpty(from.getDisplayName())) to.setDisplayName(from.getDisplayName());
    if (!StringUtils.isEmpty(from.getProfilePicture())) {
      to.setProfilePicture(from.getProfilePicture());
    }
    if (from.getJsonPayloadUpdate() == null || from.getJsonPayloadUpdate().has(UserRealm.FBID)) {
      to.setFbid(from.getFbid());
    }
    if (from.getJsonPayloadUpdate() == null || from.getJsonPayloadUpdate()
        .has(UserRealm.TRIBE_SAVE)) {
      to.setTribeSave(from.isTribeSave());
    }
    if (from.getJsonPayloadUpdate() == null || from.getJsonPayloadUpdate()
        .has(UserRealm.INVISIBLE_MODE)) {
      to.setInvisibleMode(from.isInvisibleMode());
    }
    if (from.getJsonPayloadUpdate() == null || from.getJsonPayloadUpdate()
        .has(UserRealm.PUSH_NOTIF)) {
      to.setPushNotif(from.isPushNotif());
    }
    if (from.getLastOnline() != null) to.setLastOnline(from.getLastOnline());
  }

  private void updateFriendships(Realm obsRealm, UserRealm userRealm, UserRealm userDB) {
    if (userRealm.getMemberships() != null) {
      for (MembershipRealm membershipRealm : userRealm.getMemberships()) {
        membershipRealm.setUpdatedAt(new Date());
        MembershipRealm membershipDB = obsRealm.where(MembershipRealm.class)
            .equalTo("id", membershipRealm.getId())
            .findFirst();

        obsRealm.insertOrUpdate(membershipRealm);

        if (membershipDB == null) {
          membershipDB = obsRealm.where(MembershipRealm.class)
              .equalTo("id", membershipRealm.getId())
              .findFirst();
          userDB.getMemberships().add(membershipDB);
        }

        boolean found = false;
        for (MembershipRealm membershipRealmDB : userDB.getMemberships()) {
          if (membershipRealmDB.getId().equals(membershipRealm.getId())) {
            found = true;
          }
        }

        if (!found) {
          userDB.getMemberships().add(membershipDB);
        }
      }
    }

    if (userRealm.getFriendships() != null) {
      for (FriendshipRealm friendshipRealm : userRealm.getFriendships()) {
        FriendshipRealm friendshipDB = obsRealm.where(FriendshipRealm.class)
            .equalTo("id", friendshipRealm.getId())
            .findFirst();

        if (friendshipDB != null) {
          friendshipDB.setMute(friendshipRealm.isMute());
          updateUser(obsRealm, friendshipRealm.getFriend(), friendshipDB.getFriend());
        } else {
          friendshipRealm.getFriend().setUpdatedAt(new Date());
          FriendshipRealm addedFriendship = obsRealm.copyToRealmOrUpdate(friendshipRealm);
          userDB.getFriendships().add(addedFriendship);
        }

        boolean found = false;
        for (FriendshipRealm friendshipRealmDB : userDB.getFriendships()) {
          if (friendshipRealmDB.getId().equals(friendshipRealm.getId())) {
            found = true;
          }
        }

        if (!found) {
          userDB.getFriendships().add(friendshipRealm);
        }
      }
    }
  }

  public void put(AccessToken accessToken) {
    Realm otherRealm = Realm.getDefaultInstance();
    try {
      otherRealm.beginTransaction();
      otherRealm.delete(AccessToken.class);
      otherRealm.copyToRealm(accessToken);
      otherRealm.commitTransaction();
    } catch (IllegalStateException ex) {
      if (otherRealm.isInTransaction()) otherRealm.cancelTransaction();
      ex.printStackTrace();
    } finally {
      otherRealm.close();
    }
  }

  public void put(Installation installation) {
    Realm otherRealm = Realm.getDefaultInstance();
    try {
      otherRealm.beginTransaction();
      otherRealm.delete(Installation.class);
      otherRealm.copyToRealm(installation);
      otherRealm.commitTransaction();
    } catch (IllegalStateException ex) {
      if (otherRealm.isInTransaction()) otherRealm.cancelTransaction();
      ex.printStackTrace();
    } finally {
      otherRealm.close();
    }
  }

  // ALWAYS CALLED ON MAIN THREAD
  @Override
  public Observable<UserRealm> userInfos(String userId) {
    return realm.where(UserRealm.class)
        .equalTo("id", userId)
        .findAll()
        .asObservable()
        .filter(userRealmList -> userRealmList.isLoaded() && userRealmList.size() > 0)
        .map(userRealmList -> userRealmList.get(0))
        .map(user -> realm.copyFromRealm(user))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }

  @Override
  public Observable<List<FriendshipRealm>> friendships() {
    return realm.where(FriendshipRealm.class)
        .findAll()
        .asObservable()
        .filter(friendshipList -> friendshipList.isLoaded())
        .map(friendshipList -> realm.copyFromRealm(friendshipList))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }

  @Override
  public UserRealm userInfosNoObs(String userId) {
    Realm obsRealm = Realm.getDefaultInstance();
    UserRealm userRealm = obsRealm.where(UserRealm.class).equalTo("id", userId).findFirst();
    final UserRealm results = userRealm == null ? null : obsRealm.copyFromRealm(userRealm);
    obsRealm.close();
    return results;
  }

  @Override
  public MembershipRealm membershipForGroupId(String groupId) {
    Realm obsRealm = Realm.getDefaultInstance();
    MembershipRealm membershipRealm =
        obsRealm.where(MembershipRealm.class).equalTo("group.id", groupId).findFirst();
    final MembershipRealm results =
        membershipRealm == null ? null : obsRealm.copyFromRealm(membershipRealm);
    obsRealm.close();
    return results;
  }

  @Override
  public FriendshipRealm friendshipForUserId(String userId) {
    Realm otherRealm = Realm.getDefaultInstance();
    FriendshipRealm friendshipRealm =
        otherRealm.where(FriendshipRealm.class).equalTo("friend.id", userId).findFirst();
    if (friendshipRealm != null) {
      return otherRealm.copyFromRealm(friendshipRealm);
    } else {
      return null;
    }
  }

  @Override
  public void removeFriendship(String friendshipId) {
    Realm otherRealm = Realm.getDefaultInstance();
    try {
      otherRealm.beginTransaction();
      FriendshipRealm friendshipRealm =
          otherRealm.where(FriendshipRealm.class).equalTo("id", friendshipId).findFirst();
      if (friendshipRealm != null) friendshipRealm.deleteFromRealm();
      otherRealm.commitTransaction();
    } catch (IllegalStateException ex) {
      if (otherRealm.isInTransaction()) otherRealm.cancelTransaction();
      ex.printStackTrace();
    } finally {
      otherRealm.close();
    }
  }

  @Override
  public void insertGroup(GroupRealm groupRealm) {
    Realm realm = Realm.getDefaultInstance();
    try {
      realm.beginTransaction();
      realm.copyToRealmOrUpdate(groupRealm);
      realm.commitTransaction();
    } catch (IllegalStateException ex) {
      ex.printStackTrace();
      if (realm.isInTransaction()) realm.cancelTransaction();
    } finally {
      realm.close();
    }
  }

  @Override
  public void updateGroup(GroupRealm groupRealm, boolean isFull) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        if (isFull) {
          realm1.insertOrUpdate(groupRealm);
        } else {
          GroupRealm groupRealmDB =
              realm1.where(GroupRealm.class).equalTo("id", groupRealm.getId()).findFirst();
          updateGroup(realm1, groupRealm, groupRealmDB);
        }
      });
    } finally {
      realm.close();
    }
  }

  private void updateGroup(Realm otherRealm, GroupRealm from, GroupRealm to) {
    if (from.getJsonPayloadUpdate() == null || from.getJsonPayloadUpdate().has(GroupRealm.NAME)) {
      to.setName(from.getName());
    }

    if (from.getJsonPayloadUpdate() == null || from.getJsonPayloadUpdate()
        .has(GroupRealm.PICTURE)) {
      to.setPicture(from.getPicture());
    }

    if (from.getMembers() != null && from.getMembers().size() > 0) {
      RealmList<GroupMemberRealm> groupMemberRealmList = new RealmList<>();

      for (GroupMemberRealm gmr : from.getMembers()) {
        GroupMemberRealm gmrDB =
            otherRealm.where(GroupMemberRealm.class).equalTo("id", gmr.getId()).findFirst();

        if (gmrDB == null) {
          otherRealm.insert(gmr);
          gmrDB =
              otherRealm.where(GroupMemberRealm.class).equalTo("id", gmr.getId()).findFirst();
        } else {
          updateGroupMember(gmr, gmrDB);
        }

        groupMemberRealmList.add(gmrDB);
      }

      to.setMembers(groupMemberRealmList);
    }
  }

  private void updateGroupMember(GroupMemberRealm from, GroupMemberRealm to) {
    to.setDisplayName(from.getDisplayName());
    to.setInvisibleMode(from.isInvisibleMode());
    to.setProfilePicture(from.getProfilePicture());
    to.setUsername(from.getUsername());
    to.setUpdatedAt(from.getUpdatedAt());
  }

  @Override
  public void addMembersToGroup(String groupId, List<String> memberIds) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", groupId).findFirst();

        for (String memberId : memberIds) {
          UserRealm userRealm = realm.where(UserRealm.class).equalTo("id", memberId).findFirst();
          groupRealm.getMembers().add(new GroupMemberRealm(userRealm));
        }
      });
    } finally {
      realm.close();
    }
  }

  @Override
  public void removeMembersFromGroup(String groupId, List<String> memberIds) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.beginTransaction();
      RealmList<UserRealm> usersToRemove = new RealmList<>();

      for (String memberId : memberIds) {
        usersToRemove.add(realm.where(UserRealm.class).equalTo("id", memberId).findFirst());
      }

      GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", groupId).findFirst();
      for (UserRealm user : usersToRemove) {
        groupRealm.getMembers().remove(user);
      }

      realm.commitTransaction();
    } catch (IllegalStateException ex) {
      ex.printStackTrace();
      if (realm.isInTransaction()) realm.cancelTransaction();
    } finally {
      realm.close();
    }
  }

  @Override
  public void removeGroup(String groupId) {
    Realm realm = Realm.getDefaultInstance();
    try {
      realm.beginTransaction();
      MembershipRealm membershipRealm =
          realm.where(MembershipRealm.class).equalTo("group.id", groupId).findFirst();
      if (membershipRealm != null) membershipRealm.deleteFromRealm();
      realm.commitTransaction();
    } catch (IllegalStateException ex) {
      ex.printStackTrace();
      if (realm.isInTransaction()) realm.cancelTransaction();
    } finally {
      realm.close();
    }
  }

  @Override
  public void removeGroupFromMembership(String membershipId) {
    Realm realm = Realm.getDefaultInstance();
    try {
      realm.beginTransaction();
      MembershipRealm membershipRealm =
          realm.where(MembershipRealm.class).equalTo("id", membershipId).findFirst();
      if (membershipRealm != null) membershipRealm.deleteFromRealm();
      realm.commitTransaction();
    } catch (IllegalStateException ex) {
      ex.printStackTrace();
      if (realm.isInTransaction()) realm.cancelTransaction();
    } finally {
      realm.close();
    }
  }

  @Override
  public void insertMembership(String userId, MembershipRealm membershipRealm) {
    Realm realm = Realm.getDefaultInstance();
    try {
      realm.beginTransaction();

      MembershipRealm membershipRealmDB = realm.copyToRealmOrUpdate(membershipRealm);
      UserRealm user = realm.where(UserRealm.class).equalTo("id", userId).findFirst();

      boolean found = false;

      if (user.getMemberships() != null) {
        for (MembershipRealm dbMembership : user.getMemberships()) {
          if (dbMembership.getGroup().getId().equals(membershipRealm.getGroup().getId())) {
            found = true;
          }
        }
      }

      if (!found) user.getMemberships().add(membershipRealmDB);

      realm.commitTransaction();
    } catch (IllegalStateException ex) {
      ex.printStackTrace();
      if (realm.isInTransaction()) realm.cancelTransaction();
    } finally {
      realm.close();
    }
  }

  @Override
  public void updateMembership(MembershipRealm membershipRealm) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        MembershipRealm membershipRealmDB =
            realm1.where(MembershipRealm.class).equalTo("id", membershipRealm.getId()).findFirst();
        membershipRealmDB.setMute(membershipRealm.isMute());
      });
    } finally {
      realm.close();
    }
  }

  @Override public void updateFriendship(FriendshipRealm friendshipRealm) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        FriendshipRealm friendshipRealmDB =
            realm1.where(FriendshipRealm.class).equalTo("id", friendshipRealm.getId()).findFirst();
        Timber.d("FriendshipRealm isMute : " + friendshipRealm.isMute());
        friendshipRealmDB.setMute(friendshipRealm.isMute());
      });
    } finally {
      realm.close();
    }
  }

  @Override
  public FriendshipRealm updateFriendshipNoObs(String friendshipId,
      @FriendshipRealm.FriendshipStatus String status) {
    Realm otherRealm = Realm.getDefaultInstance();

    try {
      otherRealm.beginTransaction();
      FriendshipRealm friendshipRealm =
          otherRealm.where(FriendshipRealm.class).equalTo("id", friendshipId).findFirst();
      if (friendshipRealm != null) {
        friendshipRealm.setStatus(status);
      }

      otherRealm.commitTransaction();

      if (friendshipRealm != null) {
        return otherRealm.copyFromRealm(friendshipRealm);
      }
    } catch (IllegalStateException ex) {
      if (otherRealm.isInTransaction()) otherRealm.cancelTransaction();
      ex.printStackTrace();
    } finally {
      otherRealm.close();
    }

    return null;
  }

  @Override
  public MembershipRealm membershipInfos(String membershipId) {
    Realm obsRealm = Realm.getDefaultInstance();
    MembershipRealm membershipRealm =
        obsRealm.where(MembershipRealm.class).equalTo("id", membershipId).findFirst();
    final MembershipRealm result =
        membershipRealm == null ? null : obsRealm.copyFromRealm(membershipRealm);
    obsRealm.close();
    return result;
  }

  @Override
  public void updateAll(List<UserRealm> userRealmList, List<GroupRealm> groupRealmList) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        for (UserRealm userRealm : userRealmList) {
          UserRealm userDB =
              realm1.where(UserRealm.class).equalTo("id", userRealm.getId()).findFirst();
          updateUser(realm1, userRealm, userDB);
        }

        for (GroupRealm groupRealm : groupRealmList) {
          GroupRealm groupRealmDB =
              realm1.where(GroupRealm.class).equalTo("id", groupRealm.getId()).findFirst();
          updateGroup(realm1, groupRealm, groupRealmDB);
        }
      });
    } finally {
      realm.close();
    }
  }
}
