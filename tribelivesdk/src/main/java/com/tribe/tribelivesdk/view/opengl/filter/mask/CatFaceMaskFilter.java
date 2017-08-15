package com.tribe.tribelivesdk.view.opengl.filter.mask;

import android.content.Context;
import android.support.annotation.DrawableRes;
import com.tribe.tribelivesdk.view.opengl.filter.FaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.utils.UlsFaceAR;

/**
 * Created by tiago on 11/07/2017.
 */

public class CatFaceMaskFilter extends FaceMaskFilter {

  public CatFaceMaskFilter(Context context, @FaceMaskFilterType String id, String name,
      @DrawableRes int drawableRes) {
    super(context, id, name, drawableRes);
  }

  public void computeMask(String path, boolean isFrontFacing) {
    new Thread(() -> {
      String cosmeticsMask = path + "cat_mask.png";
      UlsFaceAR.setMask(context, cosmeticsMask);

      String unicornHorn = path + "cat_ears.png";
      UlsFaceAR.insertAnimationObjectAtIndex(0, unicornHorn, 91, true, 1.2f, isFrontFacing);

      String nose = path + "cat_nose.png";
      UlsFaceAR.insertAnimationObjectAtIndex(1, nose, 30, true, 1.2f, isFrontFacing);

      UlsFaceAR.cleanAnimationObjectAtIndex(3);
    }).start();
  }
}
