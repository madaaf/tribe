package com.tribe.app.domain.entity;

import com.tribe.tribelivesdk.model.TribeGuest;
import java.io.Serializable;
import java.util.List;

/**
 * Created by madaaflak on 12/04/2017.
 */

public class RoomMember implements Serializable {

  private List<TribeGuest> peopleInRoom;
  private List<TribeGuest> anonymousInRoom;
  private List<TribeGuest> externalInRoom;

  public RoomMember(List<TribeGuest> peopleInRoom, List<TribeGuest> anonymousInRoom,
      List<TribeGuest> externalInRoom) {
    this.peopleInRoom = peopleInRoom;
    this.anonymousInRoom = anonymousInRoom;
    this.externalInRoom = externalInRoom;
  }

  public List<TribeGuest> getPeopleInRoom() {
    return peopleInRoom;
  }

  public List<TribeGuest> getExternalInRoom() {
    return externalInRoom;
  }

  public List<TribeGuest> getAnonymousInRoom() {
    return anonymousInRoom;
  }
}
