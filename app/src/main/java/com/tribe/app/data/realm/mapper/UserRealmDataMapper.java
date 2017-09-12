package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.User;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 06/05/2016.
 */
@Singleton public class UserRealmDataMapper {

  LocationRealmDataMapper locationRealmDataMapper;
  ShortcutRealmDataMapper shortcutRealmDataMapper;

  @Inject public UserRealmDataMapper(LocationRealmDataMapper locationRealmDataMapper,
      ShortcutRealmDataMapper shortcutRealmDataMapper) {
    this.locationRealmDataMapper = locationRealmDataMapper;
    this.shortcutRealmDataMapper = shortcutRealmDataMapper;
    this.shortcutRealmDataMapper.setUserRealmDataMapper(this);
  }

  /**
   * Transform a {@link com.tribe.app.data.realm.UserRealm} into an {@link
   * com.tribe.app.domain.entity.User}.
   *
   * @param userRealm Object to be transformed.
   * @return {@link com.tribe.app.domain.entity.User} if valid {@link com.tribe.app.data.realm.UserRealm}
   * otherwise null.
   */
  public User transform(UserRealm userRealm) {
    User user = null;
    if (userRealm != null) {
      user = new User(userRealm.getId());
      user.setCreatedAt(userRealm.getCreatedAt());
      user.setUpdatedAt(userRealm.getUpdatedAt());
      user.setDisplayName(userRealm.getDisplayName());
      user.setUsername(userRealm.getUsername());
      user.setProfilePicture(userRealm.getProfilePicture());
      user.setInvisibleMode(userRealm.isInvisibleMode());
      user.setPhone(userRealm.getPhone());
      user.setFbid(userRealm.getFbid());
      user.setTimeInCall(userRealm.getTimeInCall());
      user.setPushNotif(userRealm.isPushNotif());
      user.setIsOnline(userRealm.isOnline());
      user.setLastSeenAt(userRealm.getLastSeenAt());
      if (userRealm.getShortcuts() != null) {
        user.setShortcutList(shortcutRealmDataMapper.transform(userRealm.getShortcuts()));
      }
    }

    return user;
  }

  /**
   * Transform a List of {@link UserRealm} into a Collection of {@link User}.
   *
   * @param userRealmCollection Object Collection to be transformed.
   * @return {@link User} if valid {@link UserRealm} otherwise null.
   */
  public List<User> transform(Collection<UserRealm> userRealmCollection) {
    List<User> userList = new ArrayList<>();
    User user;
    if (userRealmCollection != null) {
      for (UserRealm userRealm : userRealmCollection) {
        user = transform(userRealm);
        if (user != null) {
          userList.add(user);
        }
      }
    }

    return userList;
  }

  /**
   * Transform a {@link User} into an {@link UserRealm}.
   *
   * @param user Object to be transformed.
   * @return {@link UserRealm} if valid {@link User} otherwise null.
   */
  public UserRealm transform(User user) {
    UserRealm userRealm = null;

    if (user != null) {
      userRealm = new UserRealm();
      userRealm.setId(user.getId());
      userRealm.setCreatedAt(user.getCreatedAt());
      userRealm.setUpdatedAt(user.getUpdatedAt());
      userRealm.setDisplayName(user.getDisplayName());
      userRealm.setUsername(user.getUsername());
      userRealm.setProfilePicture(user.getProfilePicture());
      userRealm.setInvisibleMode(user.isInvisibleMode());
      userRealm.setFbid(user.getFbid());
      userRealm.setPhone(user.getPhone());
      userRealm.setPushNotif(user.isPushNotif());
      userRealm.setIsOnline(user.isOnline());
      userRealm.setTimeInCall(user.getTimeInCall());
      userRealm.setLastSeenAt(user.getLastSeenAt());
      userRealm.setShortcuts(shortcutRealmDataMapper.transformList(user.getShortcutList()));
    }

    return userRealm;
  }

  /**
   * Transform a List of {@link User} into a Collection of {@link UserRealm}.
   *
   * @param userCollection Object Collection to be transformed.
   * @return {@link UserRealm} if valid {@link User} otherwise null.
   */
  public RealmList<UserRealm> transformList(Collection<User> userCollection) {
    RealmList<UserRealm> userRealmList = new RealmList<>();
    UserRealm userRealm;

    if (userCollection != null) {
      for (User user : userCollection) {
        userRealm = transform(user);
        if (userRealm != null) {
          userRealmList.add(userRealm);
        }
      }
    }

    return userRealmList;
  }

  /**
   * Transform a Collection of {@link UserRealm} into a List of {@link User}.
   *
   * @param userCollection Object Collection to be transformed.
   * @return {@link User} if valid {@link UserRealm} otherwise null.
   */
  public List<User> transformList(List<UserRealm> userCollection) {
    List<User> userList = new ArrayList<>();
    User user;

    if (userCollection != null) {
      for (UserRealm userRealm : userCollection) {
        user = transform(userRealm);
        if (user != null) {
          userList.add(user);
        }
      }
    }

    return userList;
  }

  public ShortcutRealmDataMapper getShortcutRealmDataMapper() {
    return shortcutRealmDataMapper;
  }
}
