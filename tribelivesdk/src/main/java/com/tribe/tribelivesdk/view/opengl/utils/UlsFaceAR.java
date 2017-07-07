package com.tribe.tribelivesdk.view.opengl.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.tribe.tribelivesdk.view.opengl.gles.SimplePlane;
import com.tribe.tribelivesdk.view.opengl.renderer.AnimationObject;
import com.tribe.tribelivesdk.view.opengl.renderer.AnimationObjectHolder;
import com.tribe.tribelivesdk.view.opengl.renderer.UlsRenderer;
import java.io.File;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class UlsFaceAR {

  public enum ImageDataType {
    I420, NV21, ARGB
  }

  public static void setMask(Context context, String maskFilePath) {
    UlsRenderer ulsRenderer = UlsRenderer.getInstance(context);
    if (maskFilePath != null) {
      File file = new File(maskFilePath);
      if (file.exists()) {
        ulsRenderer.setMaskFile(maskFilePath);
      }
    } else {
      ulsRenderer.setMaskFile(null);
    }
  }

  public static void insertAnimationObjectAtIndex(int layerIndex, String animObjPath,
      int pointIndex, boolean isRotationNeeded, float imgScale, boolean isFrontFacing) {
    insertAnimationObjectAtIndex(layerIndex, animObjPath, isRotationNeeded, imgScale,
        isFrontFacing);
    AnimationObject obj = AnimationObjectHolder.getNewAnimationObject(layerIndex);
    obj.pointIndex = pointIndex;
    obj.useCoordinate = false;
  }

  public static void insertAnimationObjectAtIndex(int layerIndex, String animObjPath,
      int animateAtXcoord, int animateAtYcoord, boolean isRotationNeeded, float imgScale,
      boolean isFrontFacing) {
    insertAnimationObjectAtIndex(layerIndex, animObjPath, isRotationNeeded, imgScale,
        isFrontFacing);
    AnimationObject obj = AnimationObjectHolder.getNewAnimationObject(layerIndex);
    obj.animateAtXcoord = animateAtXcoord;
    obj.animateAtYcoord = animateAtYcoord;
    obj.useCoordinate = true;
  }

  private static void insertAnimationObjectAtIndex(int layerIndex, String animObjPath,
      boolean isRotationNeeded, float imgScale, boolean isFrontFacing) {
    AnimationObject obj = new AnimationObject();
    //        AnimationObject obj = AnimationObjectHolder.getTempAnimationObject();

    obj.imgScale = imgScale;
    if (obj.animObjPath != animObjPath) {
      obj.animObjPath = animObjPath;
      if (obj.animTempMat == null) {
        obj.animTempMat = new Mat();
      }
      loadPngFile(obj.animObjPath, obj.animTempMat, isFrontFacing);
      obj.animPlane = new SimplePlane(1, 1);
      obj.animPlane.loadPixels(obj.animTempMat);
    }
    obj.isRotationNeeded = isRotationNeeded;
    obj.alive = true;

    AnimationObjectHolder.setNewAnimationObjects(layerIndex, obj);
  }

  public static void cleanAnimationObjectAtIndex(int layerIndex) {
    AnimationObjectHolder.getAnimationObject(layerIndex).alive = false;
    AnimationObjectHolder.getNewAnimationObject(layerIndex).alive = false;
  }

  public static void cleanAllAnimationObjects() {
    for (int i = 0; i < 10; i++) {
      cleanAnimationObjectAtIndex(i);
    }
  }

  private static void loadPngFile(String sFilePath, Mat mat, boolean isFrontFacing) {
    File file = new File(sFilePath);
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    Bitmap bmp = BitmapFactory.decodeFile(file.getPath(), options);

    if (bmp == null) {
      return;
    }

    android.graphics.Matrix matrix = new android.graphics.Matrix();
    if (isFrontFacing) {
      matrix.preScale(1.0f, -1.0f);
    } else {
      matrix.preScale(-1.0f, -1.0f);
    }

    Bitmap bmpFlipped =
        Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

    mat.create(bmpFlipped.getHeight(), bmpFlipped.getWidth(), CvType.CV_8U);
    Utils.bitmapToMat(bmpFlipped, mat);
    bmp.recycle();
    bmpFlipped.recycle();
  }
}
