package com.tribe.app.presentation.view.adapter.model;

import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import java.io.Serializable;

/**
 * Created by tiago on 10/10/2017.
 */

public class Header implements Serializable, LiveInviteAdapterSectionInterface {

  public static final String HEADER_NAME = "HEADER_NAME";
  public static final String HEADER_CHAT_MEMBERS = "HEADER_CHAT_MEMBERS";
  public static final String HEADER_DRAG_IN = "HEADER_DRAG_IN";
  public static final String HEADER_ONLINE = "HEADER_ONLINE";
  public static final String HEADER_RECENT = "HEADER_RECENT";

  private String id;
  private String resourceTxtId;
  private int resourceDrawableId;
  private int gravity;

  public Header(String id, String resourceTxtId, int resourceDrawableId, int gravity) {
    this.id = id;
    this.resourceTxtId = resourceTxtId;
    this.resourceDrawableId = resourceDrawableId;
    this.gravity = gravity;
  }

  public int getResourceDrawableId() {
    return resourceDrawableId;
  }

  public String getResourceTxtId() {
    return resourceTxtId;
  }

  @Override public String getId() {
    return id;
  }

  @Override public boolean isOnline() {
    return false;
  }

  @Override public boolean isRinging() {
    return false;
  }

  @Override public boolean isWaiting() {
    return false;
  }

  @Override public String getCurrentRoomId() {
    return null;
  }

  @Override public int getLiveInviteSectionType() {
    return 0;
  }

  public int getGravity() {
    return gravity;
  }

  public void setGravity(int gravity) {
    this.gravity = gravity;
  }
}
