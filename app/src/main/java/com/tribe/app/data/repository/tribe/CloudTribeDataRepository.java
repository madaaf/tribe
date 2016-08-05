package com.tribe.app.data.repository.tribe;

import com.tribe.app.data.realm.mapper.TribeRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.tribe.datasource.TribeDataStore;
import com.tribe.app.data.repository.tribe.datasource.TribeDataStoreFactory;
import com.tribe.app.data.repository.user.datasource.UserDataStoreFactory;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.interactor.tribe.TribeRepository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

/**
 * {@link CloudTribeDataRepository} for retrieving user data.
 */
@Singleton
public class CloudTribeDataRepository implements TribeRepository {

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
    public CloudTribeDataRepository(UserDataStoreFactory userDataStoreFactory,
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
        final TribeDataStore tribeDataStore = this.tribeDataStoreFactory.createCloudDataStore();
        return tribeDataStore.sendTribe(tribeRealmDataMapper.transform(tribe))
                .map(tribeRealm -> tribeRealmDataMapper.transform(tribeRealm));
    }

    @Override
    public Observable<Void> deleteTribe(Tribe tribe) {
        return null;
    }

    @Override
    public Observable<List<Tribe>> tribes() {
        final TribeDataStore tribeDataStore = this.tribeDataStoreFactory.createCloudDataStore();
        return tribeDataStore.tribes().map(collection -> {
            return tribeRealmDataMapper.transform(collection);
        });
    }

    @Override
    public Observable<List<Tribe>> tribesPending() {
        final TribeDataStore tribeDataStore = this.tribeDataStoreFactory.createCloudDataStore();
        return tribeDataStore.tribes().map(collection -> tribeRealmDataMapper.transform(collection));
    }

    @Override
    public Observable<List<Tribe>> markTribeListAsRead(final List<Tribe> tribeList) {
        final TribeDataStore tribeDataStore = this.tribeDataStoreFactory.createCloudDataStore();
        return tribeDataStore.markTribeListAsRead(tribeRealmDataMapper.transform(tribeList))
                .map(collection -> tribeRealmDataMapper.transform(collection));
    }
}
