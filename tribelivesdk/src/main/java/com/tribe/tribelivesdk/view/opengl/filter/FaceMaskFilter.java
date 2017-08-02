package com.tribe.tribelivesdk.view.opengl.filter;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringDef;

/**
 * Created by tiago on 11/07/2017.
 */

public abstract class FaceMaskFilter extends FilterMask {

  @StringDef({
      FACE_MASK_RABBIT, FACE_MASK_ORANGE_SUNGLASSES, FACE_MASK_TATTOO, FACE_MASK_FLOWER_CROWN,
      FACE_MASK_BEAR, FACE_MASK_SUNGLASSES, FACE_MASK_UNICORN, FACE_MASK_CAT, FACE_MASK_NEON_SKULL,
      FACE_MASK_HEADS_UP
  }) public @interface FaceMaskFilterType {
  }

  public static final String FACE_MASK_HEADS_UP = "FACE_MASK_HEADS_UP";

  public static final String FACE_MASK_RABBIT = "FACE_MASK_RABBIT";
  public static final String FACE_MASK_ORANGE_SUNGLASSES = "FACE_MASK_ORANGE_SUNGLASSES";
  public static final String FACE_MASK_TATTOO = "FACE_MASK_TATTOO";
  public static final String FACE_MASK_FLOWER_CROWN = "FACE_MASK_FLOWER_CROWN";
  public static final String FACE_MASK_BEAR = "FACE_MASK_BEAR";
  public static final String FACE_MASK_SUNGLASSES = "FACE_MASK_SUNGLASSES";
  public static final String FACE_MASK_UNICORN = "FACE_MASK_UNICORN";
  public static final String FACE_MASK_CAT = "FACE_MASK_CAT";
  public static final String FACE_MASK_NEON_SKULL = "FACE_MASK_NEON_SKULL";

  public FaceMaskFilter(Context context, @FaceMaskFilterType String id, String name,
      @DrawableRes int drawableRes) {
    super(context, id, name, drawableRes);
  }

  public abstract void computeMask(String path, boolean isFrontFacing);
}
