package com.tribe.tribelivesdk.view.opengl.filter.mask;

import android.content.Context;
import android.support.annotation.DrawableRes;
import com.tribe.tribelivesdk.view.opengl.filter.FaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.utils.UlsFaceAR;

/**
 * Created by tiago on 11/07/2017.
 */

public class NeonSkullFaceMaskFilter extends FaceMaskFilter {

  public NeonSkullFaceMaskFilter(Context context, @FaceMaskFilterType String id, String name,
      @DrawableRes int drawableRes) {
    super(context, id, name, drawableRes);
  }

  public void computeMask(String path, boolean isFrontFacing) {
    new Thread(() -> {
      String neonSkullContour = path + "neon_skull_contour.png";
      UlsFaceAR.insertAnimationObjectAtIndex(1, neonSkullContour, 30, true, 1.5f, isFrontFacing);

      String neonSkullEyes = path + "neon_skull_eyes.png";
      UlsFaceAR.insertAnimationObjectAtIndex(2, neonSkullEyes, 27, true, 1.5f, isFrontFacing);

      String neonSkullNose = path + "neon_skull_nose.png";
      UlsFaceAR.insertAnimationObjectAtIndex(3, neonSkullNose, 30, true, 1.5f, isFrontFacing);

      UlsFaceAR.cleanAnimationObjectAtIndex(4);
    }).start();
  }
}
