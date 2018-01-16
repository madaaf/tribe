package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.view.View;
import java.util.List;

/**
 * Created by madaaflak on 15/01/2018.
 */

public interface TouchHandler extends View.OnTouchListener {

  boolean isTouchDown(int pointer);

  int getTouchX(int pointer);

  int getTouchY(int pointer);

  List<Integer> getTouchEvents();
}
