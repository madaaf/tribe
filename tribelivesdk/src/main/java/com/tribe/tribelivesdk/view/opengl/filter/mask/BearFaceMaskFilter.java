package com.tribe.tribelivesdk.view.opengl.filter.mask;

import android.content.Context;
import android.support.annotation.DrawableRes;
import com.tribe.tribelivesdk.view.opengl.filter.FaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.utils.UlsFaceAR;

/**
 * Created by tiago on 11/07/2017.
 */

public class BearFaceMaskFilter extends FaceMaskFilter {

  public BearFaceMaskFilter(Context context, @FaceMaskFilterType String id, String name,
      @DrawableRes int drawableRes) {
    super(context, id, name, drawableRes);
  }

  public void computeMask(String path, boolean isFrontFacing) {
    new Thread(() -> {
      String bearEars = path + "bear_ears.png";
      UlsFaceAR.insertAnimationObjectAtIndex(0, bearEars, 91, true, 1.2f, isFrontFacing);

      String glasses = path + "bear_glasses.png";
      UlsFaceAR.insertAnimationObjectAtIndex(1, glasses, 27, true, 1.2f, isFrontFacing);

      String nose = path + "bear_nose.png";
      UlsFaceAR.insertAnimationObjectAtIndex(2, nose, 30, true, 1.2f, isFrontFacing);

      UlsFaceAR.cleanAnimationObjectAtIndex(3);
    }).start();
  }
}
