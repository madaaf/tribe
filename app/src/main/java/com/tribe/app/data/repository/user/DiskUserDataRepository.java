package com.tribe.app.data.repository.user;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.mapper.TribeRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.tribe.datasource.TribeDataStore;
import com.tribe.app.data.repository.tribe.datasource.TribeDataStoreFactory;
import com.tribe.app.data.repository.user.datasource.UserDataStore;
import com.tribe.app.data.repository.user.datasource.UserDataStoreFactory;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.user.UserRepository;
import com.tribe.app.presentation.view.utils.MessageStatus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

/**
 * {@link DiskUserDataRepository} for retrieving user data.
 */
@Singleton
public class DiskUserDataRepository implements UserRepository {

    private final UserDataStoreFactory userDataStoreFactory;
    private final UserRealmDataMapper userRealmDataMapper;
    private final TribeDataStoreFactory tribeDataStoreFactory;
    private final TribeRealmDataMapper tribeRealmDataMapper;

    /**
     * Constructs a {@link UserRepository}.
     *
     * @param dataStoreFactory A factory to construct different data source implementations.
     * @param realmDataMapper {@link UserRealmDataMapper}.
     */
    @Inject
    public DiskUserDataRepository(UserDataStoreFactory dataStoreFactory,
                                  UserRealmDataMapper realmDataMapper,
                                  TribeDataStoreFactory tribeDataStoreFactory,
                                  TribeRealmDataMapper tribeRealmDataMapper) {
        this.userDataStoreFactory = dataStoreFactory;
        this.userRealmDataMapper = realmDataMapper;
        this.tribeDataStoreFactory = tribeDataStoreFactory;
        this.tribeRealmDataMapper = tribeRealmDataMapper;
    }

    @Override
    public Observable<Pin> requestCode(String phoneNumber) { return null; }

    @Override
    public Observable<AccessToken> loginWithPhoneNumber(String phoneNumber, String code, String scope) { return null; }

    @Override
    public Observable<AccessToken> loginWithUserName(String username, String password) { return null; }

    @Override
    public Observable<User> userInfos(String userId) {
        final TribeDataStore tribeDataStore = this.tribeDataStoreFactory.createDiskDataStore();
        final UserDataStore userDataStore = this.userDataStoreFactory.createDiskDataStore();
        return Observable.zip(tribeDataStore.tribes().map(collection -> tribeRealmDataMapper.transform(collection)),
                userDataStore.userInfos(null).map(userRealm -> userRealmDataMapper.transform(userRealm)),
                (tribes, user) -> {
                    List<Recipient> result = user.getFriendshipList();

                    for (Recipient recipient : result) {
                        List<Tribe> receivedTribes = new ArrayList<>();
                        List<Tribe> sentTribes = new ArrayList<>();
                        List<Tribe> errorTribes = new ArrayList<>();

                        for (Tribe tribe : tribes) {
                            if (tribe.getFrom() != null) {
                                if (!tribe.getFrom().getId().equals(user.getId()) && (tribe.isToGroup() && tribe.getTo().getId().equals(recipient.getId()))
                                        || (!tribe.isToGroup() && tribe.getFrom().getId().equals(recipient.getId()))) {
                                    receivedTribes.add(tribe);
                                } else if (tribe.getFrom().getId().equals(user.getId())
                                        && tribe.getTo().getId().equals(recipient.getId())) {
                                    if (tribe.getMessageStatus().equals(MessageStatus.STATUS_ERROR))
                                        errorTribes.add(tribe);
                                    else sentTribes.add(tribe);
                                }
                            }
                        }

                        recipient.setErrorTribes(errorTribes);
                        recipient.setReceivedTribes(receivedTribes);
                        recipient.setSentTribes(sentTribes);
                    }

                    Friendship friendship = new Friendship(user.getId());
                    friendship.setFriend(user);
                    result.add(0, friendship);

                    return user;
                }
        );
    }

    @Override
    public Observable<Installation> createOrUpdateInstall(String token) {
        return null;
    }
}
