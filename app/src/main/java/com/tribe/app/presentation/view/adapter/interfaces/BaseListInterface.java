package com.tribe.app.presentation.view.adapter.interfaces;

import com.tribe.app.presentation.view.adapter.model.AvatarModel;

/**
 * Created by tiago on 02/03/2017.
 */

public interface BaseListInterface {

  void setAnimateAdd(boolean animateAdd);

  boolean isAnimateAdd();

  String getDisplayName();

  String getUsername();

  boolean isFriend();

  AvatarModel getAvatar();

  boolean isReverse();
}
