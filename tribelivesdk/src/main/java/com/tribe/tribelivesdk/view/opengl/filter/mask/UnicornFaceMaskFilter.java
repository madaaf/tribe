package com.tribe.tribelivesdk.view.opengl.filter.mask;

import android.content.Context;
import android.support.annotation.DrawableRes;
import com.tribe.tribelivesdk.view.opengl.filter.FaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.utils.UlsFaceAR;

/**
 * Created by tiago on 11/07/2017.
 */

public class UnicornFaceMaskFilter extends FaceMaskFilter {

  public UnicornFaceMaskFilter(Context context, @FaceMaskFilterType String id, String name,
      @DrawableRes int drawableRes) {
    super(context, id, name, drawableRes);
  }

  public void computeMask(String path, boolean isFrontFacing) {
    new Thread(() -> {
      String unicornHorn = path + "unicorn_horn.png";
      UlsFaceAR.insertAnimationObjectAtIndex(0, unicornHorn, 91, true, 1.2f, isFrontFacing);

      String cosmetics = path + "unicorn_cosmetics.png";
      UlsFaceAR.insertAnimationObjectAtIndex(1, cosmetics, 29, true, 1.2f, isFrontFacing);

      String nose = path + "unicorn_nose.png";
      UlsFaceAR.insertAnimationObjectAtIndex(2, nose, 30, true, 1.2f, isFrontFacing);

      UlsFaceAR.cleanAnimationObjectAtIndex(3);
    }).start();
  }
}
