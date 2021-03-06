package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

/**
 * A FrameLayout that will always be square (based out of its width)
 */
public class SquareCardView extends CardView {

  private boolean shouldSquare = true;

  public SquareCardView(Context context) {
    super(context);
  }

  public SquareCardView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SquareCardView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (shouldSquare) {
      super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    } else {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }

  protected void setShouldSquare(boolean value) {
    shouldSquare = value;
  }
}