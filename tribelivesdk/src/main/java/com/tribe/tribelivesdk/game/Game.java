package com.tribe.tribelivesdk.game;

import android.content.Context;
import android.support.annotation.StringDef;
import com.tribe.tribelivesdk.entity.GameFilter;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.tribe.tribelivesdk.webrtc.TribeI420Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 23/05/2017.
 */

public abstract class Game extends GameFilter {

  public static final String ID = "id";
  public static final String ACTION = "action";
  public static final String CHALLENGE = "challenge";
  public static final String NEW_CHALLENGE = "newChallenge";
  public static final String START = "start";
  public static final String STOP = "stop";
  public static final String CURRENT_GAME = "currentGame";

  @StringDef({
      GAME_POST_IT, GAME_CHALLENGE, GAME_DRAW, GAME_BATTLE_MUSIC, GAME_SCREAM, GAME_INVADERS,
      GAME_DROP_IT, GAME_SING_ALONG, GAME_FACESWAP, GAME_HAND_FIGHT, GAME_LAVA_FLOOR, GAME_TABOO,
      GAME_BACKGAMON, GAME_BEATS
  }) public @interface GameType {
  }

  public static final String GAME_POST_IT = "post-it";
  public static final String GAME_DRAW = "draw";
  public static final String GAME_CHALLENGE = "challenges";
  public static final String GAME_BATTLE_MUSIC = "battlemusic";
  public static final String GAME_SCREAM = "scream";
  public static final String GAME_INVADERS = "invaders";
  public static final String GAME_DROP_IT = "dropit";
  public static final String GAME_SING_ALONG = "singalong";
  public static final String GAME_FACESWAP = "faceswap";
  public static final String GAME_HAND_FIGHT = "handfight";
  public static final String GAME_LAVA_FLOOR = "lavafloor";
  public static final String GAME_TABOO = "taboo";
  public static final String GAME_BACKGAMON = "backgamon";
  public static final String GAME_BEATS = "beats";

  protected boolean localFrameDifferent = false;
  protected boolean hasView = false;
  protected boolean isOverLive = false;
  protected boolean isUserAction = false;
  protected List<TribeGuest> peerList;
  protected List<String> dataList;
  protected String previousGuestId = null;

  // OBSERVABLE / SUBSCRIPTIONS
  protected CompositeSubscription subscriptions = new CompositeSubscription();
  protected PublishSubject<Frame> onRemoteFrame = PublishSubject.create();
  protected PublishSubject<TribeI420Frame> onLocalFrame = PublishSubject.create();

  public Game(Context context, @GameType String id, String name, int drawableRes,
      boolean available) {
    super(context, id, name, drawableRes, available);
    this.localFrameDifferent = id.equals(GAME_POST_IT);
    this.hasView = !id.equals(GAME_POST_IT);
    this.isOverLive = id.equals(GAME_INVADERS);
    this.peerList = new ArrayList<>();
    this.dataList = new ArrayList<>();
  }

  @GameType public String getId() {
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

  public boolean hasDatas() {
    return dataList != null && dataList.size() > 0;
  }

  public abstract void apply(Frame frame);

  public abstract void onFrameSizeChange(Frame frame);

  public abstract void generateNewDatas();

  public void setUserAction(boolean isUserAction) {
    this.isUserAction = isUserAction;
  }

  protected TribeGuest getNextGuest() {
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

  public void setPeerList(Collection<TribeGuest> peerCollection) {
    this.peerList.clear();
    this.peerList.addAll(peerCollection);
  }

  public void setDataList(Collection<String> dataList) {
    if (dataList == null) return;
    this.dataList.clear();
    this.dataList.addAll(dataList);
    generateNewDatas();
  }

  public boolean isUserAction() {
    return isUserAction;
  }

  public int getDrawableRes() {
    return drawableRes;
  }

  public void dispose() {
    subscriptions.clear();
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
