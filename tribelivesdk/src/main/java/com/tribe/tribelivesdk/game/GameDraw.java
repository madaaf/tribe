package com.tribe.tribelivesdk.game;

import android.content.Context;
import com.tribe.tribelivesdk.webrtc.Frame;

/**
 * Created by madaaflak on 31/07/2017.
 */

public class GameDraw extends Game {

  public GameDraw(Context context, @GameType String id, String name, int drawableRes) {
    super(context, id, name, drawableRes);
  }

  @Override public void apply(Frame frame) {

  }

  @Override public void onFrameSizeChange(Frame frame) {

  }
}
