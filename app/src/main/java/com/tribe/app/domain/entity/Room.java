package com.tribe.app.domain.entity;

import com.tribe.app.domain.entity.helpers.ChangeHelper;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by tiago on 30/01/2017.
 */

public class Room implements Serializable {

  public static final String NAME = "name";
  public static final String ACCEPT_RANDOM = "accept_random";

  private String id;
  private String name;
  private String link;
  private RoomCoordinates coordinates;
  private boolean accept_random;
  private User initiator;
  private List<User> live_users;
  private List<User> invited_users;
  private Date created_at;
  private Date updated_at;
  private ChangeHelper<List<User>> liveUsersChangeHelper;
  private ChangeHelper<List<User>> invitedUsersChangeHelper;

  private PublishSubject<List<User>> onAddedLiveUser = PublishSubject.create();
  private PublishSubject<List<User>> onRemovedLiveUser = PublishSubject.create();
  private PublishSubject<List<User>> onAddedInvitedUser = PublishSubject.create();
  private PublishSubject<List<User>> onRemovedInvitedUser = PublishSubject.create();

  public Room() {
    init();
  }

  public Room(String id) {
    this.id = id;
    init();
  }

  private void init() {
    liveUsersChangeHelper = new ChangeHelper<>();
    invitedUsersChangeHelper = new ChangeHelper<>();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public RoomCoordinates getRoomCoordinates() {
    return coordinates;
  }

  public void setRoomCoordinates(RoomCoordinates roomCoordinates) {
    this.coordinates = roomCoordinates;
  }

  public boolean acceptsRandom() {
    return accept_random;
  }

  public void setAcceptRandom(boolean acceptRandom) {
    this.accept_random = acceptRandom;
  }

  public User getInitiator() {
    return initiator;
  }

  public void setInitiator(User initiator) {
    this.initiator = initiator;
  }

  public List<User> getLiveUsers() {
    return live_users;
  }

  public void setLiveUsers(List<User> liveUsers) {
    this.live_users = liveUsers;
  }

  public List<User> getInvitedUsers() {
    return invited_users;
  }

  public void setInvitedUsers(List<User> invitedUsers) {
    this.invited_users = invitedUsers;
  }

  public Date getCreatedAt() {
    return created_at;
  }

  public void setCreated_at(Date createdAt) {
    this.created_at = createdAt;
  }

  public Date getUpdated_at() {
    return updated_at;
  }

  public void setUpdated_at(Date updatedAt) {
    this.updated_at = updatedAt;
  }

  public void update(Room room) {
    if (room.getLiveUsers() != null && liveUsersChangeHelper.filter(room.getLiveUsers())) {
      Timber.d("New live users");
    }

    if (room.getInvitedUsers() != null && invitedUsersChangeHelper.filter(room.getInvitedUsers())) {
      Timber.d("New invited users");
    }
  }

  @Override public String toString() {
    return "id : " + id + "\n" + "coordinates : " + coordinates;
  }

  public Observable<List<User>> onAddedLiveUser() {
    return onAddedLiveUser;
  }

  public Observable<List<User>> onRemovedLiveUser() {
    return onRemovedLiveUser;
  }

  public Observable<List<User>> onAddedInvitedUser() {
    return onAddedInvitedUser;
  }

  public Observable<List<User>> onRemovedInvitedUser() {
    return onRemovedInvitedUser;
  }
}
