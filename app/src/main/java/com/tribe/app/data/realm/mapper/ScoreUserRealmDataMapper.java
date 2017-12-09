package com.tribe.app.data.realm.mapper;

import android.content.Context;
import com.tribe.app.data.realm.ScoreUserRealm;
import com.tribe.app.domain.entity.User;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Mapper class used to transform {@link ScoreUserRealm} (in the data layer)
 * to {@link User} in the
 * domain layer.
 */
@Singleton public class ScoreUserRealmDataMapper {

  private Context context;

  @Inject public ScoreUserRealmDataMapper(Context context) {
    this.context = context;
  }

  /**
   * Transform a {@link ScoreUserRealm} into an {@link User}.
   *
   * @param scoreUserRealm Object to be transformed.
   * @return {@link User} if valid {@link ScoreUserRealm} otherwise null.
   */
  public User transform(ScoreUserRealm scoreUserRealm) {
    User user = null;

    if (scoreUserRealm != null) {
      user = new User(scoreUserRealm.getId());
      user.setDisplayName(scoreUserRealm.getDisplay_name());
      user.setUsername(scoreUserRealm.getUsername());
      user.setProfilePicture(scoreUserRealm.getPicture());
    }

    return user;
  }

  /**
   * Transform a {@link User} into an {@link ScoreUserRealm}.
   *
   * @param user Object to be transformed.
   * @return {@link ScoreUserRealm} if valid {@link User} otherwise null.
   */
  public ScoreUserRealm transform(User user) {
    ScoreUserRealm scoreUserRealm = null;

    if (user != null) {
      scoreUserRealm = new ScoreUserRealm();
      scoreUserRealm.setId(user.getId());
      scoreUserRealm.setUsername(user.getUsername());
      scoreUserRealm.setDisplay_name(user.getDisplayName());
      scoreUserRealm.setPicture(user.getProfilePicture());
    }

    return scoreUserRealm;
  }

  public List<User> transform(Collection<ScoreUserRealm> scoreUserRealmCollection) {
    List<User> userList = new ArrayList<>();

    User user;
    if (scoreUserRealmCollection != null) {
      for (ScoreUserRealm scoreUserRealm : scoreUserRealmCollection) {
        user = transform(scoreUserRealm);
        if (user != null) {
          userList.add(user);
        }
      }
    }

    return userList;
  }

  public RealmList<ScoreUserRealm> transformList(Collection<User> userCollection) {
    RealmList<ScoreUserRealm> scoreUserRealmList = new RealmList<>();
    ScoreUserRealm scoreUserRealm;
    if (userCollection != null) {
      for (User user : userCollection) {
        scoreUserRealm = transform(user);
        if (scoreUserRealm != null) {
          scoreUserRealmList.add(scoreUserRealm);
        }
      }
    }

    return scoreUserRealmList;
  }
}
