package com.tribe.app.data.repository.user.datasource;

import android.util.Pair;
import com.tribe.app.data.cache.ContactCache;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.entity.LinkIdResult;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.RecipientRealmInterface;
import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.realm.UserPlayingRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import rx.Observable;

/**
 * {@link UserDataStore} implementation based on connections to the database.
 */
public class DiskUserDataStore implements UserDataStore, LiveDataStore {

  private final UserCache userCache;
  private LiveCache liveCache;
  private final ContactCache contactCache;
  private final AccessToken accessToken;

  /**
   * Construct a {@link UserDataStore} based on the database.
   *
   * @param userCache A {@link UserCache} to retrieve the data.
   * @param accessToken A {@link AccessToken} that contains the current user id.
   * @param contactCache A {@link ContactCache} that contains the cached data of the user's
   * possible
   * contacts
   */
  public DiskUserDataStore(UserCache userCache, LiveCache liveCache, AccessToken accessToken,
      ContactCache contactCache) {
    this.userCache = userCache;
    this.liveCache = liveCache;
    this.accessToken = accessToken;
    this.contactCache = contactCache;
  }

  @Override public Observable<PinRealm> requestCode(String phoneNumber, boolean shouldCall) {
    return null;
  }

  @Override public Observable<AccessToken> login(LoginEntity loginEntity) {
    return null;
  }

  @Override public Observable<AccessToken> register(String displayName, String username,
      LoginEntity loginEntity) {
    return null;
  }

  @Override public Observable<UserRealm> userInfos(String userId) {
    return Observable.combineLatest(this.userCache.userInfos(accessToken.getUserId()),
        Observable.combineLatest(this.userCache.shortcuts(), liveCache.onlineMap(),
            liveCache.liveMap(), liveCache.playingMap(),
            (shortcutRealms, onlineMap, liveMap, playingMap) -> shortcutRealms)
            .compose(listShortcutOnlineLiveTransformer), (userRealm, shortcutRealmList) -> {
          userRealm.setShortcuts(shortcutRealmList);
          return userRealm;
        }).doOnError(throwable -> throwable.printStackTrace());
  }

  @Override public Observable<List<UserRealm>> userInfosList(List<String> userIds) {
    return null;
  }

  @Override public Observable<Installation> createOrUpdateInstall(String token) {
    return null;
  }

  @Override public Observable<Installation> removeInstall() {
    return null;
  }

  @Override public Observable<UserRealm> updateUser(List<Pair<String, String>> values) {
    return null;
  }

  @Override public Observable<LinkIdResult> updateUserFacebook(String accessToken) {
    return null;
  }

  @Override
  public Observable<LinkIdResult> updateUserPhoneNumber(String accessToken, String phoneNumber) {
    return null;
  }

  @Override public Observable<Void> incrUserTimeInCall(String userId, Long timeInCall) {
    return null;
  }

  @Override public Observable<List<ContactInterface>> contacts() {
    return contactCache.contacts().map(contactABRealms -> new ArrayList<>(contactABRealms));
  }

  @Override public Observable<List<ContactInterface>> contactsFB() {
    return contactCache.contactsFB().map(contactFBRealms -> new ArrayList<>(contactFBRealms));
  }

  @Override public Observable<List<ContactInterface>> contactsFBInvite() {
    return contactCache.contactsFBInvite().map(contactFBRealms -> new ArrayList<>(contactFBRealms));
  }

  @Override public Observable<List<ContactInterface>> contactsOnApp() {
    return contactCache.contactsOnApp();
  }

  @Override public Observable<List<ContactInterface>> contactsToInvite() {
    return contactCache.contactsToInvite()
        .map(contactABRealms -> new ArrayList<ContactInterface>(contactABRealms));
  }

  @Override public Observable<SearchResultRealm> findByUsername(String username) {
    return contactCache.findContactByUsername(username);
  }

  @Override public Observable<Boolean> lookupUsername(String username) {
    return null;
  }

  @Override public Observable<List<ContactABRealm>> findByValue(String username) {
    return contactCache.findContactsByValue(username);
  }

  @Override public Observable<Void> notifyFBFriends() {
    return null;
  }

  @Override public Observable<String> getHeadDeepLink(String url) {
    return null;
  }

  @Override public Observable<RecipientRealmInterface> getRecipientInfos(String recipientId) {
    return null;
  }

  @Override public Observable<Map<String, Boolean>> onlineMap() {
    return liveCache.onlineMap();
  }

  @Override public Observable<Map<String, UserPlayingRealm>> playingMap() {
    return liveCache.playingMap();
  }

  @Override public Observable<Map<String, Boolean>> liveMap() {
    return liveCache.liveMap();
  }

