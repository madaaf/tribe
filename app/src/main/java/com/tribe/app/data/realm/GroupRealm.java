package com.tribe.app.data.realm;

import com.google.gson.JsonObject;
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
    private Date created_at;
    private Date updated_at;

    private RealmList<GroupMemberRealm> members;

    @Ignore
    private JsonObject jsonPayloadUpdate;

    public GroupRealm() {
        this.members = new RealmList<>();
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

    public RealmList<GroupMemberRealm> getMembers() {
        return members;
    }

    public void setMembers(RealmList<GroupMemberRealm> members) {
        this.members = members;
    }

    public List<String> getMembersPics() {
        List<String> pics = new ArrayList<>();

        if (members != null) {
            List<GroupMemberRealm> subMembers = members.subList(Math.max(members.size() - 4, 0), members.size());

            if (subMembers != null) {
                for (GroupMemberRealm user : subMembers) {
                    String url = user.getProfilePicture();
                    if (!StringUtils.isEmpty(url))
                        pics.add(url);
                }
            }
        }

        return pics;
    }

    public JsonObject getJsonPayloadUpdate() {
        return jsonPayloadUpdate;
    }

    public void setJsonPayloadUpdate(JsonObject jsonPayloadUpdate) {
        this.jsonPayloadUpdate = jsonPayloadUpdate;
    }
}
