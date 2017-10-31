package com.tribe.tribelivesdk.game;

import android.content.Context;
import com.tribe.tribelivesdk.webrtc.Frame;

/**
 * Created by tiago on 10/04/2017.
 */

public class GameBattleMusic extends Game {

  public GameBattleMusic(Context context, @GameType String id, String name, int drawableRes,
      boolean available) {
    super(context, id, name, drawableRes, available);
  }

  @Override public void apply(Frame frame) {

  }

  @Override public void onFrameSizeChange(Frame frame) {

  }

  @Override public void generateNewDatas() {

  }
}
