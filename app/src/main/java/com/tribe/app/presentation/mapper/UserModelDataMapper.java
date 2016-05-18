package com.tribe.app.presentation.mapper;

import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.PerActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

/**
 * Mapper class used to transform {@link com.tribe.app.domain.entity.User} (in the domain layer) to {@link com.tribe.app.data.realm.UserRealm} in the
 * presentation layer.
 */
@PerActivity
public class UserModelDataMapper {

    LocationModelDataMapper locationModelDataMapper;

    @Inject
    public UserModelDataMapper(LocationModelDataMapper locationModelDataMapper) {

    }

    /**
     * Transform a {@link com.tribe.app.domain.entity.User} into an {@link com.tribe.app.data.realm.UserRealm}.
     *
     * @param user Object to be transformed.
     * @return {@link com.tribe.app.data.realm.UserRealm}.
     */
    public UserRealm transform(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot transform a null value");
        }

        UserRealm userRealm = new UserRealm();
        userRealm.setId(user.getId());
        userRealm.setUpdatedAt(user.getUpdatedAt());
        userRealm.setCreatedAt(user.getCreatedAt());
        userRealm.setCountryCode(user.getCountryCode());
        userRealm.setHidePinCode(user.isHidePinCode());
        userRealm.setDisableSaveTribe(user.isDisableSaveTribe());
        userRealm.setDisplayName(user.getDisplayName());
        userRealm.setEmail(user.getEmail());
        userRealm.setEmailVerified(user.isEmailVerified());
        userRealm.setInvited(user.isInvited());
        userRealm.setLocation(locationModelDataMapper.transform(user.getLocation()));
        userRealm.setPhoneNumber(user.getPhoneNumber());
        userRealm.setPinCode(user.getPinCode());
        userRealm.setProfilePicture(user.getProfilePicture());
        userRealm.setReal(user.isReal());
        userRealm.setScore(user.getScore());
        userRealm.setShouldSync(user.isShouldSync());

        return userRealm;
    }

    /**
     * Transform a Collection of {@link User} into a Collection of {@link UserRealm}.
     *
     * @param userCollection Objects to be transformed.
     * @return List of {@link UserRealm}.
     */
    public Collection<UserRealm> transform(Collection<User> userCollection) {
        Collection<UserRealm> userRealmCollection;

        if (userCollection != null && !userCollection.isEmpty()) {
            userRealmCollection = new ArrayList<>();
            for (User user : userCollection) {
                userRealmCollection.add(transform(user));
            }
        } else {
            userRealmCollection = Collections.emptyList();
        }

        return userRealmCollection;
    }
}
