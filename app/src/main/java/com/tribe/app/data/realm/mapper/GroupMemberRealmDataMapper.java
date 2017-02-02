package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.GroupMemberRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.realm.RealmList;

/**
 * Created by tiago on 06/05/2016.
 */
@Singleton public class GroupMemberRealmDataMapper {

  @Inject public GroupMemberRealmDataMapper() {
  }

  /**
   * Transform a {@link UserRealm} into an {@link User}.
   *
   * @param groupMemberRealm Object to be transformed.
   * @return {@link User} if valid {@link GroupMemberRealm} otherwise null.
   */
  public User transform(GroupMemberRealm groupMemberRealm) {
    User user = null;
    if (groupMemberRealm != null) {
      user = new User(groupMemberRealm.getId());
      user.setCreatedAt(groupMemberRealm.getCreatedAt());
      user.setUpdatedAt(groupMemberRealm.getUpdatedAt());
      user.setDisplayName(groupMemberRealm.getDisplayName());
      user.setUsername(groupMemberRealm.getUsername());
      user.setProfilePicture(groupMemberRealm.getProfilePicture());
      user.setInvisibleMode(groupMemberRealm.isInvisibleMode());
    }

    return user;
  }

  /**
   * Transform a List of {@link UserRealm} into a Collection of {@link User}.
   *
   * @param groupMemberRealmCollection Object Collection to be transformed.
   * @return {@link User} if valid {@link GroupMemberRealm} otherwise null.
   */
  public List<User> transform(Collection<GroupMemberRealm> groupMemberRealmCollection) {
    List<User> userList = new ArrayList<>();
    User user;
    if (groupMemberRealmCollection != null) {
      for (GroupMemberRealm groupMemberRealm : groupMemberRealmCollection) {
        user = transform(groupMemberRealm);

        if (user != null) {
          userList.add(user);
        }
      }
    }

    return userList;
  }

  /**
   * Transform a {@link User} into an {@link GroupMemberRealm}.
   *
   * @param user Object to be transformed.
   * @return {@link GroupMemberRealm} if valid {@link User} otherwise null.
   */
  public GroupMemberRealm transform(User user) {
    GroupMemberRealm groupMemberRealm = null;

    if (user != null) {
      groupMemberRealm = new GroupMemberRealm();
      groupMemberRealm.setId(user.getId());
      groupMemberRealm.setCreatedAt(user.getCreatedAt());
      groupMemberRealm.setUpdatedAt(user.getUpdatedAt());
      groupMemberRealm.setDisplayName(user.getDisplayName());
      groupMemberRealm.setUsername(user.getUsername());
      groupMemberRealm.setProfilePicture(user.getProfilePicture());
      groupMemberRealm.setInvisibleMode(user.isInvisibleMode());
    }

    return groupMemberRealm;
  }

  /**
   * Transform a List of {@link User} into a Collection of {@link GroupMemberRealm}.
   *
   * @param userCollection Object Collection to be transformed.
   * @return {@link GroupMemberRealm} if valid {@link User} otherwise null.
   */
  public RealmList<GroupMemberRealm> transformList(Collection<User> userCollection) {
    RealmList<GroupMemberRealm> groupMemberRealmList = new RealmList<>();
    GroupMemberRealm groupMemberRealm;

    if (userCollection != null) {
      for (User user : userCollection) {
        groupMemberRealm = transform(user);
        if (groupMemberRealm != null) {
          groupMemberRealmList.add(groupMemberRealm);
        }
      }
    }

    return groupMemberRealmList;
  }
}
