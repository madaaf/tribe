package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.model.TribeGuest;

/**
 * Created by madaaflak on 16/01/2018.
 */

public class BirdRush {

  // VARIABLES
  private String currentUserId;
  private boolean lost = false;
  private TribeGuest tribeGuest;
  private Bitmap bitmap, bitmapBck;
  private int x, y, index;
  private float speedX, speedY;
  private int rotation = 0;
  private int txtWidth = 0;
  private Paint paint;
  private RectF rectF;
  private ScreenUtils screenUtils;

  private Integer[] birdsImage = new Integer[] {
      R.drawable.game_bird1, R.drawable.game_bird2, R.drawable.game_bird3, R.drawable.game_bird4,
      R.drawable.game_bird5, R.drawable.game_bird6, R.drawable.game_bird7, R.drawable.game_bird8
  };

  private Integer[] birdsBackImage = new Integer[] {
      R.drawable.game_bird_bck, R.drawable.game_bird_bck_2, R.drawable.game_bird_bck_3,
      R.drawable.game_bird_bck_4, R.drawable.game_bird_bck_5, R.drawable.game_bird_bck_6,
      R.drawable.game_bird_bck_7, R.drawable.game_bird_bck_8,
  };

  private String[] colors = new String[] {
      "#FBCF26", "#FA7FD9", "#BE9EFF", "#C7FFEA", "#F85C02", "#B6BFBF", "#F61D47", "#FFFFFF"
  };

  public BirdRush(int index, TribeGuest guest, ScreenUtils screenUtils, String currentUserId) {
    this.index = index;
    this.tribeGuest = guest;
    this.y = screenUtils.getHeightPx() / 2;
    this.currentUserId = currentUserId;
    this.screenUtils = screenUtils;
  }

  public int getRotation() {
    return rotation;
  }

  public void setRotation(int rotation) {
    this.rotation = rotation;
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

  public void setLost(boolean lost) {
    this.lost = lost;
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

  public Paint getPaint() {
    return paint;
  }

  public Paint putPaint() {
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setColor(Color.parseColor(colors[index]));
    paint.setTextSize(25);
    paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
    Rect bounds = new Rect();
    String name = (getName().length() > 10) ? getName().substring(0, 10).concat("...") : getName();
    paint.getTextBounds(name, 0, name.length(), bounds);
    this.txtWidth = bounds.width()+ screenUtils.dpToPx(5);
    return paint;
  }

  public int getColor() {
    return Color.parseColor(colors[index]);
  }

  public int getTxtWidth() {
    return txtWidth;
  }

  public RectF getRectF() {
    return rectF;
  }

  public void setRectF(RectF rectF) {
    this.rectF = rectF;
  }
}
