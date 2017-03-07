package com.tribe.tribelivesdk.webrtc;

/**
 * Created by tiago on 06/03/2017.
 */

public class Frame {

  private byte[] data;
  private int width;
  private int height;
  private int rotation;
  private long timestamp;

  public Frame(byte[] data, int width, int height, int rotation, long timestamp) {
    this.data = data;
    this.width = width;
    this.height = height;
    this.rotation = rotation;
    this.timestamp = timestamp;
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
}
