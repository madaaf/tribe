package com.tribe.app.presentation.view.utils;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Tiago Duarte
 */
public class Weather {

  @StringDef({ FAHRENHEIT, CELSIUS }) @Retention(RetentionPolicy.SOURCE)
  public @interface WeatherUnits {
  }

  public static final String FAHRENHEIT = "fahrenheit";
  public static final String CELSIUS = "celsius";

  private Weather() {
  }
}
