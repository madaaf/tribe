package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.utils.ScreenUtils;

/**
 * Created by madaaflak on 05/03/2018.
 */

public class EmojiPoke extends TextView {

  int transX;
  int transY;
  boolean isSmall;

  public EmojiPoke(Context context, String emo, int width, int height, int x1, int y1, int transX,
      int transY, boolean isSmall, ScreenUtils screenUtils) {
    super(context);
    this.transX = transX;
    this.transY = transY;
    this.isSmall = isSmall;

    setText(EmojiParser.demojizedText(emo));
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
    params.leftMargin = (int) x1 + transX;
    params.topMargin = (int) y1 + transY;
    setPadding(0, 0, 0, screenUtils.dpToPx(3));
    setLayoutParams(params);
    setGravity(Gravity.CENTER);
    if (isSmall) {
      setTextSize(screenUtils.pxToDp(width) / (2f));
    } else {
      setTextSize(screenUtils.pxToDp(width) / (1.5f));
    }
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
