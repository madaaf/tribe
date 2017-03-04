package com.tribe.app.presentation.view.adapter.interfaces;

import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.model.AvatarModel;

/**
 * Created by tiago on 02/03/2017.
 */

public interface BaseListInterface {

  String getId();

  void setAnimateAdd(boolean animateAdd);

  boolean isAnimateAdd();

  boolean isActionAvailable(User currentUser);

  boolean isInvisible();

  String getDisplayName();

  String getUsername();

  boolean isFriend();

  AvatarModel getAvatar();

  boolean isReverse();
}
