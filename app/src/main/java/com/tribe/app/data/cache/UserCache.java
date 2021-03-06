package com.tribe.app.data.cache;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.realm.UserRealm;
import java.util.Date;
import java.util.List;
import javax.inject.Singleton;
import rx.Observable;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton public interface UserCache {

  boolean isExpired();

  boolean isCached(int userId);

  void put(UserRealm userRealm);

  void putShortcuts(List<ShortcutRealm> shortcutRealmList);

  void updateCurrentUser(UserRealm userRealm);

  void incrUserTimeInCall(String userId, Long timeInCall);

  void put(AccessToken accessToken);

  void put(Installation installation);

  Observable<UserRealm> userInfos(String userId);

  Observable<List<ShortcutRealm>> singleShortcuts();

  Observable<List<ShortcutRealm>> shortcuts();

  Observable<List<ShortcutRealm>> blockedShortcuts();

  UserRealm userInfosNoObs(String userId);

  ShortcutRealm shortcutForUserId(String userId);

  Observable<ShortcutRealm> shortcutForUserIds(String... userIds);

  ShortcutRealm shortcutForUserIdsNoObs(String... userIds);

  void removeShortcut(String shortcutId);

  void updateShortcut(ShortcutRealm shortcutRealm);

  void updateShortcutLastText(String shortcutId, String lastMessage);

  void updateShortcutLeaveOnlineUntil(String shortcutId, Date leaveOnlineUntil);

  ShortcutRealm updateShortcutNoObs(String shortcutId, @ShortcutRealm.ShortcutStatus String status);

  void updateUserRealmList(List<UserRealm> userRealm);

  void addShortcut(ShortcutRealm shortcutRealm);

  void removeShortcut(ShortcutRealm shortcutRealm);

  void updateBadgeValue(int badge);

  void incrementBadge();

  void decrementBadge();

  Observable<String> getRandomBannedUntil();

  void putRandomBannedUntil(String date);

  List<ShortcutRealm> singleShortcutsNoObs();
}
