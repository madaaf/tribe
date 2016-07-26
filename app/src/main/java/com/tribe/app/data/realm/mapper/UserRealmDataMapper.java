package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.UserTribeRealm;
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

    @Inject
    public UserRealmDataMapper(LocationRealmDataMapper locationRealmDataMapper,
                               GroupRealmDataMapper groupRealmDataMapper) {
        this.locationRealmDataMapper = locationRealmDataMapper;
        this.groupRealmDataMapper = groupRealmDataMapper;
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
            user.setProfilePicture(userRealm.getProfilePicture());
            user.setScore(userRealm.getScore());
            user.setEmail(userRealm.getEmail());
            user.setEmailVerified(userRealm.isEmailVerified());
            user.setReal(userRealm.isReal());
            user.setInvited(userRealm.isInvited());
            if (userRealm.getLocation() != null) user.setLocation(locationRealmDataMapper.transform(userRealm.getLocation()));
            user.setDisableSaveTribe(userRealm.isDisableSaveTribe());
            if (userRealm.getGroups() != null) user.setGroupList(groupRealmDataMapper.transform(userRealm.getGroups()));
            if (userRealm.getFriends() != null) user.setFriendList(transform(userRealm.getFriends()));
            if (userRealm.getReported() != null) user.setReportedList(transform(userRealm.getReported()));
        }

        return user;
    }


    /**
     * Transform a {@link com.tribe.app.data.realm.UserRealm} into an {@link UserTribeRealm}.
     *
     * @param userRealm Object to be transformed.
     * @return {@link UserTribeRealm} if valid {@link com.tribe.app.data.realm.UserRealm} otherwise null.
     */
    public UserTribeRealm transformToUserTribe(UserRealm userRealm) {
        UserTribeRealm user = null;
        if (userRealm != null) {
            user = new UserTribeRealm();
            user.setId(userRealm.getId());
            user.setCreatedAt(userRealm.getCreatedAt());
            user.setUpdatedAt(userRealm.getUpdatedAt());
            user.setDisplayName(userRealm.getDisplayName());
            user.setProfilePicture(userRealm.getProfilePicture());
            user.setScore(userRealm.getScore());
            user.setReal(userRealm.isReal());
            user.setInvited(userRealm.isInvited());
            if (userRealm.getLocation() != null) user.setLocation(userRealm.getLocation());
            user.setDisableSaveTribe(userRealm.isDisableSaveTribe());
        }

        return user;
    }

    /**
     * Transform a {@link com.tribe.app.data.realm.UserRealm} into an {@link com.tribe.app.domain.entity.User}.
     *
     * @param userRealm Object to be transformed.
     * @return {@link com.tribe.app.domain.entity.User} if valid {@link com.tribe.app.data.realm.UserTribeRealm} otherwise null.
     */
    public User transformFromTribeUser(UserTribeRealm userRealm) {
        User user = null;
        if (userRealm != null) {
            user = new User(userRealm.getId());
            user.setCreatedAt(userRealm.getCreatedAt());
            user.setUpdatedAt(userRealm.getUpdatedAt());
            user.setDisplayName(userRealm.getDisplayName());
            user.setProfilePicture(userRealm.getProfilePicture());
            user.setScore(userRealm.getScore());
            user.setReal(userRealm.isReal());
            user.setInvited(userRealm.isInvited());
            if (userRealm.getLocation() != null) user.setLocation(locationRealmDataMapper.transform(userRealm.getLocation()));
            user.setDisableSaveTribe(userRealm.isDisableSaveTribe());
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
            userRealm.setCreatedAt(userRealm.getCreatedAt());
            userRealm.setUpdatedAt(userRealm.getUpdatedAt());
            userRealm.setDisplayName(userRealm.getDisplayName());
            userRealm.setProfilePicture(userRealm.getProfilePicture());
            userRealm.setScore(userRealm.getScore());
            userRealm.setEmail(userRealm.getEmail());
            userRealm.setEmailVerified(userRealm.isEmailVerified());
            userRealm.setReal(userRealm.isReal());
            userRealm.setInvited(userRealm.isInvited());
            if (user.getLocation() != null) userRealm.setLocation(locationRealmDataMapper.transform(user.getLocation()));
            userRealm.setDisableSaveTribe(userRealm.isDisableSaveTribe());
            if (user.getGroupList() != null) userRealm.setGroups(groupRealmDataMapper.transformGroups(user.getGroupList()));
            if (user.getFriendList() != null) userRealm.setFriends(transformList(user.getFriendList()));
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

    /**
     * Transform a {@link User} into an {@link UserRealm}.
     *
     * @param user Object to be transformed.
     * @return {@link UserTribeRealm} if valid {@link User} otherwise null.
     */
    public UserTribeRealm transformToTribeUser(User user) {
        UserTribeRealm userRealm = null;

        if (user != null) {
            userRealm = new UserTribeRealm();
            userRealm.setId(user.getId());
            userRealm.setCreatedAt(user.getCreatedAt());
            userRealm.setUpdatedAt(user.getUpdatedAt());
            userRealm.setDisplayName(user.getDisplayName());
            userRealm.setProfilePicture(user.getProfilePicture());
            userRealm.setScore(user.getScore());
            userRealm.setReal(user.isReal());
            userRealm.setInvited(user.isInvited());
            if (user.getLocation() != null) userRealm.setLocation(locationRealmDataMapper.transform(user.getLocation()));
            user.setDisableSaveTribe(user.isDisableSaveTribe());
        }

        return userRealm;
    }
}
