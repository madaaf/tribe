package com.tribe.app.data.repository.user.datasource;

import android.util.Pair;
import com.tribe.app.data.network.entity.LinkIdResult;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.RecipientRealmInterface;
import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.User;
import java.util.List;
import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface UserDataStore {

  /**
   * Get an {@link Observable} which will emit a {@link com.tribe.app.domain.entity.Pin} containing
   * info about the code.
   *
   * @param phoneNumber The phoneNumber used to login.
   */
  Observable<PinRealm> requestCode(final String phoneNumber, boolean shouldCall);

  /**
   * Get an {@link Observable} which will emit an Access Token.
   *
   * @param loginEntity the infos for the log in
   */
  Observable<AccessToken> login(LoginEntity loginEntity);

  /**
   * Get an {@link Observable} which will emit an Access Token.
   *
   * @param displayName the full name of the user.
   * @param username the username of the user.
   * @param loginEntity the login infos needed to register (pinId, etc)
   */
  Observable<AccessToken> register(String displayName, String username, LoginEntity loginEntity);

  /**
   * Get an {@link Observable} which will emit a {@link UserRealm}
   *
   * @param userId the id of the user for which we get the info
   */
  Observable<UserRealm> userInfos(final String userId);

  Observable<List<UserRealm>> userInfosList(List<String> userIds);

  /**
   * Get an {@link Observable} which will emit a {@link User}
   *
   * @param token the token of the user for which we get the info
   */
  Observable<Installation> createOrUpdateInstall(final String token);

  /**
   * Remove the install from the server
   */
  Observable<Installation> removeInstall();

  /**
   * @return the new user value
   */
  Observable<UserRealm> updateUser(List<Pair<String, String>> values);

  Observable<LinkIdResult> updateUserFacebook(String accessToken);

  Observable<LinkIdResult> updateUserPhoneNumber(String accessToken, String phoneNumber);

  Observable<Void> incrUserTimeInCall(String userId, Long timeInCall);

  /**
   * Get an {@link Observable} which will emit a {@link List <ContactInterface>} containing infos
   * about the contacts from address book.
   */
  Observable<List<ContactInterface>> contacts();

  /**
   * Get an {@link Observable} which will emit a {@link List <ContactInterface>} containing infos
   * about the contacts from Facebook.
   */
  Observable<List<ContactInterface>> contactsFB();

  Observable<List<ContactInterface>> contactsOnApp();

  Observable<List<ContactInterface>> contactsToInvite();

  /**
   * Get an {@link Observable} which will emit a {@link SearchResultRealm} containing infos
   * about the search results.
   */
  Observable<SearchResultRealm> findByUsername(String username);

  /**
   * Get an {@link Observable} which will emit a {@link Boolean} saying whether or not the
   * username is taken
   */
  Observable<Boolean> lookupUsername(String username);

  /**
   * Get an {@link Observable} which will emit a {@link List<ContactInterface>} containing infos
   * about the contacts corresponding to the value.
   */
  Observable<List<ContactABRealm>> findByValue(String value);

  /**
   * Get an {@link Observable} which will emit nothing
   */
  Observable<Void> notifyFBFriends();

  Observable<String> getHeadDeepLink(String url);

  Observable<RecipientRealmInterface> getRecipientInfos(String recipientId);

  Observable<Void> sendInvitations();

  Observable<Boolean> reportUser(String userId);

  Observable<ShortcutRealm> createShortcut(String[] userIds);

  Observable<ShortcutRealm> updateShortcut(String shortcutId, List<Pair<String, String>> values);

  Observable<Boolean> removeShortcut(String shortcutId);

  Observable<List<ShortcutRealm>> shortcuts();

  Observable<List<ShortcutRealm>> blockedShortcuts();
}
