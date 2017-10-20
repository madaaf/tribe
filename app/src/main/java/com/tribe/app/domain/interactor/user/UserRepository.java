package com.tribe.app.domain.interactor.user;

/**
 * Created by tiago on 04/05/2016.
 */

import android.util.Pair;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import java.util.List;
import java.util.Set;
import rx.Observable;

/**
 * Interface that represents a Repository for getting {@link User} related data.
 */
public interface UserRepository {

  /**
   * Get an {@link Observable} which will emit a {@link com.tribe.app.domain.entity.Pin} containing
   * info about the code.
   *
   * @param phoneNumber The phoneNumber used to login.
   */
  Observable<Pin> requestCode(final String phoneNumber, boolean shouldCall);

  /**
   * Get an {@link Observable} which will emit a {@link User}.
   *
   * @param loginEntity infos needed to login.
   */
  Observable<AccessToken> login(final LoginEntity loginEntity);

  /**
   * Get an {@link Observable} which will emit an Access Token.
   *
   * @param displayName the full name of the user.
   * @param username the username of the user.
   * @param loginEntity the login infos needed to register (pinId, etc)
   */
  Observable<AccessToken> register(String displayName, String username, LoginEntity loginEntity);

  /**
   * Get an {@link Observable} which will emit a {@link User}
   *
   * @param userId the id of the user for which we get the info
   */
  Observable<User> userInfos(final String userId);

  Observable<Shortcut> getShortcuts(String shortcutId);

  Observable<List<User>> getUsersInfosList(final List<String> usersIds);

  /**
   * Get an {@link Observable} which will emit a {@link User}
   *
   * @param token the token of the user for which we get the info
   */
  Observable<Installation> createOrUpdateInstall(final String token);

  Observable<Installation> removeInstall();

  /**
   * Get an {@link Observable} which will emit a {@link User} containing infos
   * about the user updated.
   */
  Observable<User> updateUser(List<Pair<String, String>> values);

  Observable<User> updateUserFacebook(String userId, String accessToken);

  Observable<User> updateUserPhoneNumber(String userId, String accessToken, String phoneNumber);

  Observable<Void> incrUserTimeInCall(String userId, Long timeInCall);

  /**
   * Get an {@link Observable} which will emit a {@link List <Contact>} containing infos
   * about the contacts from the AddressBook.
   * If called in cloud mode it will make a synchronization of friendships see {@link
   * com.tribe.app.data.repository.user.datasource.CloudUserDataStore}
   */
  Observable<List<Contact>> contacts();

  /**
   * Get an {@link Observable} which will emit a {@link List <Contact>} containing infos
   * about the contacts from Facebook.
   */
  Observable<List<Contact>> contactsFB();

  Observable<List<Contact>> contactsFBInvite();

  Observable<List<Contact>> contactsOnApp();

  Observable<List<Contact>> contactsInvite();

  Observable<List<Shortcut>> searchLocally(String s, Set<String> includedUserIds);

  /**
   * Get an {@link Observable} which will emit a {@link SearchResult} containing infos
   * about the user searched.
   */
  Observable<SearchResult> findByUsername(String username);

  /**
   * Get an {@link Observable} which will emit a {@link Boolean} saying whether or not the
   * username is taken
   */
  Observable<Boolean> lookupUsername(String username);

  /**
   * Get an {@link Observable} which will emit a {@link Contact} containing infos
   * about the contact.
   */
  Observable<List<Contact>> findByValue(String value);

  /**
   * Get an {@link Observable} which will emit nothing
   */
  Observable<Void> notifyFBFriends();

  Observable<String> getHeadDeepLink(String url);

  Observable<Recipient> getRecipientInfos(String recipientId);

  Observable<Void> sendInvitations();

  Observable<User> getFbIdUpdated();

  Observable<Boolean> reportUser(String userId);

  /**
   * Get an {@link Observable} which will emit a {@link List<Shortcut>}
   */
  Observable<List<Shortcut>> singleShortcuts();

  Observable<List<Shortcut>> shortcuts();

  Observable<Shortcut> shortcutForUserIds(String... userIds);

  Observable<List<Shortcut>> blockedShortcuts();

  Observable<Shortcut> createShortcut(String... userIds);

  Observable<Shortcut> updateShortcut(String shortcutId, List<Pair<String, String>> values);

  Observable<Void> removeShortcut(String shortcutId);

  Observable<List<Invite>> invites();
}
