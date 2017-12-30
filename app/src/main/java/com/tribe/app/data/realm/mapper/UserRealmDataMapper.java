package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.ImageRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.chat.model.Image;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 06/05/2016.
 */
@Singleton public class UserRealmDataMapper {

  ShortcutRealmDataMapper shortcutRealmDataMapper;
  MessageRealmDataMapper messageRealmDataMapper;

  @Inject ScreenUtils screenUtils;

  @Inject public UserRealmDataMapper(ShortcutRealmDataMapper shortcutRealmDataMapper) {
    this.shortcutRealmDataMapper = shortcutRealmDataMapper;
    this.shortcutRealmDataMapper.setUserRealmDataMapper(this);
    this.messageRealmDataMapper = new MessageRealmDataMapper(this);
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
      user.setIsLive(userRealm.isLive());
      user.setLastSeenAt(userRealm.getLastSeenAt());
      user.setRandom_banned_until(userRealm.getRandom_banned_until());
      user.setMute_online_notif(userRealm.isMute_online_notif());
      if (userRealm.getRandom_banned_permanently() != null) {
        user.setRandom_banned_permanently(userRealm.getRandom_banned_permanently());
      }
      if (userRealm.getShortcuts() != null) {
        user.setShortcutList(shortcutRealmDataMapper.transform(userRealm.getShortcuts()));
      }
      if (userRealm.getMessages() != null) {
        user.setMessageList(messageRealmDataMapper.transform(userRealm.getMessages()));
      }
    }

    return user;
  }

  public Image transform(ImageRealm o) {
    Image original = null;
    if (o != null) {
      original = new Image();
      original.setUrl(o.getUrl());
      original.setWidth(o.getWidth());
      original.setHeight(o.getHeight());
      original.setFilesize(o.getFilesize());
      original.setDuration(o.getDuration());
    }
    return original;
  }

  public Image transformOriginalRealmList(List<ImageRealm> collection, boolean isAudio) {
    List<Image> originalList = new ArrayList<>();
    Image original = new Image();

    if (collection != null) {
      ImageRealm stamp = new ImageRealm();
      stamp.setUrl("STAMP");
      stamp.setWidth(String.valueOf(screenUtils.getWidthPx()));
      collection.add(stamp);

      if (!isAudio) {
        Collections.sort(collection, (o1, o2) -> {
          Integer w1 = Integer.parseInt(o1.getWidth());
          Integer w2 = Integer.parseInt(o2.getWidth());
          return w1.compareTo(w2);
        });

        int position = collection.indexOf(stamp);
        ImageRealm imageSelected;
        if (position != 0) {
          imageSelected = collection.get(position - 1);
        } else {
          imageSelected = stamp;
        }

        original = transform(imageSelected);
      } else {
        ImageRealm imageSelected = collection.get(0);
        original = transform(imageSelected);
      }
      // originalList.add(original);
      return original;
    }

    return original;
  }

  public RealmList<ImageRealm> transformOriginalList(Collection<Image> collection) {
    RealmList<ImageRealm> originalRealmList = new RealmList<>();
    ImageRealm originalRealm;

    if (collection != null) {
      for (Image original : collection) {
        originalRealm = transform(original);
        if (originalRealm != null) {
          originalRealmList.add(originalRealm);
        }
      }
    }

    return originalRealmList;
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
      userRealm.setMute_online_notif(user.isMute_online_notif());
      userRealm.setIsOnline(user.isOnline());
      userRealm.setIsLive(user.isLive());
      userRealm.setTimeInCall(user.getTimeInCall());
      userRealm.setLastSeenAt(user.getLastSeenAt());
      userRealm.setRandom_banned_until(user.getRandom_banned_until());
      userRealm.setRandom_banned_permanently(user.isRandom_banned_permanently());
      userRealm.setShortcuts(shortcutRealmDataMapper.transformList(user.getShortcutList()));

      if (user.getMessages() != null && user.getMessageList() != null) {
        userRealm.setMessages(messageRealmDataMapper.transformMessages(user.getMessageList()));
      }
    }

    return userRealm;
  }

  public ImageRealm transform(Image o) {
    ImageRealm originalRealm = null;
    if (o != null) {
      originalRealm = new ImageRealm();
      originalRealm.setFilesize(o.getFilesize());
      originalRealm.setHeight(o.getHeight());
      originalRealm.setWidth(o.getWidth());
      originalRealm.setUrl(o.getUrl());
      originalRealm.setDuration(o.getDuration());
      return originalRealm;
    }
    return null;
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

  public MessageRealmDataMapper getMessageRealmDataMapper() {
    return messageRealmDataMapper;
  }
}
