package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.tribe.app.presentation.utils.EmojiParser;

/**
 * Created by madaaflak on 05/03/2018.
 */

public class EmojiPoke extends TextView {

  int transX;
  int transY;
  boolean isSmall;

  public EmojiPoke(Context context, String emo, int width, int height, int x1, int y1, int transX,
      int transY, boolean isSmall) {
    super(context);
    this.transX = transX;
    this.transY = transY;
    this.isSmall = isSmall;
    setTextSize(25);
    setText(EmojiParser.demojizedText(emo));
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
    params.leftMargin = (int) x1 + transX;
    params.topMargin = (int) y1 + transY;
    setPadding(0, 0, 0, 3);
    setLayoutParams(params);
    setGravity(Gravity.CENTER);
  }

  public boolean isSmall() {
    return isSmall;
  }

  public void setSmall(boolean small) {
    isSmall = small;
  }

  public int getTransX() {
    return transX;
  }

  public void setTransX(int transX) {
    this.transX = transX;
  }

  public int getTransY() {
    return transY;
  }

  public void setTransY(int transY) {
    this.transY = transY;
  }
}
