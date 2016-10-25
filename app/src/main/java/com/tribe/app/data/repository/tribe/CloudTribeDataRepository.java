package com.tribe.app.data.repository.tribe;

import com.tribe.app.data.realm.mapper.TribeRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.tribe.datasource.TribeDataStore;
import com.tribe.app.data.repository.tribe.datasource.TribeDataStoreFactory;
import com.tribe.app.data.repository.user.datasource.UserDataStoreFactory;
import com.tribe.app.domain.entity.TribeMessage;
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
    public Observable<TribeMessage> sendTribe(TribeMessage tribe) {
        final TribeDataStore tribeDataStore = this.tribeDataStoreFactory.createCloudDataStore();
        return tribeDataStore.sendTribe(tribeRealmDataMapper.transform(tribe))
                .map(tribeRealm -> tribeRealmDataMapper.transform(tribeRealm));
    }

    @Override
    public Observable<Void> deleteTribe(TribeMessage tribe) {
        return null;
    }

    @Override
    public Observable<List<TribeMessage>> tribesNotSeen(String friendshipId) {
        return null;
    }

    @Override
    public Observable<List<TribeMessage>> tribesReceived(String friendshipId) {
        return null;
    }

    @Override
    public Observable<List<TribeMessage>> tribesForARecipient(String recipientId) {
        return null;
    }

    @Override
    public Observable<List<TribeMessage>> tribesPending() {
        return null;
    }

    @Override
    public Observable<List<TribeMessage>> markTribeListAsRead(final List<TribeMessage> tribeList) {
        final TribeDataStore tribeDataStore = this.tribeDataStoreFactory.createCloudDataStore();
        return tribeDataStore.markTribeListAsRead(tribeRealmDataMapper.transform(tribeList))
                .map(collection -> tribeRealmDataMapper.transform(collection));
    }

    @Override
    public Observable<Void> markTribeAsSave(TribeMessage tribe) {
        final TribeDataStore tribeDataStore = this.tribeDataStoreFactory.createCloudDataStore();
        return tribeDataStore.markTribeAsSave(tribeRealmDataMapper.transform(tribe));
    }

    @Override
    public Observable<Void> confirmTribe(String tribeId) {
        return null;
    }
}
