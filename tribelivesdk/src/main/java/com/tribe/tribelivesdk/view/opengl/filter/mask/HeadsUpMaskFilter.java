package com.tribe.tribelivesdk.view.opengl.filter.mask;

import android.content.Context;
import android.support.annotation.DrawableRes;
import com.tribe.tribelivesdk.view.opengl.filter.FaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.utils.UlsFaceAR;

/**
 * Created by tiago on 08/02/2017.
 */

public class HeadsUpMaskFilter extends FaceMaskFilter {

  public static final String HEADS_UP_FILE = "heads_up.png";

  public HeadsUpMaskFilter(Context context, @FaceMaskFilterType String id, String name,
      @DrawableRes int drawableRes) {
    super(context, id, name, drawableRes);
  }

  public void computeMask(String path, boolean isFrontFacing) {
    new Thread(() -> {
      String headsUp = path + HEADS_UP_FILE;
      UlsFaceAR.insertAnimationObjectAtIndex(0, headsUp, 91, true, 1.2f, isFrontFacing);
      UlsFaceAR.cleanAnimationObjectAtIndex(1);
    }).start();
  }
}
