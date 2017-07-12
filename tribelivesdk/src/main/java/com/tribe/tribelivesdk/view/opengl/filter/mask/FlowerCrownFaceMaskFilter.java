package com.tribe.tribelivesdk.view.opengl.filter.mask;

import android.content.Context;
import android.support.annotation.DrawableRes;
import com.tribe.tribelivesdk.view.opengl.filter.FaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.utils.UlsFaceAR;

/**
 * Created by tiago on 11/07/2017.
 */

public class FlowerCrownFaceMaskFilter extends FaceMaskFilter {

  public FlowerCrownFaceMaskFilter(Context context, @FaceMaskFilterType String id, String name,
      @DrawableRes int drawableRes) {
    super(context, id, name, drawableRes);
  }

  public void computeMask(String path, boolean isFrontFacing) {
    new Thread(() -> {
      String flowerCrown = path + "flower_crown_crown.png";
      UlsFaceAR.insertAnimationObjectAtIndex(1, flowerCrown, 91, true, 1.5f, isFrontFacing);

      String flowerCrownCosmetics = path + "flower_crown_cosmetics.png";
      UlsFaceAR.insertAnimationObjectAtIndex(2, flowerCrownCosmetics, 29, true, 1.5f,
          isFrontFacing);

      UlsFaceAR.cleanAnimationObjectAtIndex(3);
    }).start();
  }
}
