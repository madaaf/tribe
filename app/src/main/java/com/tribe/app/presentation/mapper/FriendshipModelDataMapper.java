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
 * Mapper class used to transform {@link com.tribe.app.domain.entity.Friendship} (in the domain layer) to {@link com.tribe.app.data.realm.FriendshipRealm} in the
 * presentation layer.
 */
@PerActivity
public class FriendshipModelDataMapper {

    UserModelDataMapper userModelDataMapper;

    @Inject
    public FriendshipModelDataMapper(UserModelDataMapper userModelDataMapper) {
        this.userModelDataMapper = userModelDataMapper;
    }

    /**
     * Transform a {@link com.tribe.app.domain.entity.Friendship} into an {@link com.tribe.app.data.realm.FriendshipRealm}.
     *
     * @param friendship Object to be transformed.
     * @return {@link com.tribe.app.data.realm.FriendshipRealm}.
     */
    public FriendshipRealm transform(Friendship friendship) {
        if (friendship == null) {
            throw new IllegalArgumentException("Cannot transform a null value");
        }

        FriendshipRealm friendshipRealm = new FriendshipRealm();
        friendshipRealm.setId(friendship.getId());
        friendshipRealm.setUpdatedAt(friendship.getUpdatedAt());
        friendshipRealm.setCreatedAt(friendship.getCreatedAt());
        friendshipRealm.setFriend(userModelDataMapper.transform(friendship.getFriend()));

        return friendshipRealm;
    }

    /**
     * Transform a Collection of {@link Friendship} into a Collection of {@link FriendshipRealm}.
     *
     * @param friendshipCollection Objects to be transformed.
     * @return List of {@link com.tribe.app.data.realm.FriendshipRealm}.
     */
    public Collection<FriendshipRealm> transform(Collection<Friendship> friendshipCollection) {
        Collection<FriendshipRealm> friendshipRealmCollection;

        if (friendshipCollection != null && !friendshipCollection.isEmpty()) {
            friendshipRealmCollection = new ArrayList<>();
            for (Friendship friendship : friendshipCollection) {
                friendshipRealmCollection.add(transform(friendship));
            }
        } else {
            friendshipRealmCollection = Collections.emptyList();
        }

        return friendshipRealmCollection;
    }
}