  @Override public Observable<User> getFbIdUpdated() {
    return liveCache.getFbIdUpdated();
  }

  @Override public Observable<Room> getRoomUpdated(String roomId) {
    return null;
  }

  @Override public Observable<Void> sendInvitations() {
    return null;
  }

  @Override public Observable<Boolean> reportUser(String userId, String imageUrl) {
    return null;
  }

  @Override public Observable<ShortcutRealm> createShortcut(String[] userIds) {
    return null;
  }

  @Override public Observable<ShortcutRealm> updateShortcut(String shortcutId,
      List<Pair<String, String>> values) {
    return null;
  }

  @Override public Observable<Void> removeShortcut(String shortcutId) {
    return null;
  }

  @Override public Observable<List<ShortcutRealm>> singleShortcuts() {
    return Observable.combineLatest(userCache.singleShortcuts(), onlineMap(), liveMap(),
        (shortcutRealmList, onlineMap, liveMap) -> shortcutRealmList)
        .compose(listShortcutOnlineLiveTransformer);
  }

  @Override public Observable<List<ShortcutRealm>> shortcuts() {
    return Observable.combineLatest(userCache.shortcuts(), onlineMap(), liveMap(),
        (shortcutRealmList, onlineMap, liveMap) -> shortcutRealmList)
        .compose(listShortcutOnlineLiveTransformer);
  }

  @Override public Observable<ShortcutRealm> shortcutForUserIds(String... userIds) {
    return Observable.combineLatest(userCache.shortcutForUserIds(userIds), onlineMap(), liveMap(),
        (shortcutRealmList, onlineMap, liveMap) -> shortcutRealmList).
        compose(shortcutOnlineLiveTransformer);
  }

  public Observable<ShortcutRealm> shortcutForUserIdsNoObs(String... userIds) {
    return Observable.combineLatest(Observable.just(userCache.shortcutForUserIdsNoObs(userIds)),
        onlineMap(), liveMap(), (shortcutRealmList, onlineMap, liveMap) -> shortcutRealmList)
        .compose(shortcutOnlineLiveTransformer)
        .doOnError(Throwable::printStackTrace);
  }

  @Override public Observable<List<ShortcutRealm>> blockedShortcuts() {
    return Observable.combineLatest(userCache.blockedShortcuts(), onlineMap(),
        (shortcutRealmList, onlineMap) -> shortcutRealmList)
        .compose(listShortcutOnlineLiveTransformer);
  }

  @Override public List<Invite> invites() {
    return new ArrayList<>(liveCache.getInviteMap().values());
  }

  @Override public Observable<String> getRandomBannedUntil() {
    return userCache.getRandomBannedUntil();
  }

  @Override public Observable<Map<String, Invite>> inviteMap() {
    return liveCache.inviteMap();
  }

  private Observable.Transformer<List<ShortcutRealm>, List<ShortcutRealm>>
      listShortcutOnlineLiveTransformer =
      shortcutRealmObservable -> shortcutRealmObservable.map(shortcutRealmList -> {
        for (ShortcutRealm shortcutRealm : shortcutRealmList) {
          transformOnlineShortcut(shortcutRealm);
          transformLiveShortcut(shortcutRealm);
          transformPlayingShortcut(shortcutRealm);
        }

        return shortcutRealmList;
      });

  private Observable.Transformer<ShortcutRealm, ShortcutRealm> shortcutOnlineLiveTransformer =
      shortcutRealmObservable -> shortcutRealmObservable.map(shortcutRealm -> {
        transformOnlineShortcut(shortcutRealm);
        transformLiveShortcut(shortcutRealm);
        transformPlayingShortcut(shortcutRealm);
        return shortcutRealm;
      });

  private void transformOnlineShortcut(ShortcutRealm shortcutRealm) {
    Map<String, Boolean> onlineMap = liveCache.getOnlineMap();
    if (shortcutRealm != null) {
      shortcutRealm.computeMembersOnline(onlineMap);
      shortcutRealm.setOnline(liveCache.getOnlineMap().containsKey(shortcutRealm.getId()) ||
          shortcutRealm.isUniqueMemberOnline());
    }
  }

  private void transformPlayingShortcut(ShortcutRealm shortcutRealm) {
    Map<String, UserPlayingRealm> playingMap = liveCache.getPlayingMap();
    if (shortcutRealm != null) {
      shortcutRealm.computeMembersPlaying(playingMap);
    }
  }

  private void transformLiveShortcut(ShortcutRealm shortcutRealm) {
    Map<String, Boolean> liveMap = liveCache.getLiveMap();
    if (shortcutRealm != null) {
      shortcutRealm.computeMembersLive(liveMap);
      shortcutRealm.setLive(liveMap.containsKey(shortcutRealm.getId()));
    }
  }
}
