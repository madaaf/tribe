package com.tribe.tribelivesdk.view.opengl.filter;

import android.content.Context;
import android.support.annotation.DrawableRes;
import com.tribe.tribelivesdk.entity.GameFilter;
import com.tribe.tribelivesdk.view.opengl.utils.UlsFaceAR;

/**
 * Created by tiago on 10/07/2017.
 */

public class FilterMask extends GameFilter {

  public FilterMask(Context context, String id, String name, @DrawableRes int drawableRes) {
    super(context, id, name, drawableRes);
  }

  public void release() {
    UlsFaceAR.setMask(context, null);
    UlsFaceAR.cleanAllAnimationObjects();
  }
}
