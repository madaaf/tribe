package com.tribe.app.presentation.view.adapter.interfaces;

import com.tribe.app.presentation.view.widget.header.LiveInviteViewHeader;

public interface LiveInviteAdapterSectionInterface {

  String getId();

  boolean isOnline();

  boolean isRinging();

  boolean isWaiting();

  String getCurrentRoomId();

  @LiveInviteViewHeader.HeaderType int getSectionType();
}