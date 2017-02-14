package com.tribe.app.data.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 22/09/2016.
 */
public class MembershipRealm extends RealmObject implements RecipientRealmInterface {

  public static final String MUTE = "mute";

  @PrimaryKey private String id;

  private GroupRealm group;
  private boolean mute;
  private String category;
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

  @Override public String getSubId() {
    return group.getId();
  }

  public List<String> getMembersPic() {
    return group.getMembersPics();
  }

  public String getPicture() {
    return group.getPicture();
  }
}
