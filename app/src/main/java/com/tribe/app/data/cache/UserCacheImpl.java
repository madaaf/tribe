package com.tribe.app.data.cache;

import android.content.Context;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.presentation.utils.StringUtils;
import io.realm.Realm;
import io.realm.RealmList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tiago on 06/05/2016.
 */
public class UserCacheImpl implements UserCache {

  private Context context;
  private Realm realm;
  private UserRealm results;
  private AccessToken accessToken;

  @Inject public UserCacheImpl(Context context, Realm realm, AccessToken accessToken) {
    this.context = context;
    this.realm = realm;
    this.accessToken = accessToken;
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
      obsRealm.executeTransaction(realm1 -> {
        realm1.delete(FriendshipRealm.class);
        realm1.insertOrUpdate(userRealm);
      });
    } finally {
      obsRealm.close();
    }
  }

  @Override public void updateCurrentUser(UserRealm userRealm) {
    Realm obsRealm = Realm.getDefaultInstance();

    try {
      obsRealm.executeTransaction(realm1 -> {
        UserRealm currentUser =
            realm1.where(UserRealm.class).equalTo("id", userRealm.getId()).findFirst();
        updateUser(userRealm, currentUser);
      });
    } finally {
      obsRealm.close();
    }
  }

  @Override public void incrUserTimeInCall(String userId, Long timeInCall) {
    Realm obsRealm = Realm.getDefaultInstance();

    try {
      obsRealm.executeTransaction(realm1 -> {
        UserRealm currentUser = realm1.where(UserRealm.class).equalTo("id", userId).findFirst();
        currentUser.setTimeInCall(currentUser.getTimeInCall() + timeInCall);
      });
    } finally {
      obsRealm.close();
    }
  }

  private void updateUser(UserRealm from, UserRealm to) {
    if (!StringUtils.isEmpty(from.getUsername())) to.setUsername(from.getUsername());
    if (!StringUtils.isEmpty(from.getPhone())) to.setPhone(from.getPhone());
    if (!StringUtils.isEmpty(from.getDisplayName())) to.setDisplayName(from.getDisplayName());
    if (from.getTimeInCall() > 0) to.setTimeInCall(from.getTimeInCall());
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
    if (from.getLastSeenAt() != null) to.setLastSeenAt(from.getLastSeenAt());
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
  @Override public Observable<UserRealm> userInfos(String userId) {
    return realm.where(UserRealm.class)
        .equalTo("id", userId)
        .findAll()
        .asObservable()
        .filter(userRealmList -> userRealmList.isLoaded() && userRealmList.size() > 0)
        .map(userRealmList -> userRealmList.get(0))
        .map(user -> realm.copyFromRealm(user))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }

  @Override public Observable<List<FriendshipRealm>> friendships() {
    return realm.where(FriendshipRealm.class)
        .findAll()
        .asObservable()
        .filter(friendshipList -> friendshipList.isLoaded())
        .map(friendshipList -> realm.copyFromRealm(friendshipList))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }

  @Override public UserRealm userInfosNoObs(String userId) {
    Realm obsRealm = Realm.getDefaultInstance();
    UserRealm userRealm = obsRealm.where(UserRealm.class).equalTo("id", userId).findFirst();
    final UserRealm results = userRealm == null ? null : obsRealm.copyFromRealm(userRealm);
    obsRealm.close();
    return results;
  }

  @Override public FriendshipRealm friendshipForUserId(String userId) {
    Realm otherRealm = Realm.getDefaultInstance();
    FriendshipRealm friendshipRealm =
        otherRealm.where(FriendshipRealm.class).equalTo("friend.id", userId).findFirst();
    if (friendshipRealm != null) {
      return otherRealm.copyFromRealm(friendshipRealm);
    } else {
      return null;
    }
  }

  @Override public void removeFriendship(String friendshipId) {
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

  @Override public void updateFriendship(FriendshipRealm friendshipRealm) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        FriendshipRealm friendshipRealmDB =
            realm1.where(FriendshipRealm.class).equalTo("id", friendshipRealm.getId()).findFirst();
        friendshipRealmDB.setMute(friendshipRealm.isMute());
        friendshipRealmDB.setStatus(friendshipRealm.getStatus());
      });
    } finally {
      realm.close();
    }
  }

  @Override public FriendshipRealm updateFriendshipNoObs(String friendshipId,
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

  @Override public void updateUserRealmList(List<UserRealm> userRealmList) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        for (UserRealm userRealm : userRealmList) {
          UserRealm userDB =
              realm1.where(UserRealm.class).equalTo("id", userRealm.getId()).findFirst();
          if (userDB != null) updateUser(userRealm, userDB);
        }
      });
    } finally {
      realm.close();
    }
  }

  @Override public void addFriendship(final FriendshipRealm friendshipRealm) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        UserRealm userRealmDB =
            realm1.where(UserRealm.class).equalTo("id", accessToken.getUserId()).findFirst();
        FriendshipRealm friendshipRealmDB =
            realm1.where(FriendshipRealm.class).equalTo("id", friendshipRealm.getId()).findFirst();
        if (friendshipRealmDB == null) {
          realm1.insertOrUpdate(friendshipRealm);
          friendshipRealmDB = realm1.where(FriendshipRealm.class)
              .equalTo("id", friendshipRealm.getId())
              .findFirst();
          userRealmDB.getFriendships().add(friendshipRealmDB);
        }
      });
    } finally {
      realm.close();
    }
  }

  @Override public void removeFriendship(FriendshipRealm friendshipRealm) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        UserRealm userRealmDB =
            realm1.where(UserRealm.class).equalTo("id", accessToken.getUserId()).findFirst();
        FriendshipRealm friendshipRealmDB =
            realm1.where(FriendshipRealm.class).equalTo("id", friendshipRealm.getId()).findFirst();
        if (friendshipRealmDB != null) friendshipRealmDB.deleteFromRealm();

        RealmList<FriendshipRealm> newFriendships = new RealmList<>();
        for (FriendshipRealm friendshipLoop : userRealmDB.getFriendships()) {
          if (!friendshipLoop.getId().equals(friendshipRealm.getId())) {
            newFriendships.add(friendshipLoop);
          }
        }

        userRealmDB.setFriendships(newFriendships);
      });
    } finally {
      realm.close();
    }
  }
}
