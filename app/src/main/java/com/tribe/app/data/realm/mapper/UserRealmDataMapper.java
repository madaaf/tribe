package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

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
}
