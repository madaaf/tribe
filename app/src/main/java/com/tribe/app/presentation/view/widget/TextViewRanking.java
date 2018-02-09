package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;

/**
 * Created by tiago on 02/06/2018.
 */
public class TextViewRanking extends TextViewFont {

  @Inject ScreenUtils screenUtils;

  private int padding;

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
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    padding = screenUtils.dpToPx(15);

    setBackgroundResource(R.drawable.bg_ranking);
    setGravity(Gravity.CENTER);
    TextViewCompat.setTextAppearance(this, R.style.BiggerTitle_2_BlueNew);
    setCustomFont(context, FontUtils.CIRCULARSTD_BOLD);
  }

  /**
   * PUBLIC
   */

  public void setRanking(int ranking) {
    if (ranking == 0) {
      setText("∞");
      setPadding(0, 0, 0, 0);
    } else if (ranking == 1) {
      setPadding(padding, 0, padding, 0);
      setText(EmojiParser.getEmoji(":crown:") + " " + ranking);
    } else {
      if (ranking >= 10) {
        setPadding(padding, 0, padding, 0);
      } else {
        setPadding(0, 0, 0, 0);
      }

      setText("" + ranking);
    }
  }
}