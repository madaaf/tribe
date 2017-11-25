package com.tribe.app.presentation.view.adapter.model;

/**
 * Created by tiago on 02/03/2017.
 */

public class ButtonModel {

  private String text;
  private int backgroundColor;
  private int textColor;
  private int imageRessource = 0;
  private  boolean setLoader = false;

  public ButtonModel(String text, int backgroundColor, int textColor) {
    this.text = text;
    this.backgroundColor = backgroundColor;
    this.textColor = textColor;
  }

  public ButtonModel(int imageRessource, boolean setLoader) {
    this.imageRessource = imageRessource;
    this.setLoader = setLoader;
  }

  public int getImageRessource() {
    return imageRessource;
  }

  public boolean isSetLoader() {
    return setLoader;
  }

  public int getTextColor() {
    return textColor;
  }

  public int getBackgroundColor() {
    return backgroundColor;
  }

  public String getText() {
    return text;
  }
}
