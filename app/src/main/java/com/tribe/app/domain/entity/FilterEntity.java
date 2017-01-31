package com.tribe.app.domain.entity;

import android.support.annotation.StringDef;

import com.tribe.app.presentation.utils.StringUtils;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by tiago on 15/09/2016.
 */
public class FilterEntity implements Serializable {

  @StringDef({ ICON, LETTER }) @Retention(RetentionPolicy.SOURCE) public @interface FilterType {
  }

  public static final String ICON = "icon";
  public static final String LETTER = "letter";

  private @FilterType String type;
  private boolean activated = false;
  private String letter;
  private int drawable;

  public FilterEntity(@FilterType String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public boolean isActivated() {
    return activated;
  }

  public void setActivated(boolean activated) {
    this.activated = activated;
  }

  public String getLetter() {
    return letter;
  }

  public void setLetter(String letter) {
    this.letter = letter;
  }

  public int getDrawable() {
    return drawable;
  }

  public void setDrawable(int drawable) {
    this.drawable = drawable;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (!StringUtils.isEmpty(letter) ? letter.hashCode() : drawable);
    return result;
  }
}
