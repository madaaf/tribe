package com.tribe.app.domain.entity;

import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

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
  private transient ObservableRxHashMap<String, User> liveUsersMap;
  private transient ObservableRxHashMap<String, User> invitedUsersMap;

  private transient CompositeSubscription subscriptions = new CompositeSubscription();
  private transient PublishSubject<User> onAddedInvitedUser = PublishSubject.create();
  private transient PublishSubject<User> onRemovedInvitedUser = PublishSubject.create();
  private transient PublishSubject<User> onAddedLiveUser = PublishSubject.create();
  private transient PublishSubject<User> onRemovedLiveUser = PublishSubject.create();

  public Room() {
    init();
  }

  public Room(String id) {
    this.id = id;
    init();
  }

  private void init() {
    liveUsersMap = new ObservableRxHashMap<>();
    invitedUsersMap = new ObservableRxHashMap<>();
    invited_users = new ArrayList<>();
    live_users = new ArrayList<>();

    subscriptions.add(liveUsersMap.getObservable().doOnNext(rxLiveUserMap -> {
      if (rxLiveUserMap.changeType == ObservableRxHashMap.ADD) {
        onAddedLiveUser.onNext(rxLiveUserMap.item);
      } else if (rxLiveUserMap.changeType == ObservableRxHashMap.REMOVE) {
        onRemovedLiveUser.onNext(rxLiveUserMap.item);
      }
    }).subscribe());

    subscriptions.add(invitedUsersMap.getObservable().doOnNext(rxLiveUserMap -> {
      if (rxLiveUserMap.changeType == ObservableRxHashMap.ADD) {
        onAddedInvitedUser.onNext(rxLiveUserMap.item);
      } else if (rxLiveUserMap.changeType == ObservableRxHashMap.REMOVE) {
        onRemovedInvitedUser.onNext(rxLiveUserMap.item);
      }
    }).subscribe());
  }

  public void dispose() {
    subscriptions.clear();
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
    List<User> newLiveUsers = room.getLiveUsers();
    if (newLiveUsers != null && newLiveUsers.size() != liveUsersMap.size()) {
      computeUsersChanges(liveUsersMap, newLiveUsers);
    }

    live_users.clear();
    live_users.addAll(newLiveUsers);

    List<User> newInvitedUsers = room.getInvitedUsers();
    if (room.getInvitedUsers() != null && room.getInvitedUsers().size() != invitedUsersMap.size()) {
      computeUsersChanges(invitedUsersMap, newInvitedUsers);
    }

    invited_users.clear();
    invited_users.addAll(newInvitedUsers);
  }

  private void computeUsersChanges(ObservableRxHashMap<String, User> map, List<User> updatedUsers) {
    Map<String, User> previousUsers = map.getMap();

    if (previousUsers.size() > updatedUsers.size()) {
      // Somebody left
      for (String id : previousUsers.keySet()) {
        boolean found = true;

        for (User user : updatedUsers) {
          if (!id.equals(user.getId())) found = false;
        }

        if (!found) map.remove(id);
      }
    } else {
      // Somebody was added
      for (User user : updatedUsers) {
        map.put(user.getId(), user);
      }
    }
  }

  public Observable<User> onAddedLiveUser() {
    return onAddedLiveUser;
  }

  public Observable<User> onAddedInvitedUser() {
    return onAddedInvitedUser;
  }

  public Observable<User> onRemovedLiveUser() {
    return onRemovedLiveUser;
  }

  public Observable<User> onRemovedInvitedUser() {
    return onRemovedInvitedUser;
  }

  @Override public String toString() {
    return "id : " + id + "\n" + "coordinates : " + coordinates;
  }
}
