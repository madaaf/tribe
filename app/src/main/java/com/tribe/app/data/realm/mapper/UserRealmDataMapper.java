package com.tribe.app.data.realm.mapper;

import android.content.Context;
import com.tribe.app.data.realm.MediaRealm;
import com.tribe.app.data.realm.UserPlayingRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.entity.UserPlaying;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.chat.model.Media;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
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

  private ShortcutRealmDataMapper shortcutRealmDataMapper;
  private MessageRealmDataMapper messageRealmDataMapper;
  private ScoreRealmDataMapper scoreRealmDataMapper;
  private GameManager gameManager;

  @Inject ScreenUtils screenUtils;

  @Inject
  public UserRealmDataMapper(Context context, ShortcutRealmDataMapper shortcutRealmDataMapper,
      ScoreRealmDataMapper scoreRealmDataMapper) {
    this.shortcutRealmDataMapper = shortcutRealmDataMapper;
    this.shortcutRealmDataMapper.setUserRealmDataMapper(this);
    this.scoreRealmDataMapper = scoreRealmDataMapper;
    this.scoreRealmDataMapper.setUserRealmDataMapper(this);
    this.messageRealmDataMapper = new MessageRealmDataMapper(this);
    this.gameManager = GameManager.getInstance(context);
  }

  /**
   * Transform a {@link com.tribe.app.data.realm.UserRealm} into an {@link
   * com.tribe.app.domain.entity.User}.
   *
   * @param userRealm Object to be transformed.
   * @return {@link com.tribe.app.domain.entity.User} if valid {@link com.tribe.app.data.realm.UserRealm}
   * otherwise null.
   */
  public User transform(UserRealm userRealm, boolean keepScores) {
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
      user.setTrophy(userRealm.getTrophy());

      if (userRealm.isPlaying() != null) {
        UserPlayingRealm userPlayingRealm = userRealm.isPlaying();
        UserPlaying userPlaying =
            new UserPlaying(userPlayingRealm.getRoom_id(), userPlayingRealm.getGame_id());

        if (!StringUtils.isEmpty(userPlayingRealm.getGame_id())) {
          Game game = gameManager.getGameById(userPlayingRealm.getGame_id());
          if (game != null) {
            userPlaying.setEmoji(game.getEmoji());
            userPlaying.setTitle(game.getTitle());
          }
        }

        user.setPlaying(userPlaying);
      }

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

      if (userRealm.getScores() != null && keepScores) {
        user.setScoreList(scoreRealmDataMapper.transform(userRealm.getScores()));
      }

      List<String> emojis = new ArrayList<>();

      if (gameManager.getGames() != null) {
        for (Game game : gameManager.getGames()) {
          if (game.getFriendLeader() != null &&
              game.getFriendLeader().getId().equals(user.getId())) {
            emojis.add(game.getEmoji());
          }
        }
      }

      user.setEmojiLeaderGameList(emojis);
    }

    return user;
  }

  public Media transform(MediaRealm o) {
    Media original = null;
    if (o != null) {
      original = new Media();
      original.setUrl(o.getUrl());
      original.setWidth(o.getWidth());
      original.setHeight(o.getHeight());
      original.setFilesize(o.getFilesize());
      original.setDuration(o.getDuration());
    }
    return original;
  }

  public Media transformOriginalRealmList(List<MediaRealm> collection, boolean isAudio) {
    List<Media> originalList = new ArrayList<>();
    Media original = new Media();

    if (collection != null) {
      MediaRealm stamp = new MediaRealm();
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
        MediaRealm imageSelected;
        if (position != 0) {
          imageSelected = collection.get(position - 1);
        } else {
          imageSelected = stamp;
        }

        original = transform(imageSelected);
      } else {
        MediaRealm imageSelected = collection.get(0);
        original = transform(imageSelected);
      }
      // originalList.add(original);
      return original;
    }

    return original;
  }

  public RealmList<MediaRealm> transformOriginalList(Collection<Media> collection) {
    RealmList<MediaRealm> originalRealmList = new RealmList<>();
    MediaRealm originalRealm;

    if (collection != null) {
      for (Media original : collection) {
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
  public List<User> transform(Collection<UserRealm> userRealmCollection, boolean keepScores) {
    List<User> userList = new ArrayList<>();
    User user;
    if (userRealmCollection != null) {
      for (UserRealm userRealm : userRealmCollection) {
        user = transform(userRealm, keepScores);
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
  public UserRealm transform(User user, boolean keepScores) {
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
      UserPlayingRealm userPlayingRealm = new UserPlayingRealm();
      if (user.isPlaying() != null) userPlayingRealm.setGame_id(user.isPlaying().getGame_id());
      if (user.isPlaying() != null) userPlayingRealm.setRoom_id(user.isPlaying().getRoom_id());
      userRealm.setIsPlaying(userPlayingRealm);
      userRealm.setIsLive(user.isLive());
      userRealm.setTimeInCall(user.getTimeInCall());
      userRealm.setLastSeenAt(user.getLastSeenAt());
      userRealm.setRandom_banned_until(user.getRandom_banned_until());
      userRealm.setRandom_banned_permanently(user.isRandom_banned_permanently());
      userRealm.setShortcuts(shortcutRealmDataMapper.transformList(user.getShortcutList()));
      userRealm.setTrophy(user.getTrophy());

      if (user.getMessages() != null && user.getMessageList() != null) {
        userRealm.setMessages(messageRealmDataMapper.transformMessages(user.getMessageList()));
      }

      if (user.getScoreList() != null && keepScores) {
        userRealm.setScores(scoreRealmDataMapper.transformList(user.getScoreList()));
      }
    }

    return userRealm;
  }

  public MediaRealm transform(Media o) {
    MediaRealm originalRealm = null;
    if (o != null) {
      originalRealm = new MediaRealm();
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
  public RealmList<UserRealm> transformList(Collection<User> userCollection, boolean keepScores) {
    RealmList<UserRealm> userRealmList = new RealmList<>();
    UserRealm userRealm;

    if (userCollection != null) {
      for (User user : userCollection) {
        userRealm = transform(user, keepScores);
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
  public List<User> transformList(List<UserRealm> userCollection, boolean keepScores) {
    List<User> userList = new ArrayList<>();
    User user;

    if (userCollection != null) {
      for (UserRealm userRealm : userCollection) {
        user = transform(userRealm, keepScores);
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
