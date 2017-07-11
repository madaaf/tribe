package com.tribe.tribelivesdk.entity;

import android.content.Context;
import android.support.annotation.DrawableRes;

/**
 * Created by tiago on 02/06/2017.
 */

public class GameFilter {

  protected Context context;
  protected String id;
  protected String name;
  protected int drawableRes;
  private boolean activated = false;

  public GameFilter(Context context, String id, String name, @DrawableRes int drawableRes) {
    this.context = context;
    this.id = id;
    this.name = name;
    this.drawableRes = drawableRes;
  }

  public Context getContext() {
    return context;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getDrawableRes() {
    return drawableRes;
  }

  public void setDrawableRes(int drawableRes) {
    this.drawableRes = drawableRes;
  }

  public void setActivated(boolean activated) {
    this.activated = activated;
  }

  public boolean isActivated() {
    return activated;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof GameFilter)) return false;

    GameFilter that = (GameFilter) o;

    return id != null ? id.equals(that.id) : that.id == null;
  }
}
