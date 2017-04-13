package com.tribe.app.domain.entity;

import com.tribe.tribelivesdk.model.TribeGuest;
import java.io.Serializable;
import java.util.List;

/**
 * Created by madaaflak on 12/04/2017.
 */

public class RoomMember implements Serializable {

  private List<TribeGuest> tribeGuestList;
  private List<TribeGuest> anonymousGuestList;

  public RoomMember(List<TribeGuest> tribeGuestList, List<TribeGuest> anonymousGuestList) {
    this.tribeGuestList = tribeGuestList;
    this.anonymousGuestList = anonymousGuestList;
  }

  public List<TribeGuest> getTribeGuestList() {
    return tribeGuestList;
  }

  public List<TribeGuest> getAnonymousGuestList() {
    return anonymousGuestList;
  }
}
