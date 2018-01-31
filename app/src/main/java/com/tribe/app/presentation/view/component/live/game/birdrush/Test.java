package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.tribe.app.R;

/**
 * Created by madaaflak on 31/01/2018.
 */

public class Test extends Sprite {

  /** Static bitmap to reduce memory usage. */
  public static Bitmap globalBitmap;

  public Test(Resources resources) {
    super();
    move();

    if (globalBitmap == null) {
      globalBitmap = BitmapFactory.decodeResource(resources, R.drawable.game_aliens_attack_alien_2);
    }
    this.bitmap = globalBitmap;
  }
}
