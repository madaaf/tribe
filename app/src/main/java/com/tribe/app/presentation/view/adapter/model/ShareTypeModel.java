package com.tribe.app.presentation.view.adapter.model;

import com.tribe.app.presentation.view.adapter.decorator.BaseSectionItemDecoration;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import java.io.Serializable;

/**
 * Created by tiago on 10/10/2017.
 */

public class ShareTypeModel implements Serializable, LiveInviteAdapterSectionInterface {

  public static final String SHARE_TYPE_MESSENGER = "SHARE_TYPE_MESSENGER";
  public static final String SHARE_TYPE_SMS = "SHARE_TYPE_SMS";

  private String id;
  private int resourceDrawableId;

  public ShareTypeModel(String id, int resourceDrawableId) {
    this.id = id;
    this.resourceDrawableId = resourceDrawableId;
  }

  public int getResourceDrawableId() {
    return resourceDrawableId;
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

  @Override public String getCurrentRoomId() {
    return null;
  }

  @Override public int getLiveInviteSectionType() {
    return id.equals(SHARE_TYPE_MESSENGER) ? BaseSectionItemDecoration.LIVE_SHARE_MESSENGER
        : BaseSectionItemDecoration.LIVE_SHARE_SMS;
  }
}
