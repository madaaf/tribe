package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.model.TribeGuest;

/**
 * Created by madaaflak on 16/01/2018.
 */

public class BirdRush {

  private ScreenUtils screenUtils;

  // VARIABLES
  private String currentUserId;
  private int index;
  private boolean lost = false;
  private TribeGuest tribeGuest;
  private String id;
  private Bitmap bitmap;
  private Bitmap bitmapBck;
  private int x, y;
  private float speedX, speedY;

  private String[] birdsColors = new String[] {
      "FBCF26", "FA7FD9", "BE9EFF", "42F4B2", "F85C02", "3DE9DA", "8B572A", "FFFFFF"
  };

  private Integer[] birdsImage = new Integer[] {
      R.drawable.game_bird1, R.drawable.game_bird2, R.drawable.game_bird3, R.drawable.game_bird4,
      R.drawable.game_bird5, R.drawable.game_bird6, R.drawable.game_bird7, R.drawable.game_bird8
  };

  private Integer[] birdsBackImage = new Integer[] {
      R.drawable.game_bird_bck, R.drawable.game_bird_bck_2, R.drawable.game_bird_bck_3,
      R.drawable.game_bird_bck_4, R.drawable.game_bird_bck_5, R.drawable.game_bird_bck_6,
      R.drawable.game_bird_bck_7, R.drawable.game_bird_bck_8,
  };

  public BirdRush(int index, TribeGuest guest, ScreenUtils screenUtils, String currentUserId) {
    this.index = index;
    this.tribeGuest = guest;
    this.y = screenUtils.getHeightPx() / 2;
    this.currentUserId = currentUserId;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public boolean isMine() {
    return tribeGuest.getId().equals(currentUserId);
  }

  public void setY(int y) {
    this.y = y;
  }

  public boolean isLost() {
    return lost;
  }

  public float getSpeedX() {
    return speedX;
  }

  public void setSpeedX(float speedX) {
    this.speedX = speedX;
  }

  public float getSpeedY() {
    return speedY;
  }

  public void setSpeedY(float speedY) {
    this.speedY = speedY;
  }

  public String getGuestId() {
    return tribeGuest.getId();
  }

  public String getName() {
    return tribeGuest.getDisplayName();
  }

  public void setBitmap(Resources resources) {
    this.bitmap = BitmapFactory.decodeResource(resources, birdsImage[index]);
    this.bitmapBck = BitmapFactory.decodeResource(resources, birdsBackImage[index]);
  }

  public Bitmap getBitmap() {
    return bitmap;
  }

  public Bitmap getBackgroundBitmap() {
    return bitmapBck;
  }
}
