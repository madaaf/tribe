package com.tribe.tribelivesdk.game;

import android.content.Context;
import android.support.annotation.StringDef;
import com.tribe.tribelivesdk.entity.GameFilter;
import com.tribe.tribelivesdk.webrtc.Frame;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 23/05/2017.
 */

public abstract class Game extends GameFilter {

  public static final String ID = "id";
  public static final String ACTION = "action";
  public static final String START = "start";
  public static final String STOP = "stop";
  public static final String CURRENT_GAME = "currentGame";

  @StringDef({ GAME_POST_IT }) public @interface GameType {
  }

  public static final String GAME_POST_IT = "post-it";

  protected boolean localFrameDifferent = false;

  // OBSERVABLE / SUBSCRIPTIONS
  protected CompositeSubscription subscriptions = new CompositeSubscription();

  public Game(Context context, @GameType String id, String name, int drawableRes) {
    super(context, id, name, drawableRes);
    this.localFrameDifferent = id.equals(GAME_POST_IT);
  }

  @GameType public String getId() {
    return id;
  }

  public abstract void apply(Frame frame);

  public abstract void onFrameSizeChange(Frame frame);

  public int getDrawableRes() {
    return drawableRes;
  }

  public void dispose() {
    subscriptions.clear();
  }

  /////////////////
  // OBSERVABLES //
  /////////////////
}
