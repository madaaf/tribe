package com.tribe.app.presentation.view.component.live.game.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by tiago on 10/30/2017.
 */

public abstract class GameViewWithEngine extends GameView {

  private static final String ACTION_NEW_GAME = "newGame";
  private static final String ACTION_USER_GAME_OVER = "userGameOver";
  private static final String ACTION_USER_WAITING = "userWaiting";
  private static final String ACTION_SHOW_USER_LOST = "showUserLost";
  private static final String ACTION_GAME_OVER = "gameOver";

  protected GameEngine gameEngine;

  public GameViewWithEngine(@NonNull Context context) {
    super(context);
  }

  public GameViewWithEngine(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void initView(Context context) {
    super.initView(context);
  }

  /**
   * PRIVATE
   */

  protected abstract void generateEngine();

  /**
   * PUBLIC
   */

  public void start() {
    super.start();
    generateEngine();
  }

  public void stop() {
    super.stop();
    gameEngine.stop();
    dispose();
  }

  public void dispose() {

  }

  /**
   * OBSERVABLE
   */
}
