package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

/**
 * Created by tiago on 18/05/2016.
 */
public class FontCache {

  private static HashMap<String, Typeface> fontCache = new HashMap<>();

  public static Typeface getTypeface(String name, Context context) {
    Typeface typeface = fontCache.get(name);

    if (typeface == null) {
      try {
        typeface = Typeface.createFromAsset(context.getAssets(), name);
      } catch (Exception e) {
        return null;
      }

      fontCache.put(name, typeface);
    }

    return typeface;
  }
}
