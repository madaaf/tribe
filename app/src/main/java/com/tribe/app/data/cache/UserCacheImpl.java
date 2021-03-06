package com.tribe.app.data.cache;

import android.content.Context;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.BadgeRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.data.realm.ShortcutLastSeenRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.utils.ShortcutUtils;
import com.tribe.app.presentation.utils.StringUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.exceptions.RealmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by tiago on 06/05/2016.
 */
public class UserCacheImpl implements UserCache {

  private Context context;
  private Realm realm;
  private UserRealm results;
  private AccessToken accessToken;

  private PublishSubject<String> onRandomBannedUntil = PublishSubject.create();

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
        realm1.where(ScoreRealm.class)
            .equalTo("user.id", userRealm.getId())
            .findAll()
            .deleteAllFromRealm();
        realm1.insertOrUpdate(userRealm);
      });
    } finally {
      obsRealm.close();
    }
  }

  @Override public void putShortcuts(List<ShortcutRealm> shortcutRealmList) {
    Realm obsRealm = Realm.getDefaultInstance();

    try {
      obsRealm.executeTransaction(realm1 -> {
        for (ShortcutRealm shortcutRealm : shortcutRealmList) {
          ShortcutRealm shortcutRealmDB =
              realm1.where(ShortcutRealm.class).equalTo("id", shortcutRealm.getId()).findFirst();
          if (shortcutRealmDB == null) {
            shortcutRealm.setMembersHash(ShortcutUtils.hashShortcut(accessToken.getUserId(),
                shortcutRealm.getMembersIdsArray()));
            realm1.insertOrUpdate(shortcutRealm);
          } else {
            updateShortcutPartially(realm1, shortcutRealm, shortcutRealmDB);
          }
        }
      });
    } finally {
      obsRealm.close();
    }
  }

  private void updateShortcutPartially(Realm tempRealm, ShortcutRealm from, ShortcutRealm to) {
    if (to == null) return;

    to.setMute(from.isMute());
    to.setStatus(from.getStatus().toUpperCase());
    to.setRead(from.isRead());
    to.setName(from.getName());
    to.setPicture(from.getPicture());
    to.setSingle(from.isSingle());
    to.setLastActivityAt(from.getLastActivityAt());
    to.setMembersHash(
        ShortcutUtils.hashShortcut(accessToken.getUserId(), from.getMembersIdsArray()));

    RealmList<ShortcutLastSeenRealm> lastSeenRealmList = new RealmList<>();
    for (ShortcutLastSeenRealm ls : from.getLastSeen()) {
      tempRealm.insertOrUpdate(ls);
      ShortcutLastSeenRealm shortcutLastSeenRealm =
          tempRealm.where(ShortcutLastSeenRealm.class).equalTo("id", ls.getId()).findFirst();
      if (shortcutLastSeenRealm != null) {
        shortcutLastSeenRealm.setUserId(ls.getUserId());
        shortcutLastSeenRealm.setDate(ls.getDate());
      }
      lastSeenRealmList.add(shortcutLastSeenRealm);
    }

    to.setLastSeen(lastSeenRealmList);

    RealmList<UserRealm> userRealmList = new RealmList<>();
    for (UserRealm member : from.getMembers()) {
      tempRealm.where(ScoreRealm.class)
          .equalTo("user.id", member.getId())
          .findAll()
          .deleteAllFromRealm();
      tempRealm.insertOrUpdate(member);
      userRealmList.add(tempRealm.where(UserRealm.class).equalTo("id", member.getId()).findFirst());
    }

    to.setMembers(userRealmList);
    to.setPinned(from.isPinned());
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

    if (from.getJsonPayloadUpdate() == null ||
        from.getJsonPayloadUpdate().has(UserRealm.MUTE_ONLINE_NOTIF)) {
      to.setMute_online_notif(from.isMute_online_notif());
    }

    if (from.getLastSeenAt() != null) to.setLastSeenAt(from.getLastSeenAt());

    if (from.getRandom_banned_until() != null) {
      to.setRandom_banned_until(from.getRandom_banned_until());
    }

    if (from.getRandom_banned_permanently() != null) {
      to.setRandom_banned_permanently(from.getRandom_banned_permanently());
    }

    if (from.getTrophy() != null) {
      to.setTrophy(from.getTrophy());
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

  @Override public Observable<List<ShortcutRealm>> singleShortcuts() {
    return realm.where(ShortcutRealm.class)
        .equalTo(ShortcutRealm.SINGLE, true)
        .equalTo(ShortcutRealm.STATUS, ShortcutRealm.DEFAULT)
        .findAll()
        .asObservable()
        .filter(singleShortcutList -> singleShortcutList.isLoaded())
        .map(singleShortcutList -> realm.copyFromRealm(singleShortcutList))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }

  @Override public Observable<List<ShortcutRealm>> shortcuts() {
    return realm.where(ShortcutRealm.class)
        .equalTo(ShortcutRealm.STATUS, ShortcutRealm.DEFAULT)
        .findAllSorted(new String[] { "pinned", "last_activity_at" },
            new Sort[] { Sort.DESCENDING, Sort.DESCENDING })
        .asObservable()
        .filter(singleShortcutList -> singleShortcutList.isLoaded())
        .map(singleShortcutList -> realm.copyFromRealm(singleShortcutList))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }

  @Override public Observable<List<ShortcutRealm>> blockedShortcuts() {
    return realm.where(ShortcutRealm.class)
        .in(ShortcutRealm.STATUS, new String[] { ShortcutRealm.BLOCKED, ShortcutRealm.HIDDEN })
        .findAll()
        .asObservable()
        .filter(singleShortcutList -> singleShortcutList.isLoaded())
        .map(singleShortcutList -> realm.copyFromRealm(singleShortcutList))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }

  @Override public UserRealm userInfosNoObs(String userId) {
    Realm obsRealm = Realm.getDefaultInstance();
    UserRealm userRealm = obsRealm.where(UserRealm.class).equalTo("id", userId).findFirst();
    final UserRealm results = userRealm == null ? null : obsRealm.copyFromRealm(userRealm);
    RealmResults<ShortcutRealm> shortcutRealmResults =
        obsRealm.where(ShortcutRealm.class).findAll();
    if (results != null) results.setShortcuts(obsRealm.copyFromRealm(shortcutRealmResults));
    obsRealm.close();
    return results;
  }

  @Override public ShortcutRealm shortcutForUserId(String userId) {
    Realm otherRealm = Realm.getDefaultInstance();
    ShortcutRealm shortcutRealm = otherRealm.where(ShortcutRealm.class)
        .equalTo(ShortcutRealm.SINGLE, true)
        .equalTo("members.id", userId)
        .findFirst();
    if (shortcutRealm != null) {
      return otherRealm.copyFromRealm(shortcutRealm);
    } else {
      return null;
    }
  }

  @Override public Observable<ShortcutRealm> shortcutForUserIds(String... userIds) {
    return realm.where(ShortcutRealm.class)
        .equalTo("membersHash", ShortcutUtils.hashShortcut(accessToken.getUserId(), userIds))
        .findAll()
        .asObservable()
        .filter(shortcutList -> shortcutList.isLoaded())
        .map(shortcutList -> {
          for (ShortcutRealm shortcutRealm : shortcutList) {
            if (shortcutRealm.isSameShortcut(userIds)) {
              return realm.copyFromRealm(shortcutRealm);
            }
          }

          return null;
        })
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }

  @Override public ShortcutRealm shortcutForUserIdsNoObs(String... userIds) {
    Realm otherRealm = Realm.getDefaultInstance();
    ShortcutRealm shortcutRealm = otherRealm.where(ShortcutRealm.class)
        .equalTo("membersHash", ShortcutUtils.hashShortcut(accessToken.getUserId(), userIds))
        .findFirst();
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
        updateShortcutPartially(realm1, shortcutRealm, shortcutRealmDB);
      });
    } finally {
      realm.close();
    }
  }

  @Override public void updateShortcutLastText(String shortcutId, String lastMessage) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        ShortcutRealm shortcutRealmDB =
            realm1.where(ShortcutRealm.class).equalTo("id", shortcutId).findFirst();
        shortcutRealmDB.setLastMessage(lastMessage);
      });
    } finally {
      realm.close();
    }
  }

  @Override public void updateShortcutLeaveOnlineUntil(String shortcutId, Date leaveOnlineUntil) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        ShortcutRealm shortcutRealmDB =
            realm1.where(ShortcutRealm.class).equalTo("id", shortcutId).findFirst();
        shortcutRealmDB.setLeaveOnlineUntil(leaveOnlineUntil);
      });
    } finally {
      realm.close();
    }
  }

  @Override public ShortcutRealm updateShortcutNoObs(String shortcutId,
      @ShortcutRealm.ShortcutStatus String status) {
    Realm otherRealm = Realm.getDefaultInstance();

    try {
      otherRealm.beginTransaction();
      ShortcutRealm shortcutRealm =
          otherRealm.where(ShortcutRealm.class).equalTo("id", shortcutId).findFirst();
      if (shortcutRealm != null) {
        shortcutRealm.setStatus(status);
      }

      otherRealm.commitTransaction();

      if (shortcutRealm != null) {
        return otherRealm.copyFromRealm(shortcutRealm);
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

  @Override public void addShortcut(final ShortcutRealm shortcutRealm) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> realm1.insertOrUpdate(shortcutRealm));
    } finally {
      realm.close();
    }
  }

  @Override public void removeShortcut(ShortcutRealm shortcutRealm) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        ShortcutRealm shortcutRealmDB =
            realm1.where(ShortcutRealm.class).equalTo("id", shortcutRealm.getId()).findFirst();
        if (shortcutRealmDB != null) shortcutRealmDB.deleteFromRealm();
      });
    } finally {
      realm.close();
    }
  }

  @Override public void updateBadgeValue(int badge) {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        BadgeRealm badgeRealm = new BadgeRealm();
        badgeRealm.setValue(badge);
        realm1.insertOrUpdate(badgeRealm);
      });
    } finally {
      realm.close();
    }
  }

  @Override public void incrementBadge() {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        BadgeRealm badgeRealm = realm1.where(BadgeRealm.class).findFirst();
        if (badgeRealm != null) badgeRealm.setValue(badgeRealm.getValue() + 1);
      });
    } finally {
      realm.close();
    }
  }

  @Override public void decrementBadge() {
    Realm realm = Realm.getDefaultInstance();

    try {
      realm.executeTransaction(realm1 -> {
        BadgeRealm badgeRealm = realm1.where(BadgeRealm.class).findFirst();
        if (badgeRealm != null) badgeRealm.setValue(Math.min(0, badgeRealm.getValue() - 1));
      });
    } finally {
      realm.close();
    }
  }

  @Override public Observable<String> getRandomBannedUntil() {
    return onRandomBannedUntil;
  }

  @Override public void putRandomBannedUntil(String date) {
    onRandomBannedUntil.onNext(date);
  }

  @Override public List<ShortcutRealm> singleShortcutsNoObs() {
    Realm realmNew = Realm.getDefaultInstance();
    List<ShortcutRealm> returnResults = new ArrayList<>();

    try {
      RealmResults<ShortcutRealm> shortcutRealmListResuls = realmNew.where(ShortcutRealm.class)
          .equalTo(ShortcutRealm.SINGLE, true)
          .equalTo(ShortcutRealm.STATUS, ShortcutRealm.DEFAULT)
          .findAll();

      if (shortcutRealmListResuls != null) {
        returnResults.addAll(realmNew.copyFromRealm(shortcutRealmListResuls));
      }
    } catch (RealmException ex) {
      Timber.e(ex);
    } finally {
      realmNew.close();
    }

    return returnResults;
  }
}
