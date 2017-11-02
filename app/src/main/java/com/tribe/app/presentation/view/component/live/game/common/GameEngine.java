package com.tribe.app.presentation.view.component.live.game.common;

import android.content.Context;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 02/11/2017.
 */

public class GameEngine {

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
