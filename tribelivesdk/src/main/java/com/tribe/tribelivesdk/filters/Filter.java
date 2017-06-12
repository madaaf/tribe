package com.tribe.tribelivesdk.filters;

import android.content.Context;
import android.support.annotation.DrawableRes;
import com.tribe.tribelivesdk.entity.GameFilter;

/**
 * Created by tiago on 02/06/2017.
 */

public abstract class Filter extends GameFilter {

  public Filter(Context context, String id, String name, @DrawableRes int drawableRes) {
    super(context, id, name, drawableRes);
  }

  public abstract void apply(byte[] argb);

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof Filter)) return false;

    Filter that = (Filter) o;

    return id != null ? id.equals(that.id) : that.id == null;
  }
}
