package com.tribe.app.data.realm.mapper;

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
@Singleton
public class UserRealmDataMapper {

    LocationRealmDataMapper locationRealmDataMapper;
    GroupRealmDataMapper groupRealmDataMapper;
    FriendshipRealmDataMapper friendshipRealmDataMapper;

    @Inject
    public UserRealmDataMapper(LocationRealmDataMapper locationRealmDataMapper,
                               GroupRealmDataMapper groupRealmDataMapper) {
        this.locationRealmDataMapper = locationRealmDataMapper;
        this.groupRealmDataMapper = groupRealmDataMapper;
        this.friendshipRealmDataMapper = new FriendshipRealmDataMapper(this);
    }

    /**
     * Transform a {@link com.tribe.app.data.realm.UserRealm} into an {@link com.tribe.app.domain.entity.User}.
     *
     * @param userRealm Object to be transformed.
     * @return {@link com.tribe.app.domain.entity.User} if valid {@link com.tribe.app.data.realm.UserRealm} otherwise null.
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
            user.setScore(userRealm.getScore());
            user.setEmail(userRealm.getEmail());
            user.setEmailVerified(userRealm.isEmailVerified());
            user.setReal(userRealm.isReal());
            user.setInvited(userRealm.isInvited());
            user.setPhone(userRealm.getPhone());
            if (userRealm.getLocation() != null) user.setLocation(locationRealmDataMapper.transform(userRealm.getLocation()));
            user.setDisableSaveTribe(userRealm.isDisableSaveTribe());
            if (userRealm.getGroups() != null) user.setGroupList(groupRealmDataMapper.transform(userRealm.getGroups()));
            if (userRealm.getFriendships() != null) user.setFriendships(friendshipRealmDataMapper.transform(userRealm.getFriendships()));
            if (userRealm.getReported() != null) user.setReportedList(transform(userRealm.getReported()));
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
        for (UserRealm userRealm : userRealmCollection) {
            user = transform(userRealm);
            if (user != null) {
                userList.add(user);
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
            userRealm.setScore(user.getScore());
            userRealm.setEmail(user.getEmail());
            userRealm.setEmailVerified(user.isEmailVerified());
            userRealm.setReal(user.isReal());
            userRealm.setInvited(user.isInvited());
            userRealm.setPhone(user.getPhone());
            if (user.getLocation() != null) userRealm.setLocation(locationRealmDataMapper.transform(user.getLocation()));
            userRealm.setDisableSaveTribe(user.isDisableSaveTribe());
            if (user.getGroupList() != null) userRealm.setGroups(groupRealmDataMapper.transformGroups(user.getGroupList()));
            if (user.getFriendships() != null) userRealm.setFriendships(friendshipRealmDataMapper.transformFriendships(user.getFriendships()));
            if (user.getReportedList() != null) userRealm.setReported(transformList(user.getReportedList()));
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
        for (User user : userCollection) {
            userRealm = transform(user);
            if (userRealm != null) {
                userRealmList.add(userRealm);
            }
        }

        return userRealmList;
    }
}
