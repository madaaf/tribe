package com.tribe.tribelivesdk.view.opengl.renderer;

public class AnimationObjectHolder {

  private static AnimationObject[] animationObjects = new AnimationObject[11];
  private static AnimationObject[] newAnimationObjects = new AnimationObject[11];
  private static AnimationObject tempAnimationObject;

  public static AnimationObject getAnimationObject(int index) {
    if (animationObjects[index] == null) {
      setAnimationObjects(index, new AnimationObject());
    }

    return animationObjects[index];
  }

  public static void setAnimationObjects(int index, AnimationObject obj) {
    animationObjects[index] = obj;
  }

  public static AnimationObject getNewAnimationObject(int index) {
    if (newAnimationObjects[index] == null) {
      setNewAnimationObjects(index, new AnimationObject());
    }

    return newAnimationObjects[index];
  }

  public static void setNewAnimationObjects(int index, AnimationObject obj) {
    newAnimationObjects[index] = obj;
  }
}
