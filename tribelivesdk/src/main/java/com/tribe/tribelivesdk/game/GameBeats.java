package com.tribe.tribelivesdk.game;

import android.content.Context;
import com.tribe.tribelivesdk.webrtc.Frame;

/**
 * Created by tiago on 10/04/2017.
 */

public class GameBeats extends Game {

  public GameBeats(Context context, @GameType String id, String name, int drawableRes,
      boolean available) {
    super(context, id, name, drawableRes, available);
  }

  @Override public void apply(Frame frame) {

  }

  @Override public void onFrameSizeChange(Frame frame) {

  }
}
