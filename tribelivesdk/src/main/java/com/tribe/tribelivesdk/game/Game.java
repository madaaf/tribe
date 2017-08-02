package com.tribe.tribelivesdk.game;

import android.content.Context;
import android.support.annotation.StringDef;
import com.tribe.tribelivesdk.entity.GameFilter;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.tribe.tribelivesdk.webrtc.TribeI420Frame;
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

  @StringDef({ GAME_POST_IT, GAME_CHALLENGE, GAME_DRAW }) public @interface GameType {
  }

  public static final String GAME_POST_IT = "post-it";
  public static final String GAME_DRAW = "draw";
  public static final String GAME_CHALLENGE = "challenges";

  protected boolean localFrameDifferent = false;
  private boolean isUserAction = false;

  // OBSERVABLE / SUBSCRIPTIONS
  protected CompositeSubscription subscriptions = new CompositeSubscription();
  protected PublishSubject<Frame> onRemoteFrame = PublishSubject.create();
  protected PublishSubject<TribeI420Frame> onLocalFrame = PublishSubject.create();

  public Game(Context context, @GameType String id, String name, int drawableRes) {
    super(context, id, name, drawableRes);
    this.localFrameDifferent = id.equals(GAME_POST_IT);
  }

  @GameType public String getId() {
    return id;
  }

  public boolean isLocalFrameDifferent() {
    return localFrameDifferent;
  }

  public abstract void apply(Frame frame);

  public abstract void onFrameSizeChange(Frame frame);

  public void setUserAction(boolean isUserAction) {
    this.isUserAction = isUserAction;
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
