package com.tribe.app.presentation.view.widget;

/**
 * Created by madaaflak on 08/08/2017.
 */

import android.graphics.Path;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 08/08/17.
 */

public class TrackablePath extends Path {
  private List<Point> pathData = new ArrayList<>();

  @Override public void moveTo(float x, float y) {
    super.moveTo(x, y);
    pathData.add(new Point(x, y));
  }

  @Override public void lineTo(float x, float y) {
    super.lineTo(x, y);
    pathData.add(new Point(x, y));
  }

  public List<Point> getPathData() {
    return pathData;
  }

  public static class Point implements Serializable {
    public float x;
    public float y;

    public Point(float x, float y) {
      this.x = x;
      this.y = y;
    }
  }
}