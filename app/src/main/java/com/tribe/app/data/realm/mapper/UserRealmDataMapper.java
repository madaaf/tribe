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
    MembershipRealmDataMapper membershipRealmDataMapper;
    FriendshipRealmDataMapper friendshipRealmDataMapper;

    @Inject
    public UserRealmDataMapper(LocationRealmDataMapper locationRealmDataMapper) {
        this.locationRealmDataMapper = locationRealmDataMapper;
        this.membershipRealmDataMapper = new MembershipRealmDataMapper(new GroupRealmDataMapper(this));
        this.friendshipRealmDataMapper = new FriendshipRealmDataMapper(this);
    }

    /**
     * Transform a {@link com.tribe.app.data.realm.UserRealm} into an {@link com.tribe.app.domain.entity.User}.
     *
     * @param userRealm Object to be transformed.
     * @return {@link com.tribe.app.domain.entity.User} if valid {@link com.tribe.app.data.realm.UserRealm} otherwise null.
     */
    public User transform(UserRealm userRealm, boolean shouldTransformFriendships) {
        User user = null;
        if (userRealm != null) {
            user = new User(userRealm.getId());
            user.setCreatedAt(userRealm.getCreatedAt());
            user.setUpdatedAt(userRealm.getUpdatedAt());
            user.setDisplayName(userRealm.getDisplayName());
            user.setUsername(userRealm.getUsername());
            user.setProfilePicture(userRealm.getProfilePicture());
            user.setScore(userRealm.getScore());
            user.setInvisibleMode(userRealm.isInvisibleMode());
            user.setPhone(userRealm.getPhone());
            user.setFbid(userRealm.getFbid());
            if (userRealm.getLocation() != null) user.setLocation(locationRealmDataMapper.transform(userRealm.getLocation()));
            user.setTribeSave(userRealm.isTribeSave());
            if (shouldTransformFriendships) {
                if (userRealm.getMemberships() != null)
                    user.setMembershipList(membershipRealmDataMapper.transform(userRealm.getMemberships()));
                if (userRealm.getFriendships() != null)
                    user.setFriendships(friendshipRealmDataMapper.transform(userRealm.getFriendships()));
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
    public List<User> transform(Collection<UserRealm> userRealmCollection, boolean shouldTransformFriendships) {
        List<User> userList = new ArrayList<>();
        User user;
        if (userRealmCollection != null) {
            for (UserRealm userRealm : userRealmCollection) {
                user = transform(userRealm, shouldTransformFriendships);
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
    public UserRealm transform(User user, boolean shouldTransformFriendships) {
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
            userRealm.setInvisibleMode(user.isInvisibleMode());
            userRealm.setFbid(user.getFbid());
            userRealm.setPhone(user.getPhone());
            if (user.getLocation() != null) userRealm.setLocation(locationRealmDataMapper.transform(user.getLocation()));
            userRealm.setTribeSave(user.isTribeSave());
            if (shouldTransformFriendships) {
                if (user.getMembershipList() != null)
                    userRealm.setMemberships(membershipRealmDataMapper.transformMemberships(user.getMembershipList()));
                if (user.getFriendships() != null)
                    userRealm.setFriendships(friendshipRealmDataMapper.transformFriendships(user.getFriendships()));
            }
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
                userRealm = transform(user, true);
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
                user = transform(userRealm, true);
                if (user != null) {
                    userList.add(user);
                }
            }
        }

        return userList;
    }

    public FriendshipRealmDataMapper getFriendshipRealmDataMapper() {
        return friendshipRealmDataMapper;
    }

    public MembershipRealmDataMapper getMembershipRealmDataMapper() {
        return membershipRealmDataMapper;
    }
}
