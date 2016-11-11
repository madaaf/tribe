package com.tribe.app.data.realm;

import java.util.Date;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 22/09/2016.
 */
public class MembershipRealm extends RealmObject implements RecipientRealmInterface {

    @PrimaryKey
    private String id;

    private GroupRealm group;
    private boolean mute;
    private String category;
    private boolean is_admin;
    private String link;
    private Date link_expires_at;
    private Date created_at;
    private Date updated_at;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GroupRealm getGroup() {
        return group;
    }

    public void setGroup(GroupRealm group) {
        this.group = group;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAdmin() {
        return is_admin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.is_admin = isAdmin;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getLink_expires_at() {
        return link_expires_at;
    }

    public void setLink_expires_at(Date link_expires_at) {
        this.link_expires_at = link_expires_at;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
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

    @Override
    public String getSubId() {
        return group.getId();
    }

    public List<String> getMembersPic() {
        return group.getMembersPics();
    }

    public String getPicture() {
        return group.getPicture();
    }
}
