package com.tribe.app.data.realm;

import com.google.gson.annotations.SerializedName;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import java.util.List;

/**
 * Created by tiago on 04/05/2016.
 */
public class GameRealm extends RealmObject {

  public static final String GAME_NATIVE = "GameNative";
  public static final String GAME_WEBV1 = "GameWebV1";
  public static final String GAME_CORONA = "GameCorona";

  @PrimaryKey private String id;

  private boolean online;
  private boolean playable;
  private boolean featured;
  private boolean has_scores;
  @SerializedName("new") private boolean isNew;
  private String title;
  private String baseline;
  private String icon;
  private String primary_color;
  private String secondary_color;
  private int plays_count;
  private String __typename;
  private String url;
  @SerializedName("data") private String dataUrl;
  private RealmList<ScoreRealm> friends_score;
  private RealmList<ScoreRealm> overall_score;
  private String emoji;
  private ScoreUserRealm friendLeaderScoreUser;
  private String logo;
  private String background;
  private RealmList<AnimationIconRealm> animated_icons;

  @Ignore private ScoreRealm friendLeader;
  @Ignore RealmList<ScoreRealm> scores;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean hasScores() {
    return has_scores;
  }

  public void setHas_scores(boolean has_scores) {
    this.has_scores = has_scores;
  }

  public boolean isOnline() {
    return online;
  }

  public void setOnline(boolean online) {
    this.online = online;
  }

  public boolean isPlayable() {
    return playable;
  }

  public void setPlayable(boolean playable) {
    this.playable = playable;
  }

  public boolean isFeatured() {
    return featured;
  }

  public void setFeatured(boolean featured) {
    this.featured = featured;
  }

  public boolean isNew() {
    return isNew;
  }

  public void setNew(boolean aNew) {
    isNew = aNew;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getBaseline() {
    return baseline;
  }

  public void setBaseline(String baseline) {
    this.baseline = baseline;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getPrimary_color() {
    return primary_color;
  }

  public void setPrimary_color(String primary_color) {
    this.primary_color = primary_color;
  }

  public String getSecondary_color() {
    return secondary_color;
  }

  public void setSecondary_color(String secondary_color) {
    this.secondary_color = secondary_color;
  }

  public int getPlays_count() {
    return plays_count;
  }

  public void setPlays_count(int plays_count) {
    this.plays_count = plays_count;
  }

  public String get__typename() {
    return __typename;
  }

  public void set__typename(String __typename) {
    this.__typename = __typename;
  }

  public String getDataUrl() {
    return dataUrl;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setDataUrl(String dataUrl) {
    this.dataUrl = dataUrl;
  }

  public RealmList<ScoreRealm> getFriends_score() {
    return friends_score;
  }

  public void setFriends_score(RealmList<ScoreRealm> friends_score) {
    this.friends_score = friends_score;
  }

  public RealmList<ScoreRealm> getOverall_score() {
    return overall_score;
  }

  public void setOverall_score(RealmList<ScoreRealm> overall_score) {
    this.overall_score = overall_score;
  }

  public RealmList<ScoreRealm> getScores() {
    return scores;
  }

  public ScoreRealm getFriendLeader() {
    return friendLeader;
  }

  public String getEmoji() {
    return emoji;
  }

  public void setEmoji(String emoji) {
    this.emoji = emoji;
  }

  public void setFriendLeaderScoreUser(ScoreUserRealm friendLeaderScoreUser) {
    this.friendLeaderScoreUser = friendLeaderScoreUser;
  }

  public ScoreUserRealm getFriendLeaderScoreUser() {
    return friendLeaderScoreUser;
  }

  public String getLogo() {
    return logo;
  }

  public void setLogo(String logo) {
    this.logo = logo;
  }

  public String getBackground() {
    return background;
  }

  public void setBackground(String background) {
    this.background = background;
  }

  public List<AnimationIconRealm> getAnimation_icons() {
    return animated_icons;
  }

  public void setAnimation_icons(RealmList<AnimationIconRealm> animation_icons) {
    this.animated_icons = animation_icons;
  }
}
