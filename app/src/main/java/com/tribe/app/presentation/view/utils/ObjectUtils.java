package com.tribe.app.presentation.view.utils;

/**
 * Created by tiago on 29/01/2017.
 */

public class ObjectUtils {

  public static boolean nullSafeEquals(Object a, Object b) {
    return (a == b) || (a != null && a.equals(b));
  }
}
