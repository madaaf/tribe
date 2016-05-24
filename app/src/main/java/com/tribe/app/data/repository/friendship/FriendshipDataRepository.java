package com.tribe.app.data.repository.friendship;

import com.tribe.app.data.realm.mapper.FriendshipRealmDataMapper;
import com.tribe.app.data.repository.friendship.datasource.FriendshipDataStore;
import com.tribe.app.data.repository.friendship.datasource.FriendshipDataStoreFactory;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.interactor.friendship.FriendshipRepository;
import com.tribe.app.domain.interactor.user.UserRepository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import graphql.schema.GraphQLObjectType;
import rx.Observable;

/**
 * {@link com.tribe.app.domain.interactor.friendship.FriendshipRepository} for retrieving user data.
 */
@Singleton
public class FriendshipDataRepository implements FriendshipRepository {

    private final FriendshipDataStoreFactory friendshipDataStoreFactory;
    private final FriendshipRealmDataMapper friendshipRealmDataMapper;

    /**
     * Constructs a {@link FriendshipRepository}.
     *
     * @param dataStoreFactory           A factory to construct different data source implementations.
     * @param friendshipRealmDataMapper {@link FriendshipRealmDataMapper}.
     */
    @Inject
    public FriendshipDataRepository(FriendshipDataStoreFactory dataStoreFactory,
                                    FriendshipRealmDataMapper friendshipRealmDataMapper) {
        this.friendshipDataStoreFactory = dataStoreFactory;
        this.friendshipRealmDataMapper = friendshipRealmDataMapper;
    }

    @Override
    public Observable<List<Friendship>> friendships(int userId) {
        // We always get all users from the cloud
        final FriendshipDataStore friendshipDataStore = this.friendshipDataStoreFactory.createCloudDataStore();
        return friendshipDataStore.friendships(userId)
                .map(friendshipRealms -> this.friendshipRealmDataMapper.transform(friendshipRealms));
    }
}
