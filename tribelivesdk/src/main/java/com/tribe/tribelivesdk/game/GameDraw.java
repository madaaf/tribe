package com.tribe.tribelivesdk.game;

import android.content.Context;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.util.Random;
import timber.log.Timber;

/**
 * Created by madaaflak on 31/07/2017.
 */

public class GameDraw extends Game {

  private String currentDrawName = "";
  private TribeGuest currentDrawer = null;

  public GameDraw(Context context, @GameType String id, String name, int drawableRes,
      boolean available) {
    super(context, id, name, drawableRes, available);
  }

  @Override public void apply(Frame frame) {

  }

  @Override public void onFrameSizeChange(Frame frame) {

  }

  @Override public void generateNewDatas() {
    if (dataList == null || dataList.size() == 0) {
      return;
    }
    currentDrawName = dataList.get(new Random().nextInt(dataList.size()));
    Timber.d("set current game name  : " + currentDrawName);

    if (peerList == null || peerList.size() == 0) {
      return;
    }
    currentDrawer = getNextGuest();
    Timber.d("set current gamer : " + currentDrawer.getDisplayName());
  }

  public String getCurrentDrawName() {
    return currentDrawName;
  }

  public TribeGuest getCurrentDrawer() {
    return currentDrawer;
  }

  public void setCurrentDrawer(TribeGuest currentDrawer) {
    this.currentDrawer = currentDrawer;
  }

  public void setCurrentDrawName(String currentDrawName) {
    this.currentDrawName = currentDrawName;
  }
}
