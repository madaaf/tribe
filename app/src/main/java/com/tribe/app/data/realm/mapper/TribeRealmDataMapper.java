package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.entity.User;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 28/06/2016.
 */
@Singleton
public class TribeRealmDataMapper {

    LocationRealmDataMapper locationRealmDataMapper;
    GroupRealmDataMapper groupRealmDataMapper;
    UserRealmDataMapper userRealmDataMapper;

    @Inject
    public TribeRealmDataMapper(LocationRealmDataMapper locationRealmDataMapper,
                                GroupRealmDataMapper groupRealmDataMapper,
                                UserRealmDataMapper userRealmDataMapper) {
        this.locationRealmDataMapper = locationRealmDataMapper;
        this.groupRealmDataMapper = groupRealmDataMapper;
        this.userRealmDataMapper = userRealmDataMapper;
    }

    /**
     * Transform a {@link com.tribe.app.data.realm.TribeRealm} into an {@link com.tribe.app.domain.entity.Tribe}.
     *
     * @param tribeRealm Object to be transformed.
     * @return {@link com.tribe.app.domain.entity.Tribe} if valid {@link com.tribe.app.data.realm.TribeRealm} otherwise null.
     */
    public Tribe transform(TribeRealm tribeRealm) {
        Tribe tribe = null;

        if (tribeRealm != null) {
            tribe = new Tribe(tribeRealm.getId());
            tribe.setTo(tribeRealm.getToGroup() != null ? groupRealmDataMapper.transform(tribeRealm.getToGroup()) : userRealmDataMapper.transform(tribeRealm.getToUser()));
            tribe.setType(tribeRealm.getType());
            tribe.setFrom(userRealmDataMapper.transform(tribeRealm.getFrom()));
            tribe.setRecordedAt(tribeRealm.getRecordedAt());
            tribe.setToGroup(tribeRealm.getToGroup() != null);
        }

        return tribe;
    }

    /**
     * Transform a {@link Tribe} into an {@link TribeRealm}.
     *
     * @param tribe Object to be transformed.
     * @return {@link TribeRealm} if valid {@link Tribe} otherwise null.
     */
    public TribeRealm transform(Tribe tribe) {
        TribeRealm tribeRealm = null;

        if (tribe != null) {
            tribeRealm = new TribeRealm();
            tribeRealm.setId(tribe.getId());

            if (tribe.isToGroup()) {
                tribeRealm.setToGroup(groupRealmDataMapper.transform((Group) tribe.getTo()));
            } else {
                tribeRealm.setToUser(userRealmDataMapper.transform((User) tribe.getTo()));
            }

            tribeRealm.setType(tribe.getType());
            tribeRealm.setRecordedAt(tribe.getRecordedAt());
            tribeRealm.setFrom(userRealmDataMapper.transform(tribe.getFrom()));
        }

        return tribeRealm;
    }
}
