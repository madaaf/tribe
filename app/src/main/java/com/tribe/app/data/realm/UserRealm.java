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

  private static final int FIFTEEN_MINUTES = 15 * 60 * 1000;

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
  public static final String MUTE_ONLINE_NOTIF = "mute_online_notif";
  public static final String TIME_IN_CALL = "time_in_call";
  public static final String AGE_RANGE_MIN = "age_range_min";
  public static final String AGE_RANGE_MAX = "age_range_max";
  public static final String TROPHY = "trophy";

  @StringDef({ NOOB, EXPERT, PRO, MASTER, GOD }) public @interface TrophyType {
  }

  public static final String NOOB = "NOOB";
  public static final String EXPERT = "EXPERT";
  public static final String PRO = "PRO";
  public static final String MASTER = "MASTER";
  public static final String GOD = "GOD";

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
  private RealmList<MessageRealm> messages;
  private RealmList<ScoreRealm> scores;
  private boolean invisible_mode;
  private boolean push_notif = true;
  private boolean mute_online_notif = false;
  private Date last_seen_at;
  private long time_in_call = 0;
  private Boolean random_banned_permanently;
  private Date random_banned_until;
  private @TrophyType String trophy = NOOB;

  @Ignore private List<Invite> invites;

  @Ignore private List<ShortcutRealm> shortcuts;

  @Ignore private JsonObject jsonPayloadUpdate;

  @Ignore private boolean is_online = false;
  @Ignore private UserPlayingRealm is_playing;
  @Ignore private boolean is_live = false;

  public UserRealm() {

  }

  public Boolean getRandom_banned_permanently() {
    return random_banned_permanently;
  }

  public void setRandom_banned_permanently(Boolean random_banned_permanently) {
    this.random_banned_permanently = random_banned_permanently;
  }

  public Date getLastSeenAt() {
    return last_seen_at;
  }

  public Date getRandom_banned_until() {
    return random_banned_until;
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

  public List<ShortcutRealm> getShortcuts() {
    return shortcuts;
  }

  public void setShortcuts(List<ShortcutRealm> shortcuts) {
    this.shortcuts = shortcuts;
  }

  public RealmList<MessageRealm> getMessages() {
    return messages;
  }

  public void setMessages(RealmList<MessageRealm> messages) {
    this.messages = messages;
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

  public void setMute_online_notif(boolean mute_online_notif) {
    this.mute_online_notif = mute_online_notif;
  }

  public boolean isMute_online_notif() {
    return mute_online_notif;
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
    if (is_online) return is_online;
    if (last_seen_at == null) return false;

    // We consider that somebody that was online less than fifteen minutes ago is still online
    return System.currentTimeMillis() - last_seen_at.getTime() <= FIFTEEN_MINUTES;
  }

  public void setIsOnline(boolean isOnline) {
    this.is_online = isOnline;
  }

  public void setIsPlaying(UserPlayingRealm is_playing) {
    this.is_playing = is_playing;
  }

  public UserPlayingRealm isPlaying() {
    return is_playing;
  }

  public boolean isLive() {
    return is_live;
  }

  public void setIsLive(boolean isLive) {
    this.is_live = isLive;
  }

  public void setLastSeenAt(Date lastSeenAt) {
    this.last_seen_at = lastSeenAt;
  }

  public void setRandom_banned_until(Date random_banned_until) {
    this.random_banned_until = random_banned_until;
  }

  public long getTimeInCall() {
    return time_in_call;
  }

  public void setTimeInCall(long time_in_call) {
    this.time_in_call = time_in_call;
  }

  public void setScores(RealmList<ScoreRealm> scores) {
    this.scores = scores;
  }

  public RealmList<ScoreRealm> getScores() {
    return scores;
  }

  public ScoreRealm getScoreForGame(String gameId) {
    if (scores == null || scores.size() == 0) return null;

    for (ScoreRealm scoreRealm : scores) {
      if (scoreRealm.getGame_id().equals(gameId)) return scoreRealm;
    }

    return null;
  }

  public void setJsonPayloadUpdate(JsonObject jsonPayloadUpdate) {
    this.jsonPayloadUpdate = jsonPayloadUpdate;
  }

  public JsonObject getJsonPayloadUpdate() {
    return jsonPayloadUpdate;
  }

  public void setTrophy(@TrophyType String trophy) {
    this.trophy = trophy;
  }

  public @TrophyType String getTrophy() {
    return trophy;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof UserRealm)) return false;

    UserRealm that = (UserRealm) o;

    return id != null ? id.equals(that.getId()) : that.getId() == null;
  }
}
