package com.tribe.app.data.repository.tribe;

import com.tribe.app.data.realm.mapper.TribeRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.tribe.datasource.TribeDataStore;
import com.tribe.app.data.repository.tribe.datasource.TribeDataStoreFactory;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.interactor.tribe.TribeRepository;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

/**
 * {@link DiskTribeDataRepository} for retrieving user data.
 */
@Singleton
public class DiskTribeDataRepository implements TribeRepository {

    private final TribeDataStoreFactory tribeDataStoreFactory;
    private final TribeRealmDataMapper tribeRealmDataMapper;

    /**
     * Constructs a {@link TribeRepository}.
     *
     * @param tribeDataStoreFactory A factory to construct different data source implementations.
     * @param tribeRealmDataMapper {@link UserRealmDataMapper}.
     */
    @Inject
    public DiskTribeDataRepository(TribeDataStoreFactory tribeDataStoreFactory,
                                   TribeRealmDataMapper tribeRealmDataMapper) {
        this.tribeDataStoreFactory = tribeDataStoreFactory;
        this.tribeRealmDataMapper = tribeRealmDataMapper;
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
    public Observable<Map<Friendship, List<Tribe>>> tribes() {
        return null;
        //final TribeDataStore tribeDataStore = this.tribeDataStoreFactory.createDiskDataStore();
        //return tribeDataStore.tribes().map(tribeRealmCollection -> tribeRealmDataMapper.transform(tribeRealmCollection));
    }
}
