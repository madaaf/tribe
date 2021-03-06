package com.tribe.tribelivesdk.game;

import android.content.Context;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.tribe.tribelivesdk.webrtc.TribeI420Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 23/05/2017.
 */

public class Game {

  public static final String ID = "id";
  public static final String ACTION = "action";
  public static final String CONTEXT = "context";
  public static final String CHALLENGE = "challenge";
  public static final String NEW_CHALLENGE = "newChallenge";
  public static final String START = "start";
  public static final String STOP = "stop";
  public static final String CURRENT_GAME = "currentGame";

  public static final String GAME_POST_IT = "post-it";
  public static final String GAME_DRAW = "draw";
  public static final String GAME_CHALLENGE = "challenges";
  public static final String GAME_BATTLE_MUSIC = "song-pop2";
  public static final String GAME_SCREAM = "scream";
  public static final String GAME_INVADERS = "aliens-attack";
  public static final String GAME_DROP_IT = "dropit";
  public static final String GAME_SING_ALONG = "singalong";
  public static final String GAME_FACESWAP = "faceswap";
  public static final String GAME_HAND_FIGHT = "handfight";
  public static final String GAME_LAVA_FLOOR = "lavafloor";
  public static final String GAME_TABOO = "taboo";
  public static final String GAME_BACKGAMON = "backgamon";
  public static final String GAME_BEATS = "beats";
  public static final String GAME_SPEED_RACER = "speedracer";
  public static final String GAME_SLICE_FRUIT = "slicefruit";
  public static final String GAME_TRIVIA = "trivia";
  public static final String GAME_SUPPORT = "GAME_SUPPORT";
  public static final String GAME_LOGO = "GAME_LOGO";
  public static final String GAME_BIRD_RUSH = "birdRush";
  public static final String GAME_INVADERS_CORONA = "lab-aliens-attack-corona";
  public static final String GAME_BATTLE_ROYALE = "battleroyale";
  public static final String GAME_COOL_CAMS = "coolcams";

  public static final String TYPE_NAME_WEBV1 = "GameWebV1";
  public static final String TYPE_NAME_NATIVE = "GameNative";
  public static final String TYPE_NAME_CORONA = "GameCorona";

  // VARIABLE
  protected Context context;
  protected String id;
  protected boolean online;
  protected boolean playable;
  protected boolean featured;
  protected boolean hasScores;
  protected boolean isNew;
  protected String title;
  protected String baseline;
  protected String icon;
  protected String primary_color;
  protected String secondary_color;
  protected int plays_count;
  protected String __typename;
  protected String url;
  protected boolean localFrameDifferent = false, hasView = false, isOverLive = false,
      isOverLiveEmbed = false, isWeb = false, isCorona = false, isUserAction = false,
      needsLandscape = false, isNotOverLiveWithScores = false;
  protected List<TribeGuest> peerList;
  protected List<String> dataList;
  protected String previousGuestId = null;
  protected Map<String, Object> contextMap = null;
  protected TribeGuest currentMaster;
  protected TribeGuest friendLeader;
  protected String emoji;
  protected String logo;
  protected String background;
  protected List<String> animation_icons;
  protected int roundCount = 0;
  protected boolean in_home;
  protected int min_android_version;
  protected boolean uses_gamemaster;
  protected int priority;

  // OBSERVABLE / SUBSCRIPTIONS
  protected CompositeSubscription subscriptions = new CompositeSubscription();
  protected CompositeSubscription roomSubscriptions = new CompositeSubscription();
  protected PublishSubject<Frame> onRemoteFrame = PublishSubject.create();
  protected PublishSubject<TribeI420Frame> onLocalFrame = PublishSubject.create();

  public Game(Context context, String id) {
    this.context = context;
    this.id = id;
    this.localFrameDifferent = id.equals(GAME_POST_IT);
    this.hasView = !id.equals(GAME_POST_IT);
    this.isOverLive =
        id.equals(GAME_INVADERS) || id.equals(GAME_SPEED_RACER) || id.equals(GAME_SLICE_FRUIT) || id
            .equals(GAME_BIRD_RUSH) || id.equals(GAME_INVADERS_CORONA);
    this.isOverLiveEmbed = id.equals(GAME_BATTLE_ROYALE);
    this.isNotOverLiveWithScores =
        id.equals(GAME_TRIVIA) || id.equals(GAME_BATTLE_MUSIC) || id.equals(GAME_COOL_CAMS);
    this.isWeb = id.equals(GAME_SPEED_RACER) || id.equals(GAME_SLICE_FRUIT);

    if (__typename != null) {
      this.isCorona = __typename.equals(TYPE_NAME_CORONA);
    } else {
      this.isCorona = id.equals(GAME_INVADERS_CORONA) || id.equals(GAME_BATTLE_ROYALE);
    }

    this.needsLandscape = id.equals(GAME_SLICE_FRUIT);
    this.peerList = new ArrayList<>();
    this.dataList = new ArrayList<>();
    this.contextMap = new HashMap<>();
  }

