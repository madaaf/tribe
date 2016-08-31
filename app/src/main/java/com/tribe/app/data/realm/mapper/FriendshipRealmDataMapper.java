package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Friendship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import io.realm.RealmList;

/**
 * Mapper class used to transform {@link FriendshipRealm} (in the data layer) to {@link Friendship} in the
 * domain layer.
 */
@Singleton
public class FriendshipRealmDataMapper {

    private UserRealmDataMapper userRealmDataMapper;

    public FriendshipRealmDataMapper(UserRealmDataMapper userRealmDataMapper) {
        this.userRealmDataMapper = userRealmDataMapper;
    }

    /**
     * Transform a {@link FriendshipRealm} into an {@link Friendship}.
     *
     * @param friendshipRealm Object to be transformed.
     * @return {@link Friendship} if valid {@link FriendshipRealm} otherwise null.
     */
    public Friendship transform(FriendshipRealm friendshipRealm) {
        Friendship friendship = null;
        if (friendshipRealm != null) {
            friendship = new Friendship(friendshipRealm.getId());
            friendship.setFriend(userRealmDataMapper.transform(friendshipRealm.getFriend()));
            friendship.setBlocked(friendshipRealm.isBlocked());
            friendship.setCategory(friendshipRealm.getCategory());
            friendship.setTag(friendshipRealm.getTag());
            friendship.setCreatedAt(friendshipRealm.getCreatedAt());
            friendship.setUpdatedAt(friendshipRealm.getUpdatedAt());
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

    /**
     * Transform a {@link Friendship} into an {@link FriendshipRealm}.
     *
     * @param friendship Object to be transformed.
     * @return {@link FriendshipRealm} if valid {@link Friendship} otherwise null.
     */
    public FriendshipRealm transform(Friendship friendship) {
        FriendshipRealm friendshipRealm = null;
        if (friendship != null) {
            friendshipRealm = new FriendshipRealm();
            friendshipRealm.setId(friendship.getFriendshipId());
            friendshipRealm.setFriend(userRealmDataMapper.transform(friendship.getFriend()));
            friendshipRealm.setBlocked(friendship.isBlocked());
            friendshipRealm.setCategory(friendship.getCategory());
            friendshipRealm.setTag(friendship.getTag());
            friendshipRealm.setCreatedAt(friendship.getCreatedAt());
            friendshipRealm.setUpdatedAt(friendship.getUpdatedAt());
        }

        return friendshipRealm;
    }


    /**
     * Transform a List of {@link Friendship} into a Collection of {@link FriendshipRealm}.
     *
     * @param friendshipCollection Object Collection to be transformed.
     * @return {@link FriendshipRealm} if valid {@link Friendship} otherwise null.
     */
    public RealmList<FriendshipRealm> transformFriendships(Collection<Friendship> friendshipCollection) {
        RealmList<FriendshipRealm> friendshipRealmList = new RealmList<>();
        FriendshipRealm friendshipRealm;

        for (Friendship friendship : friendshipCollection) {
            friendshipRealm = transform(friendship);
            if (friendshipRealm != null) {
                friendshipRealmList.add(friendshipRealm);
            }
        }

        return friendshipRealmList;
    }
}
