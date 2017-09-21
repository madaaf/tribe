package com.tribe.app.presentation.view.widget.header;

import android.support.annotation.IntDef;

/**
 * Created by tiago on 09/19/17.
 */
public class LiveInviteViewHeader {

  @IntDef({ CHAT_MEMBERS, INVITE_LINK, ADD_FRIENDS_IN_CALL }) public @interface HeaderType {
  }

  public static final int CHAT_MEMBERS = 0;
  public static final int INVITE_LINK = 1;
  public static final int ADD_FRIENDS_IN_CALL = 2;
}
