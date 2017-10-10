package com.tribe.app.presentation.view.adapter.interfaces;

import com.tribe.app.presentation.view.adapter.decorator.BaseSectionItemDecoration;

public interface LiveInviteAdapterSectionInterface {

  String getId();

  boolean isOnline();

  boolean isRinging();

  boolean isWaiting();

  String getCurrentRoomId();

  @BaseSectionItemDecoration.HeaderType int getLiveInviteSectionType();
}