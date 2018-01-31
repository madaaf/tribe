package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import timber.log.Timber;

/**
 * Created by madaaflak on 31/01/2018.
 */

public class Sprite {

  /** The bitmaps that holds the frames that should be drawn */
  protected Bitmap bitmap;

  /** Height and width of one frame of the bitmap */
  protected int height, width;

  /** Horizontal and vertical speed of the sprite */
  protected float speedX, speedY;

  /** x and y coordinates on the canvas */
  protected int x, y;

  /** The source frame of the bitmap that should be drawn */
  protected Rect src;

  /** The destination area that the frame should be drawn to */
  protected Rect dst;

  public Sprite() {
    speedX = 10;
    speedY = 10;
    width = 200;
    height = 200;
    src = new Rect();
    dst = new Rect();
  }

  /**
   * Draws the frame of the bitmap specified by col and row
   * at the position given by x and y
   *
   * @param canvas Canvas that should be drawn on
   */
  public void draw(Canvas canvas) {
    //src.set(col * width, row * height, (col + 1) * width, (row + 1) * height);
    Timber.e("DRAW BITMAP " + x + " " + y);
    dst.set(x, y, x + width, y + height);
    canvas.drawBitmap(bitmap, src, dst, null);
  }

  /**
   * Modifies the x and y coordinates according to the speedX and speedY value
   */
  public void move() {
    // changeToNextFrame();
    // Its more efficient if only the classes that need this implement it in their move method.

    x += speedX;
    y += speedY;
  }
}
