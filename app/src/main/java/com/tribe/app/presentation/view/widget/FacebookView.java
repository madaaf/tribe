package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.tribe.app.R;

/**
 * Created by horatiothomas on 8/17/16.
 * Last modified by tiago on 12/13/16
 */
public class FacebookView extends LinearLayout {

  public FacebookView(Context context) {
    this(context, null);
    init(context, null);
  }

  public FacebookView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_facebook, this, true);

    setBackgroundResource(R.color.blue_facebook_dark);
    setOrientation(VERTICAL);
    setGravity(Gravity.CENTER);
    setClickable(true);
  }
}

