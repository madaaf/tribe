package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.domain.entity.Group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.realm.RealmList;

/**
 * Mapper class used to transform {@link com.tribe.app.data.realm.GroupRealm} (in the data layer) to {@link com.tribe.app.domain.entity.Group} in the
 * domain layer.
 */
@Singleton
public class GroupRealmDataMapper {

    @Inject
    public GroupRealmDataMapper() {}

    /**
     * Transform a {@link com.tribe.app.data.realm.GroupRealm} into an {@link com.tribe.app.domain.entity.Group}.
     *
     * @param groupRealm Object to be transformed.
     * @return {@link com.tribe.app.domain.entity.Group} if valid {@link com.tribe.app.data.realm.GroupRealm} otherwise null.
     */
    public Group transform(GroupRealm groupRealm) {
        Group group = null;
        if (groupRealm != null) {
            group = new Group(groupRealm.getId());
            group.setDisplayName(groupRealm.getName());
            group.setProfilePicture(groupRealm.getPicture());
            group.setCreatedAt(groupRealm.getCreatedAt());
            group.setUpdatedAt(groupRealm.getUpdatedAt());
        }

        return group;
    }

    /**
     * Transform a List of {@link GroupRealm} into a Collection of {@link Group}.
     *
     * @param groupRealmCollection Object Collection to be transformed.
     * @return {@link Group} if valid {@link GroupRealm} otherwise null.
     */
    public List<Group> transform(Collection<GroupRealm> groupRealmCollection) {
        List<Group> groupList = new ArrayList<>();
        Group group;

        for (GroupRealm groupRealm : groupRealmCollection) {
            group = transform(groupRealm);
            if (group != null) {
                groupList.add(group);
            }
        }

        return groupList;
    }

    /**
     * Transform a {@link Group} into an {@link GroupRealm}.
     *
     * @param group Object to be transformed.
     * @return {@link GroupRealm} if valid {@link Group} otherwise null.
     */
    public GroupRealm transform(Group group) {
        GroupRealm groupRealm = null;
        if (group != null) {
            groupRealm = new GroupRealm();
            groupRealm.setId(group.getId());
            groupRealm.setCreatedAt(group.getCreatedAt());
            groupRealm.setUpdatedAt(group.getUpdatedAt());
        }

        return groupRealm;
    }


    /**
     * Transform a List of {@link Group} into a Collection of {@link GroupRealm}.
     *
     * @param groupCollection Object Collection to be transformed.
     * @return {@link GroupRealm} if valid {@link Group} otherwise null.
     */
    public RealmList<GroupRealm> transformGroups(Collection<Group> groupCollection) {
        RealmList<GroupRealm> groupRealmList = new RealmList<>();
        GroupRealm groupRealm;

        for (Group group : groupCollection) {
            groupRealm = transform(group);
            if (groupRealm != null) {
                groupRealmList.add(groupRealm);
            }
        }

        return groupRealmList;
    }
}
