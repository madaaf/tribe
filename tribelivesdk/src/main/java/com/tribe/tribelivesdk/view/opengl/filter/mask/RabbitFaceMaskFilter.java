package com.tribe.tribelivesdk.view.opengl.filter.mask;

import android.content.Context;
import android.support.annotation.DrawableRes;
import com.tribe.tribelivesdk.view.opengl.filter.FaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.utils.UlsFaceAR;

/**
 * Created by tiago on 11/07/2017.
 */

public class RabbitFaceMaskFilter extends FaceMaskFilter {

  public RabbitFaceMaskFilter(Context context, @FaceMaskFilter.FaceMaskFilterType String id,
      String name, @DrawableRes int drawableRes) {
    super(context, id, name, drawableRes);
  }

  public void computeMask(String path, boolean isFrontFacing) {
    new Thread(() -> {
      String cosmeticsMask = path + "rabbit_mask.png";
      UlsFaceAR.setMask(context, cosmeticsMask);

      String rabbitEars = path + "rabbit_ears.png";
      UlsFaceAR.insertAnimationObjectAtIndex(0, rabbitEars, 91, true, 0.7f, isFrontFacing);

      UlsFaceAR.cleanAnimationObjectAtIndex(1);
    }).start();
  }
}
