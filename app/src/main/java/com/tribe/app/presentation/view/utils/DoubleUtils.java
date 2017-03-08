package com.tribe.app.presentation.view.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by tiago on 07/03/2017.
 */

public class DoubleUtils {

  public static double round(double value, int scale) {
    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(scale, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }
}
