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

    UserRealmDataMapper userRealmDataMapper;

    @Inject
    public GroupRealmDataMapper(UserRealmDataMapper userRealmDataMapper) {
        this.userRealmDataMapper = userRealmDataMapper;
    }

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
            group.setName(groupRealm.getName());
            group.setPicture(groupRealm.getPicture());
            group.setGroupLink(groupRealm.getLink());
            group.setMembers(userRealmDataMapper.transform(groupRealm.getMembers()));
            group.setAdmins(userRealmDataMapper.transform(groupRealm.getAdmins()));
            group.setPrivateGroup(groupRealm.isPrivateGroup());
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
            groupRealm.setPicture(group.getPicture());
            groupRealm.setName(group.getName());
            groupRealm.setMembers(userRealmDataMapper.transformList(group.getMembers()));
            groupRealm.setAdmins(userRealmDataMapper.transformList(group.getAdmins()));
            groupRealm.setPrivateGroup(group.isPrivateGroup());
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
