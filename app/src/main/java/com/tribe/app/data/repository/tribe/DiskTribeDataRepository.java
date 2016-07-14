package com.tribe.app.data.repository.tribe;

import com.tribe.app.data.realm.mapper.TribeRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.tribe.datasource.TribeDataStore;
import com.tribe.app.data.repository.tribe.datasource.TribeDataStoreFactory;
import com.tribe.app.data.repository.user.datasource.UserDataStore;
import com.tribe.app.data.repository.user.datasource.UserDataStoreFactory;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.interactor.tribe.TribeRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

/**
 * {@link DiskTribeDataRepository} for retrieving user data.
 */
@Singleton
public class DiskTribeDataRepository implements TribeRepository {

    private final UserDataStoreFactory userDataStoreFactory;
    private final TribeDataStoreFactory tribeDataStoreFactory;
    private final TribeRealmDataMapper tribeRealmDataMapper;
    private final UserRealmDataMapper userRealmDataMapper;

    /**
     * Constructs a {@link TribeRepository}.
     *
     * @param tribeDataStoreFactory A factory to construct different data source implementations.
     * @param tribeRealmDataMapper {@link UserRealmDataMapper}.
     */
    @Inject
    public DiskTribeDataRepository(UserDataStoreFactory userDataStoreFactory,
                                   TribeDataStoreFactory tribeDataStoreFactory,
                                   TribeRealmDataMapper tribeRealmDataMapper,
                                   UserRealmDataMapper userRealmDataMapper) {
        this.userDataStoreFactory = userDataStoreFactory;
        this.tribeDataStoreFactory = tribeDataStoreFactory;
        this.tribeRealmDataMapper = tribeRealmDataMapper;
        this.userRealmDataMapper = userRealmDataMapper;
    }

    @Override
    public Observable<Tribe> sendTribe(Tribe tribe) {
        final TribeDataStore tribeDataStore = this.tribeDataStoreFactory.createDiskDataStore();
        return tribeDataStore.sendTribe(tribeRealmDataMapper.transform(tribe))
                .map(tribeRealm -> tribeRealmDataMapper.transform(tribeRealm));
    }

    @Override
    public Observable<Void> deleteTribe(Tribe tribe) {
        final TribeDataStore tribeDataStore = this.tribeDataStoreFactory.createDiskDataStore();
        return tribeDataStore.deleteTribe(tribeRealmDataMapper.transform(tribe));
    }

    @Override
    public Observable<List<Friendship>> tribes() {
        final TribeDataStore tribeDataStore = this.tribeDataStoreFactory.createDiskDataStore();
        final UserDataStore userDataStore = this.userDataStoreFactory.createDiskDataStore();
        return Observable.zip(tribeDataStore.tribes().map(collection -> tribeRealmDataMapper.transform(collection)),
                userDataStore.userInfos(null).map(userRealm -> userRealmDataMapper.transform(userRealm)),
                (tribes, user) -> {
                    List<Friendship> result = user.getFriendshipList();

                    for (Friendship friendship : user.getFriendshipList()) {
                        List<Tribe> newTribes = new ArrayList<>();

                        for (Tribe tribe : tribes) {
                            if (tribe.isToGroup() && tribe.getTo().getId().equals(friendship.getId())
                                    || !tribe.isToGroup() && tribe.getFrom().getId().equals(friendship.getId())) {
                                newTribes.add(tribe);
                            }
                        }

                        friendship.setTribes(newTribes);
                    }

                    Collections.sort(result, (lhs, rhs) -> {
                        int res = Tribe.nullSafeComparator(lhs.getMostRecentTribe(), rhs.getMostRecentTribe());
                        if (res != 0) {
                            return res;
                        }

                        return Friendship.nullSafeComparator(lhs, rhs);
                    });

                    result.add(0, user);

                    return result;
                }
        );
    }
}
