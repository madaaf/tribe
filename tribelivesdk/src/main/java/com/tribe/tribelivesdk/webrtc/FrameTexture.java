package com.tribe.tribelivesdk.webrtc;

/**
 * Created by tiago on 06/03/2017.
 */

public class FrameTexture {

  private int width;
  private int height;
  private int oesTextureId;
  private float[] transformMatrix;
  private int rotation;
  private long timestamp;

  public FrameTexture(int width, int height, int oesTextureId, float[] transformMatrix,
      int rotation, long timestamp) {
    this.width = width;
    this.height = height;
    this.oesTextureId = oesTextureId;
    this.transformMatrix = transformMatrix;
    this.rotation = rotation;
    this.timestamp = timestamp;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getOesTextureId() {
    return oesTextureId;
  }

  public void setOesTextureId(int oesTextureId) {
    this.oesTextureId = oesTextureId;
  }

  public float[] getTransformMatrix() {
    return transformMatrix;
  }

  public void setTransformMatrix(float[] transformMatrix) {
    this.transformMatrix = transformMatrix;
  }

  public int getRotation() {
    return rotation;
  }

  public void setRotation(int rotation) {
    this.rotation = rotation;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
