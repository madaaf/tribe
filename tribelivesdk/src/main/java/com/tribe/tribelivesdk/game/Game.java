package com.tribe.tribelivesdk.game;

import android.support.annotation.StringDef;

/**
 * Created by tiago on 23/05/2017.
 */

public class Game {

  @StringDef({ GAME_POST_IT }) public @interface GameType {
  }

  public static final String GAME_POST_IT = "GAME_POST_IT";

  private String id;
  private String name;
  private int drawableRes;
  private boolean localFrameDifferent = false;

  public Game(@GameType String id, String name, int drawableRes) {
    this.id = id;
    this.name = name;
    this.drawableRes = drawableRes;
    this.localFrameDifferent = id.equals(GAME_POST_IT);
  }

  public boolean isLocalFrameDifferent() {
    return localFrameDifferent;
  }
}
