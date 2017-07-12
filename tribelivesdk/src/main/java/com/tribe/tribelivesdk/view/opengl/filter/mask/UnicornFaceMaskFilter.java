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
      UlsFaceAR.insertAnimationObjectAtIndex(1, unicornHorn, 91, true, 1.5f, isFrontFacing);

      String cosmetics = path + "unicorn_cosmetics.png";
      UlsFaceAR.insertAnimationObjectAtIndex(2, cosmetics, 29, true, 1.5f, isFrontFacing);

      String nose = path + "unicorn_nose.png";
      UlsFaceAR.insertAnimationObjectAtIndex(3, nose, 30, true, 1.5f, isFrontFacing);

      UlsFaceAR.cleanAnimationObjectAtIndex(4);
    }).start();
  }
}
