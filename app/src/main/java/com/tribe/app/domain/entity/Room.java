package com.tribe.app.domain.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
  private Date last_activity_at;

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

  public void setLive_users(List<User> liveUsers) {
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

  public Date getLastActivityAt() {
    return last_activity_at;
  }

  public void setLastActivityAt(Date lastActivityAt) {
    this.last_activity_at = lastActivityAt;
  }

  @Override public String toString() {
    return "id : " + id + "\n" + "coordinates : " + coordinates;
  }
}
