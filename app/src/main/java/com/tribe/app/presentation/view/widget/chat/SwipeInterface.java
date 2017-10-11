package com.tribe.app.presentation.view.widget.chat;

import android.view.View;

/**
 * Created by madaaflak on 11/10/2017.
 */

public interface SwipeInterface {

  public void bottom2top(View v);

  public void left2right(View v);

  public void right2left(View v);

  public void top2bottom(View v);

  public void onActionUp(View v);

  public void onActionDown(View v);
}