  public String getId() {
    return id;
  }

  public boolean isLocalFrameDifferent() {
    return localFrameDifferent;
  }

  public boolean hasView() {
    return hasView;
  }

  public boolean isOverLive() {
    return isOverLive;
  }

  public boolean isOverLiveEmbed() {
    return isOverLiveEmbed;
  }

  public boolean isNotOverLiveWithScores() {
    return isNotOverLiveWithScores;
  }

  public boolean isWeb() {
    return isWeb;
  }

  public boolean isCorona() {
    return isCorona;
  }

  public boolean needsLandscape() {
    return needsLandscape;
  }

  public boolean hasDatas() {
    return dataList != null && dataList.size() > 0;
  }

  public void setUserAction(boolean isUserAction) {
    this.isUserAction = isUserAction;
  }

  public String getUrl() {
    return url;
  }

  public void setId(String id) {
    this.id = id;
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

  public int getMin_android_version() {
    return min_android_version;
  }

  public void setMin_android_version(int min_android_version) {
    this.min_android_version = min_android_version;
  }

  public boolean isUses_gamemaster() {
    return uses_gamemaster;
  }

  public void setUses_gamemaster(boolean uses_gamemaster) {
    this.uses_gamemaster = uses_gamemaster;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public boolean isIn_home() {
    return in_home;
  }

  public void setIn_home(boolean in_home) {
    this.in_home = in_home;
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

  public void setUrl(String url) {
    this.url = url;
  }

  public void setCurrentMaster(TribeGuest currentMaster) {
    this.currentMaster = currentMaster;
  }

  public TribeGuest getCurrentMaster() {
    return currentMaster;
  }

  public void setHasScores(boolean hasScores) {
    this.hasScores = hasScores;
  }

  public boolean hasScores() {
    return hasScores;
  }

  public void setFriendLeader(TribeGuest friendLeader) {
    this.friendLeader = friendLeader;
  }

  public TribeGuest getFriendLeader() {
    return friendLeader;
  }

  public void setEmoji(String emoji) {
    this.emoji = emoji;
  }

  public String getEmoji() {
    return emoji;
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

  public List<String> getAnimation_icons() {
    return animation_icons;
  }

  public void setAnimation_icons(List<String> animation_icons) {
    this.animation_icons = animation_icons;
  }

  protected TribeGuest getNextGuest() {
    if (peerList == null || peerList.size() == 0) return null;

    Collections.sort(peerList, (o1, o2) -> o1.getId().compareTo(o2.getId()));
    TribeGuest tribeGuest;

    if (previousGuestId == null) {
      tribeGuest = peerList.get(new Random().nextInt(peerList.size()));
      previousGuestId = tribeGuest.getId();
      return tribeGuest;
    } else {
      int index = 0;
      for (int i = 0; i < peerList.size(); i++) {
        if (peerList.get(i).getId().equals(previousGuestId)) {
          index = i + 1;
          break;
        }
      }

      if (index >= peerList.size()) index = 0;

      tribeGuest = peerList.get(index);
      previousGuestId = tribeGuest.getId();
      return tribeGuest;
    }
  }

  public void initPeerMapObservable(Observable<Map<String, TribeGuest>> peerMap) {
    roomSubscriptions.add(peerMap.subscribe(map -> {
      this.peerList.clear();
      this.peerList.addAll(map.values());
    }));
  }

  public void setDataList(Collection<String> dataList) {
    if (dataList == null) return;
    this.dataList.clear();
    this.dataList.addAll(dataList);
    generateNewDatas();
  }

  public List<String> getDataList() {
    return dataList;
  }

  public boolean isUserAction() {
    return isUserAction;
  }

  public void dispose() {
    subscriptions.clear();
  }

  public Map<String, Object> getContextMap() {
    return contextMap;
  }

  public void incrementRoundCount() {
    roundCount++;
  }

  public int getRoundCount() {
    return roundCount;
  }

  public void resetRoundCount() {
    roundCount = 0;
  }

  /**
   * TO OVERRIDE IF NEEDED
   */

  public void apply(Frame frame) {

  }

  public void onFrameSizeChange(Frame frame) {

  }

  public void generateNewDatas() {

  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getId() != null ? getId().hashCode() : 0);
    return result;
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Frame> onRemoteFrame() {
    return onRemoteFrame;
  }

  public Observable<TribeI420Frame> onLocalFrame() {
    return onLocalFrame;
  }
}
