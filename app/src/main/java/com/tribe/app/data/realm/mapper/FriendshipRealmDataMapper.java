package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Friendship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Mapper class used to transform {@link com.tribe.app.data.realm.FriendshipRealm} (in the data layer) to {@link com.tribe.app.domain.entity.Friendship} in the
 * domain layer.
 */
@Singleton
public class FriendshipRealmDataMapper {

    UserRealmDataMapper userRealmDataMapper;

    @Inject
    public FriendshipRealmDataMapper(UserRealmDataMapper userRealmDataMapper) {
        this.userRealmDataMapper = userRealmDataMapper;
    }

    /**
     * Transform a {@link com.tribe.app.data.realm.FriendshipRealm} into an {@link com.tribe.app.domain.entity.Friendship}.
     *
     * @param friendshipRealm Object to be transformed.
     * @return {@link com.tribe.app.domain.entity.Friendship} if valid {@link com.tribe.app.data.realm.FriendshipRealm} otherwise null.
     */
    public Friendship transform(FriendshipRealm friendshipRealm) {
        Friendship friendship = null;
        if (friendshipRealm != null) {
            friendship = new Friendship(friendshipRealm.getId());
            friendship.setCreatedAt(friendshipRealm.getCreatedAt());
            friendship.setUpdatedAt(friendshipRealm.getUpdatedAt());
            friendship.setFriend(userRealmDataMapper.transform(friendshipRealm.getFriend()));
        }

        return friendship;
    }

    /**
     * Transform a List of {@link FriendshipRealm} into a Collection of {@link Friendship}.
     *
     * @param friendshipRealmCollection Object Collection to be transformed.
     * @return {@link Friendship} if valid {@link FriendshipRealm} otherwise null.
     */
    public List<Friendship> transform(Collection<FriendshipRealm> friendshipRealmCollection) {
        List<Friendship> friendshipList = new ArrayList<>();
        Friendship friendship;
        for (FriendshipRealm friendshipRealm : friendshipRealmCollection) {
            friendship = transform(friendshipRealm);
            if (friendship != null) {
                friendshipList.add(friendship);
            }
        }

        return friendshipList;
    }
}
