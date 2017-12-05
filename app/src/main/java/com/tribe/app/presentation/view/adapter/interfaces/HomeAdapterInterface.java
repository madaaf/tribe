package com.tribe.app.presentation.view.adapter.interfaces;

import com.tribe.app.presentation.view.adapter.decorator.BaseSectionItemDecoration;
import java.util.Date;

public interface HomeAdapterInterface {

  String getId();

  boolean isOnline();

  boolean isLive();

  String getDisplayName();

  boolean isRead();

  String getProfilePicture();

  Date getLastSeenAt();

  public @BaseSectionItemDecoration.HeaderType int getHomeSectionType();
}