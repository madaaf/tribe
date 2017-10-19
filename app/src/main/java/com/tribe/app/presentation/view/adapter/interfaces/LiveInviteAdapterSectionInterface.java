package com.tribe.app.presentation.view.adapter.interfaces;

import com.tribe.app.presentation.view.adapter.decorator.BaseSectionItemDecoration;

public interface LiveInviteAdapterSectionInterface {

  String getId();

  boolean isOnline();

  boolean isRinging();

  String getCurrentRoomId();

  @BaseSectionItemDecoration.HeaderType int getLiveInviteSectionType();
}