package com.tribe.app.data.realm;

import com.tribe.app.presentation.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 30/05/2016.
 */
public class GroupRealm extends RealmObject {

    public static final String PUBLIC = "PUBLIC";

    public static final String NAME = "name";
    public static final String PICTURE = "picture";

    @PrimaryKey
    private String id;

    private String name;
    private String picture;
    private String link;
    private Date created_at;
    private Date updated_at;

    @Ignore
    private RealmList<UserRealm> members;
    private RealmList<GroupMemberRealm> memberIdList;

    @Ignore
    private RealmList<UserRealm> admins;
    private RealmList<GroupMemberRealm> adminIdList;

    public GroupRealm() {
        this.members = new RealmList<>();
        this.memberIdList = new RealmList<>();
        this.admins = new RealmList<>();
        this.adminIdList = new RealmList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String groupLink) {
        this.link = groupLink;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(Date updated_at) {
        this.updated_at = updated_at;
    }

    public RealmList<UserRealm> getMembers() {
        return members;
    }

    public void setMembers(RealmList<UserRealm> members) {
        this.members = members;
    }

    public RealmList<UserRealm> getAdmins() {
        return admins;
    }

    public void setAdmins(RealmList<UserRealm> admins) {
        this.admins = admins;
    }

    public List<String> getMembersPics() {
        List<String> pics = new ArrayList<>();

        if (members != null) {
            List<UserRealm> subMembers = members.subList(Math.max(members.size() - 4, 0), members.size());

            if (subMembers != null) {
                for (UserRealm user : subMembers) {
                    String url = user.getProfilePicture();
                    if (!StringUtils.isEmpty(url))
                        pics.add(url);
                }
            }
        }

        return pics;
    }

    public void setMemberIdList(RealmList<GroupMemberRealm> memberIdList) {
        this.memberIdList = memberIdList;
    }

    public void setAdminIdList(RealmList<GroupMemberRealm> adminIdList) {
        this.adminIdList = adminIdList;
    }

    public RealmList<GroupMemberRealm> getMemberIdList() {
        return memberIdList;
    }

    public RealmList<GroupMemberRealm> getAdminIdList() {
        return adminIdList;
    }
}
