package com.tribe.tribelivesdk.game;

import android.content.Context;
import android.support.annotation.StringDef;
import com.tribe.tribelivesdk.webrtc.Frame;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 23/05/2017.
 */

public abstract class Game {

  @StringDef({ GAME_POST_IT }) public @interface GameType {
  }

  public static final String GAME_POST_IT = "GAME_POST_IT";

  protected Context context;
  protected String id;
  protected String name;
  protected int drawableRes;
  protected boolean localFrameDifferent = false;

  // OBSERVABLES
  protected PublishSubject<Frame> onRemoteFrame = PublishSubject.create();
  protected PublishSubject<Frame> onLocalFrame = PublishSubject.create();

  public Game(Context context, @GameType String id, String name, int drawableRes) {
    this.context = context;
    this.id = id;
    this.name = name;
    this.drawableRes = drawableRes;
    this.localFrameDifferent = id.equals(GAME_POST_IT);
  }

  public boolean isLocalFrameDifferent() {
    return localFrameDifferent;
  }

  public abstract void apply(Frame frame);

  public abstract void onFrameSizeChange(Frame frame);

  public int getDrawableRes() {
    return drawableRes;
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Frame> onRemoteFrame() {
    return onRemoteFrame;
  }

  public Observable<Frame> onLocalFrame() {
    return onLocalFrame;
  }
}
