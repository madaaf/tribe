package com.tribe.tribelivesdk.view.opengl.filter.mask;

import android.content.Context;
import android.support.annotation.DrawableRes;
import com.tribe.tribelivesdk.view.opengl.filter.FaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.utils.UlsFaceAR;

/**
 * Created by tiago on 11/07/2017.
 */

public class RoundSunglassesFaceMaskFilter extends FaceMaskFilter {

  public RoundSunglassesFaceMaskFilter(Context context, @FaceMaskFilterType String id, String name,
      @DrawableRes int drawableRes) {
    super(context, id, name, drawableRes);
  }

  public void computeMask(String path, boolean isFrontFacing) {
    new Thread(() -> {
      String glasses = path + "round_sunglasses_sunglasses.png";
      UlsFaceAR.insertAnimationObjectAtIndex(0, glasses, 27, true, 1.2f, isFrontFacing);

      UlsFaceAR.cleanAnimationObjectAtIndex(1);
    }).start();
  }
}
