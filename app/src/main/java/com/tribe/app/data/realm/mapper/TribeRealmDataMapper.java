package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.TribeMessage;

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
    MembershipRealmDataMapper membershipRealmDataMapper;
    UserRealmDataMapper userRealmDataMapper;
    WeatherRealmDataMapper weatherRealmDataMapper;
    FriendshipRealmDataMapper friendshipRealmDataMapper;
    MessageRecipientRealmDataMapper messageRecipientRealmDataMapper;

    @Inject
    public TribeRealmDataMapper(LocationRealmDataMapper locationRealmDataMapper,
                                GroupRealmDataMapper groupRealmDataMapper,
                                UserRealmDataMapper userRealmDataMapper,
                                WeatherRealmDataMapper weatherRealmDataMapper,
                                MessageRecipientRealmDataMapper messageRecipientRealmDataMapper) {
        this.locationRealmDataMapper = locationRealmDataMapper;
        this.userRealmDataMapper = userRealmDataMapper;
        this.weatherRealmDataMapper = weatherRealmDataMapper;
        this.friendshipRealmDataMapper = new FriendshipRealmDataMapper(userRealmDataMapper);
        this.membershipRealmDataMapper = new MembershipRealmDataMapper(groupRealmDataMapper);
        this.messageRecipientRealmDataMapper = messageRecipientRealmDataMapper;
    }

    /**
     * Transform a {@link com.tribe.app.data.realm.TribeRealm} into an {@link TribeMessage}.
     *
     * @param tribeRealm Object to be transformed.
     * @return {@link TribeMessage} if valid {@link com.tribe.app.data.realm.TribeRealm} otherwise null.
     */
    public TribeMessage transform(TribeRealm tribeRealm) {
        TribeMessage tribe = null;

        if (tribeRealm != null) {
            tribe = new TribeMessage();
            tribe.setId(tribeRealm.getId());
            tribe.setLocalId(tribeRealm.getLocalId());
            tribe.setTo(tribeRealm.isToGroup() ? membershipRealmDataMapper.transform(tribeRealm.getMembershipRealm()) : friendshipRealmDataMapper.transform(tribeRealm.getFriendshipRealm()));
            tribe.setType(tribeRealm.getType());
            tribe.setFrom(userRealmDataMapper.transform(tribeRealm.getFrom()));
            tribe.setRecordedAt(tribeRealm.getRecordedAt());
            tribe.setUpdatedAt(tribeRealm.getUpdatedAt());
            tribe.setToGroup(tribeRealm.isToGroup());
            tribe.setLocation(locationRealmDataMapper.transform(tribeRealm.getLocationRealm()));
            tribe.setContent(tribeRealm.getUrl());
            tribe.setMessageSendingStatus(tribeRealm.getMessageSendingStatus());
            tribe.setMessageDownloadingStatus(tribeRealm.getMessageDownloadingStatus());
            tribe.setMessageReceivingStatus(tribeRealm.getMessageReceivingStatus());
            tribe.setWeather(weatherRealmDataMapper.transform(tribeRealm.getWeatherRealm()));
            tribe.setRecipientList(messageRecipientRealmDataMapper.transform(tribeRealm.getRecipientList()));
            tribe.setTranscript(tribeRealm.getTranscript());
        }

        return tribe;
    }

    /**
     * Transform a {@link TribeMessage} into an {@link TribeRealm}.
     *
     * @param tribe Object to be transformed.
     * @return {@link TribeRealm} if valid {@link TribeMessage} otherwise null.
     */
    public TribeRealm transform(TribeMessage tribe) {
        TribeRealm tribeRealm = null;

        if (tribe != null) {
            tribeRealm = new TribeRealm();
            tribeRealm.setId(tribe.getId());
            tribeRealm.setLocalId(tribe.getLocalId());

            if (tribe.isToGroup()) {
                tribeRealm.setMembershipRealm(membershipRealmDataMapper.transform((Membership) tribe.getTo()));
            } else {
                tribeRealm.setFriendshipRealm(friendshipRealmDataMapper.transform((Friendship) tribe.getTo()));
            }

            tribeRealm.setToGroup(tribe.isToGroup());
            tribeRealm.setType(tribe.getType());
            tribeRealm.setRecordedAt(tribe.getRecordedAt());
            tribeRealm.setUpdatedAt(tribe.getUpdatedAt());
            tribeRealm.setFrom(userRealmDataMapper.transform(tribe.getFrom()));
            tribeRealm.setLocationRealm(locationRealmDataMapper.transform(tribe.getLocation()));
            tribeRealm.setUrl(tribe.getContent());
            tribeRealm.setMessageSendingStatus(tribe.getMessageSendingStatus());
            tribeRealm.setMessageDownloadingStatus(tribe.getMessageDownloadingStatus());
            tribeRealm.setMessageReceivingStatus(tribe.getMessageReceivingStatus());
            tribeRealm.setWeatherRealm(weatherRealmDataMapper.transform(tribe.getWeather()));
            tribeRealm.setRecipientList(messageRecipientRealmDataMapper.transform(tribe.getRecipientList()));
            tribeRealm.setTranscript(tribe.getTranscript());
        }

        return tribeRealm;
    }

    /**
     * Transform a List of {@link TribeRealm} into a Collection of {@link TribeMessage}.
     *
     * @param tribeRealmCollection Object Collection to be transformed.
     * @return {@link List< TribeMessage >} if valid {@link List<TribeRealm>} otherwise empty list.
     */
    public List<TribeMessage> transform(Collection<TribeRealm> tribeRealmCollection) {
        List<TribeMessage> tribeList = new ArrayList<>();
        TribeMessage tribe;
        for (TribeRealm tribeRealm : tribeRealmCollection) {
            tribe = transform(tribeRealm);
            if (tribe != null) {
                tribeList.add(tribe);
            }
        }

        return tribeList;
    }

    /**
     * Transform a List of {@link TribeMessage} into a Collection of {@link TribeRealm}.
     *
     * @param tribeCollection Object Collection to be transformed.
     * @return {@link List< TribeMessage >} if valid {@link List<TribeRealm>} otherwise empty list.
     */
    public List<TribeRealm> transform(List<TribeMessage> tribeCollection) {
        List<TribeRealm> tribeList = new ArrayList<>();
        TribeRealm tribeRealm;
        for (TribeMessage tribe : tribeCollection) {
            tribeRealm = transform(tribe);
            if (tribe != null) {
                tribeList.add(tribeRealm);
            }
        }

        return tribeList;
    }
}
