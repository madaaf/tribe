package com.tribe.tribelivesdk.view.opengl.renderer;

import com.tribe.tribelivesdk.view.opengl.gles.SimplePlane;
import org.opencv.core.Mat;

/**
 * Created by ian on 2016/10/24.
 */

public class AnimationObject {

  public String animObjPath;
  public int pointIndex;
  public int animateAtXcoord;
  public int animateAtYcoord;
  public boolean isRotationNeeded;
  public float imgScale;

  public boolean alive = false;
  public boolean useCoordinate;

  public SimplePlane animPlane;
  public Mat animTempMat;
}
