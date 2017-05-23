package com.tribe.tribelivesdk.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

/**
 * Created by tiago on 05/22/2017.
 */

public class BitmapUtils {

  public static Bitmap generateNewPostIt(Context context, String text, int textSize, int textColor,
      int backgroundId) {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setTypeface(FontCache.getTypeface("ProximaNovaSoft-Bold.ttf", context));
    paint.setTextSize(textSize);
    paint.setColor(textColor);
    paint.setTextAlign(Paint.Align.CENTER);
    float baseline = -paint.ascent();
    int width = (int) ((paint.measureText(text) + 0.5f) * 1.3f);
    int height = (int) ((baseline + paint.descent() + 0.5f) * 2);

    Drawable bg = ContextCompat.getDrawable(context, backgroundId);
    Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

    if (bg != null) {
      bg.setBounds(0, 0, width, height);
    }

    Canvas canvas = new Canvas(image);
    canvas.drawColor(0, PorterDuff.Mode.CLEAR);
    bg.draw(canvas);
    int xPos = (canvas.getWidth() / 2);
    int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
    canvas.drawText(text, xPos, yPos, paint);
    return image;
  }
}
