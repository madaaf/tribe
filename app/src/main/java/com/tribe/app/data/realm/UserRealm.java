package com.tribe.app.data.realm;

import android.support.annotation.StringDef;
import com.google.gson.JsonObject;
import com.tribe.app.domain.entity.Invite;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 04/05/2016.
 */
public class UserRealm extends RealmObject {

  @StringDef({ UPDATED_AT }) public @interface UserRealmAttributes {
  }

  public static final String DISPLAY_NAME = "display_name";
  public static final String USERNAME = "username";
  public static final String PROFILE_PICTURE = "picture";
  public static final String FBID = "fbid";
  public static final String INVISIBLE_MODE = "invisible_mode";
  public static final String TRIBE_SAVE = "tribe_save";
  public static final String UPDATED_AT = "updated_at";
  public static final String PUSH_NOTIF = "push_notif";
  public static final String TIME_IN_CALL = "time_in_call";

  @PrimaryKey private String id;

  private Date created_at;
  private Date updated_at;
  private String display_name;
  private String username;
  private String phone;
  private String fbid;
  private String picture;
  private LocationRealm location;
  private boolean tribe_save = false;
  private RealmList<FriendshipRealm> friendships;
  private boolean invisible_mode;
  private boolean push_notif = true;
  private Date last_seen_at;
  private long time_in_call = 0;

  @Ignore private List<Invite> invites;

  @Ignore private JsonObject jsonPayloadUpdate;

  @Ignore private boolean is_online = false;

  public UserRealm() {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getCreatedAt() {
    return created_at;
  }

  public void setCreatedAt(Date createdAt) {
    this.created_at = createdAt;
  }

  public Date getUpdatedAt() {
    return updated_at;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updated_at = updatedAt;
  }

  public String getDisplayName() {
    return display_name;
  }

  public void setDisplayName(String displayName) {
    this.display_name = displayName;
  }

  public String getProfilePicture() {
    return picture;
  }

  public void setProfilePicture(String profilePicture) {
    this.picture = profilePicture;
  }

  public LocationRealm getLocation() {
    return location;
  }

  public void setLocation(LocationRealm location) {
    this.location = location;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public RealmList<FriendshipRealm> getFriendships() {
    return friendships;
  }

  public void setFriendships(RealmList<FriendshipRealm> friendships) {
    this.friendships = friendships;
  }

  public String getFbid() {
    return fbid;
  }

  public void setFbid(String fbid) {
    this.fbid = fbid;
  }

  public boolean isInvisibleMode() {
    return invisible_mode;
  }

  public void setInvisibleMode(boolean invisibleMode) {
    this.invisible_mode = invisibleMode;
  }

  public boolean isTribeSave() {
    return tribe_save;
  }

  public void setTribeSave(boolean tribeSave) {
    this.tribe_save = tribeSave;
  }

  public void setInvites(List<Invite> invites) {
    this.invites = invites;
  }

  public List<Invite> getInvites() {
    return invites;
  }

  public void setPushNotif(boolean pushNotif) {
    this.push_notif = pushNotif;
  }

  public boolean isPushNotif() {
    return push_notif;
  }

  public boolean isOnline() {
    return is_online;
  }

  public void setIsOnline(boolean isOnline) {
    this.is_online = isOnline;
  }

  public Date getLastSeenAt() {
    return last_seen_at;
  }

  public void setLastSeenAt(Date lastSeenAt) {
    this.last_seen_at = lastSeenAt;
  }

  public long getTimeInCall() {
    return time_in_call;
  }

  public void setTimeInCall(long time_in_call) {
    this.time_in_call = time_in_call;
  }

  public void setJsonPayloadUpdate(JsonObject jsonPayloadUpdate) {
    this.jsonPayloadUpdate = jsonPayloadUpdate;
  }

  public JsonObject getJsonPayloadUpdate() {
    return jsonPayloadUpdate;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof UserRealm)) return false;

    UserRealm that = (UserRealm) o;

    return id != null ? id.equals(that.getId()) : that.getId() == null;
  }
}
