package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.FontUtils;

/**
 * Created by tiago on 02/06/2018.
 */
public class TextViewRanking extends TextViewFont {

  public TextViewRanking(Context context) {
    super(context);
    init(context, null);
  }

  public TextViewRanking(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public TextViewRanking(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    setBackgroundResource(R.drawable.bg_ranking);
    setGravity(Gravity.CENTER);
    TextViewCompat.setTextAppearance(this, R.style.BiggerTitle_2_BlueNew);
    setCustomFont(context, FontUtils.CIRCULARSTD_BOLD);
  }

  /**
   * PUBLIC
   */

  public void setRanking(int ranking) {
    // TODO add crown
    setText("" + ranking);
  }
}