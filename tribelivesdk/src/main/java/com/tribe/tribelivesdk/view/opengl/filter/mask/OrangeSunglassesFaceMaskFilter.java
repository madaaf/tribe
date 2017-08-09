package com.tribe.tribelivesdk.view.opengl.filter.mask;

import android.content.Context;
import android.support.annotation.DrawableRes;
import com.tribe.tribelivesdk.view.opengl.filter.FaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.utils.UlsFaceAR;

/**
 * Created by tiago on 11/07/2017.
 */

public class OrangeSunglassesFaceMaskFilter extends FaceMaskFilter {

  public OrangeSunglassesFaceMaskFilter(Context context, @FaceMaskFilterType String id, String name,
      @DrawableRes int drawableRes) {
    super(context, id, name, drawableRes);
  }

  public void computeMask(String path, boolean isFrontFacing) {
    new Thread(() -> {
      String tattooFace = path + "orange_sunglasses_tattoo_mask.png";
      UlsFaceAR.setMask(context, tattooFace);

      String glasses = path + "orange_sunglasses_sunglasses.png";
      UlsFaceAR.insertAnimationObjectAtIndex(0, glasses, 27, true, 1.2f, isFrontFacing);

      String noseRing = path + "orange_sunglasses_nose_ring.png";
      UlsFaceAR.insertAnimationObjectAtIndex(1, noseRing, 33, true, 1.2f, isFrontFacing);

      UlsFaceAR.cleanAnimationObjectAtIndex(2);
    }).start();
  }
}
