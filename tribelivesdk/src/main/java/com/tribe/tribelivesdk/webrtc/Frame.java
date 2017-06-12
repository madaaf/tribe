package com.tribe.tribelivesdk.webrtc;

/**
 * Created by tiago on 06/03/2017.
 */

public class Frame {

  private byte[] data, dataOut;
  private int width;
  private int height;
  private int rotation;
  private long timestamp;
  private boolean frontCamera;

  public Frame(byte[] data, int width, int height, int rotation, long timestamp,
      boolean frontCamera) {
    this.data = data;
    this.width = width;
    this.height = height;
    this.rotation = rotation;
    this.timestamp = timestamp;
    this.frontCamera = frontCamera;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
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

  public void setDataOut(byte[] dataOut) {
    this.dataOut = dataOut;
  }

  public byte[] getDataOut() {
    return dataOut;
  }

  public boolean isFrontCamera() {
    return frontCamera;
  }

  public void setFrontCamera(boolean frontCamera) {
    this.frontCamera = frontCamera;
  }

  public Frame copy(byte[] argbIn) {
    System.arraycopy(data, 0, argbIn, 0, data.length);
    return new Frame(argbIn, width, height, rotation, timestamp, frontCamera);
  }
}
