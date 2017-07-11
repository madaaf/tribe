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
    String glasses = path + "round_sunglasses.png";
    UlsFaceAR.insertAnimationObjectAtIndex(1, glasses, 27, true, 0.7f, isFrontFacing);
    UlsFaceAR.cleanAnimationObjectAtIndex(2);
  }
}
