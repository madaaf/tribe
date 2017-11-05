package com.tribe.app.presentation.view.component.live.game.common;

import android.content.Context;
import android.support.annotation.IntDef;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 02/11/2017.
 */

public class GameEngine {

  @IntDef({ PENDING, PLAYING, GAMEOVER }) public @interface PlayerStatus {
  }

  public static final int PENDING = 0;
  public static final int PLAYING = 1;
  public static final int GAMEOVER = 2;

  // VARIABLES
  protected Context context;

  // OBSERVABLES
  protected CompositeSubscription subscriptions = new CompositeSubscription();

  public GameEngine(Context context) {
    this.context = context;
  }

  /**
   * PUBLIC
   */

  public void start() {

  }

  public void stop() {
    subscriptions.clear();
  }

  /**
   * OBSERVABLES
   */
}
