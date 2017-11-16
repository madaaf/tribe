package com.tribe.tribelivesdk.game;

import android.content.Context;
import com.tribe.tribelivesdk.webrtc.Frame;

/**
 * Created by tiago on 11/16/2017.
 */

public class GameSliceFruit extends Game {

  public GameSliceFruit(Context context, @GameType String id, String name, int drawableRes, String url,
      boolean available) {
    super(context, id, name, drawableRes, url, available);
  }

  @Override public void apply(Frame frame) {

  }

  @Override public void onFrameSizeChange(Frame frame) {

  }

  @Override public void generateNewDatas() {

  }
}
