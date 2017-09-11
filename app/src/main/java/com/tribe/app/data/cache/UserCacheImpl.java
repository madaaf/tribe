package com.tribe.app.data.cache;

import android.content.Context;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.ShortcutRealm;
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
        realm1.delete(ShortcutRealm.class);
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
    if (from.getJsonPayloadUpdate() == null ||
        from.getJsonPayloadUpdate().has(UserRealm.TRIBE_SAVE)) {
      to.setTribeSave(from.isTribeSave());
    }
    if (from.getJsonPayloadUpdate() == null ||
        from.getJsonPayloadUpdate().has(UserRealm.INVISIBLE_MODE)) {
      to.setInvisibleMode(from.isInvisibleMode());
    }
    if (from.getJsonPayloadUpdate() == null ||
        from.getJsonPayloadUpdate().has(UserRealm.PUSH_NOTIF)) {
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

  @Override public Observable<List<ShortcutRealm>> shortcuts() {
    return realm.where(ShortcutRealm.class)
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

  @Override public ShortcutRealm shortcutForUserId(String userId) {
    Realm otherRealm = Realm.getDefaultInstance();
    ShortcutRealm shortcutRealm =
        otherRealm.where(ShortcutRealm.class).equalTo("id", userId).findFirst();
    if (shortcutRealm != null) {
      return otherRealm.copyFromRealm(shortcutRealm);
    } else {
      return null;
    }
  }

  @Override public void removeShortcut(String shortcutId) {
    Realm otherRealm = Realm.getDefaultInstance();
    try {
      otherRealm.beginTransaction();
      ShortcutRealm shortcutRealm =
          otherRealm.where(ShortcutRealm.class).equalTo("id", shortcutId).findFirst();
      if (shortcutRealm != null) shortcutRealm.deleteFromRealm();
      otherRealm.commitTransaction();
    } catch (IllegalStateException ex) {
      if (otherRealm.isInTransaction()) otherRealm.cancelTransaction();
      ex.printStackTrace();
    } finally {
      otherRealm.close();
    }
  }

  @Override public void updateShortcut(ShortcutRealm shortcutRealm) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        ShortcutRealm shortcutRealmDB =
            realm1.where(ShortcutRealm.class).equalTo("id", shortcutRealm.getId()).findFirst();
        // TODO
        // shortcutRealmDB.setMute(shortcutRealm.isMute());
        // shortcutRealmDB.setStatus(shortcutRealm.getStatus());
      });
    } finally {
      realm.close();
    }
  }

  // TODO
  //@Override public ShortcutRealm updateShortcutNoObs(String friendshipId,
  //    @FriendshipRealm.FriendshipStatus String status) {
  //  Realm otherRealm = Realm.getDefaultInstance();
  //
  //  try {
  //    otherRealm.beginTransaction();
  //    FriendshipRealm friendshipRealm =
  //        otherRealm.where(FriendshipRealm.class).equalTo("id", friendshipId).findFirst();
  //    if (friendshipRealm != null) {
  //      friendshipRealm.setStatus(status);
  //    }
  //
  //    otherRealm.commitTransaction();
  //
  //    if (friendshipRealm != null) {
  //      return otherRealm.copyFromRealm(friendshipRealm);
  //    }
  //  } catch (IllegalStateException ex) {
  //    if (otherRealm.isInTransaction()) otherRealm.cancelTransaction();
  //    ex.printStackTrace();
  //  } finally {
  //    otherRealm.close();
  //  }
  //
  //  return null;
  //}

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

  @Override public void addShortcut(final ShortcutRealm shortcutRealm) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        UserRealm userRealmDB =
            realm1.where(UserRealm.class).equalTo("id", accessToken.getUserId()).findFirst();
        ShortcutRealm shortcutRealmDB =
            realm1.where(ShortcutRealm.class).equalTo("id", shortcutRealm.getId()).findFirst();
        if (shortcutRealmDB == null) {
          realm1.insertOrUpdate(shortcutRealm);
          shortcutRealmDB =
              realm1.where(ShortcutRealm.class).equalTo("id", shortcutRealm.getId()).findFirst();
          userRealmDB.getShortcuts().add(shortcutRealmDB);
        }
      });
    } finally {
      realm.close();
    }
  }

  @Override public void removeShortcut(ShortcutRealm shortcutRealm) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        UserRealm userRealmDB =
            realm1.where(UserRealm.class).equalTo("id", accessToken.getUserId()).findFirst();
        ShortcutRealm shortcutRealmDB =
            realm1.where(ShortcutRealm.class).equalTo("id", shortcutRealm.getId()).findFirst();
        if (shortcutRealmDB != null) shortcutRealmDB.deleteFromRealm();

        RealmList<ShortcutRealm> newShortcuts = new RealmList<>();
        for (ShortcutRealm shortcutLoop : userRealmDB.getShortcuts()) {
          if (!shortcutLoop.getId().equals(shortcutRealm.getId())) {
            newShortcuts.add(shortcutLoop);
          }
        }

        userRealmDB.setShortcuts(newShortcuts);
      });
    } finally {
      realm.close();
    }
  }
}
