package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;

/**
 * Created by tiago on 02/06/2018.
 */
public class TextViewScore extends TextViewFont {

  @Inject ScreenUtils screenUtils;

  public TextViewScore(Context context) {
    super(context);
    init(context, null);
  }

  public TextViewScore(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public TextViewScore(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    setBackgroundResource(R.drawable.bg_pts);
    setGravity(Gravity.CENTER);
    setPadding(screenUtils.dpToPx(10), screenUtils.dpToPx(5), screenUtils.dpToPx(10),
        screenUtils.dpToPx(5));
  }

  /**
   * PUBLIC
   */

  public void setScore(int score) {
    if (score == 0) {
      setText(R.string.leaderboards_no_score);
    } else {
      setText("" + score);
    }
  }
}