package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.domain.entity.Group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

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
}
