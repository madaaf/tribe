package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
            tribe = new Tribe();
            tribe.setId(tribeRealm.getId());
            tribe.setLocalId(tribeRealm.getLocalId());
            tribe.setTo(tribeRealm.getGroup() != null ? groupRealmDataMapper.transform(tribeRealm.getGroup()) : userRealmDataMapper.transformFromTribeUser(tribeRealm.getUser()));
            tribe.setType(tribeRealm.getType());
            tribe.setFrom(userRealmDataMapper.transformFromTribeUser(tribeRealm.getFrom()));
            tribe.setRecordedAt(tribeRealm.getRecordedAt());
            tribe.setToGroup(tribeRealm.isToGroup());
            tribe.setLat(tribeRealm.getLat());
            tribe.setLng(tribeRealm.getLng());
            tribe.setUrl(tribeRealm.getUrl());
            tribe.setMessageStatus(tribeRealm.getMessageStatus());
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
            tribeRealm.setLocalId(tribe.getLocalId());

            if (tribe.isToGroup()) {
                tribeRealm.setGroup(groupRealmDataMapper.transform((Group) tribe.getTo()));
            } else {
                tribeRealm.setUser(userRealmDataMapper.transformToTribeUser((User) tribe.getTo()));
            }

            tribeRealm.setType(tribe.getType());
            tribeRealm.setRecordedAt(tribe.getRecordedAt());
            tribeRealm.setFrom(userRealmDataMapper.transformToTribeUser(tribe.getFrom()));
            tribeRealm.setLat(tribe.getLat());
            tribeRealm.setLng(tribe.getLng());
            tribeRealm.setUrl(tribe.getUrl());
            tribeRealm.setMessageStatus(tribe.getMessageStatus());
        }

        return tribeRealm;
    }

    /**
     * Transform a List of {@link TribeRealm} into a Collection of {@link Tribe}.
     *
     * @param tribeRealmCollection Object Collection to be transformed.
     * @return {@link List<Tribe>} if valid {@link List<TribeRealm>} otherwise empty list.
     */
    public List<Tribe> transform(Collection<TribeRealm> tribeRealmCollection) {
        List<Tribe> tribeList = new ArrayList<>();
        Tribe tribe;
        for (TribeRealm tribeRealm : tribeRealmCollection) {
            tribe = transform(tribeRealm);
            if (tribe != null) {
                tribeList.add(tribe);
            }
        }

        return tribeList;
    }
}
