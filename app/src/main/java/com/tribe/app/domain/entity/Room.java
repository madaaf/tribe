package com.tribe.app.domain.entity;

import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.decorator.BaseSectionItemDecoration;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 30/01/2017.
 */

public class Room implements Serializable, LiveInviteAdapterSectionInterface {

  private static final int NB_MAX_CHARS = 17;

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
  private List<User> all_users;
  private Date created_at;
  private Date updated_at;
  private transient ObservableRxHashMap<String, User> liveUsersMap;
  private transient ObservableRxHashMap<String, User> invitedUsersMap;
  private Set<String> waitingIds;
  private Shortcut shortcut;

  private transient CompositeSubscription subscriptions;
  private transient PublishSubject<User> onAddedInvitedUser = PublishSubject.create();
  private transient PublishSubject<User> onRemovedInvitedUser = PublishSubject.create();
  private transient PublishSubject<User> onAddedLiveUser = PublishSubject.create();
  private transient PublishSubject<User> onRemovedLiveUser = PublishSubject.create();
  private transient PublishSubject<Room> onRoomUpdated = PublishSubject.create();

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
    waitingIds = new HashSet<>();
    all_users = new ArrayList<>();
    subscriptions = new CompositeSubscription();

    subscriptions.add(liveUsersMap.getObservable().doOnNext(rxLiveUserMap -> {
      if (rxLiveUserMap.changeType == ObservableRxHashMap.ADD) {
        onAddedLiveUser.onNext(rxLiveUserMap.item);
      } else if (rxLiveUserMap.changeType == ObservableRxHashMap.REMOVE) {
        onRemovedLiveUser.onNext(rxLiveUserMap.item);
      }
    }).subscribe());

    subscriptions.add(invitedUsersMap.getObservable().doOnNext(rxInvitedUserMap -> {
      if (rxInvitedUserMap.changeType == ObservableRxHashMap.ADD) {
        onAddedInvitedUser.onNext(rxInvitedUserMap.item);
      } else if (rxInvitedUserMap.changeType == ObservableRxHashMap.REMOVE) {
        onRemovedInvitedUser.onNext(rxInvitedUserMap.item);
      }
    }).subscribe());
  }

  public void dispose() {
    if (subscriptions != null) subscriptions.clear();
  }

  public String getId() {
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

  public void setShortcut(Shortcut shortcut) {
    this.shortcut = shortcut;
    onRoomUpdated.onNext(this);
  }

  public Shortcut getShortcut() {
    return shortcut;
  }

  public String getUserNames() {
    return StringUtils.constrainUsersStr(getAllUsers(), NB_MAX_CHARS, true);
  }

  public synchronized void onJoinSuccess(User currentUser) {
    live_users.add(currentUser);
    update(this, true);
  }

  public synchronized void update(Room room, boolean shouldOverwrite) {
    this.name = room.name;
    this.accept_random = room.accept_random;

    //if (room.getShortcut() != null) setShortcut(room.getShortcut());

    List<User> newLiveUsers = room.getLiveUsers();
    //if (newLiveUsers == null || newLiveUsers.size() != liveUsersMap.size()) {
    //  computeUsersChanges(currentUser, liveUsersMap, newLiveUsers);
    //}

    if (shouldOverwrite) {
      live_users.clear();
      live_users.addAll(newLiveUsers);
    }

    List<User> newInvitedUsers = room.getInvitedUsers();
    //if (newInvitedUsers == null || newInvitedUsers.size() != invitedUsersMap.size()) {
    //  computeUsersChanges(currentUser, invitedUsersMap, newInvitedUsers);
    //}

    if (shouldOverwrite) {
      invited_users.clear();
      invited_users.addAll(newInvitedUsers);
    }

    if (shouldOverwrite) {
      all_users.clear();
      all_users.addAll(newLiveUsers);
      all_users.addAll(newInvitedUsers);
    }

    onRoomUpdated.onNext(this);
  }

  private void computeUsersChanges(User currentUser, ObservableRxHashMap<String, User> map,
      List<User> updatedUsers) {
    Map<String, User> previousUsers = map.getMap();

    if (updatedUsers == null || updatedUsers.size() == 0) {
      for (String key : map.getMap().keySet()) {
        map.remove(key, true);
      }

      return;
    }

    if (previousUsers.size() > updatedUsers.size()) {
      // Somebody left
      for (String id : previousUsers.keySet()) {
        boolean found = true;

        for (User user : updatedUsers) {
          if (!id.equals(user.getId())) found = false;
        }

        if (!found) map.remove(id, true);
      }
    } else {
      // Somebody was added
      for (User user : updatedUsers) {
        if (updatedUsers != invited_users || !user.equals(currentUser)) map.put(user.getId(), user);
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

  /*
  @Override public String toString() {
    return "id : " + id + "\n" + "coordinates : " + coordinates;
  }
  */

  @Override public String toString() {
    return "Room{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + ", initiator="
        + initiator
        + ", live_users="
        + live_users
        + ", invited_users="
        + invited_users
        + ", all_users="
        + all_users
        + ", shortcut "
        + shortcut
        + ", onAddedInvitedUser="
        + onAddedInvitedUser
        + ", onRemovedInvitedUser="
        + onRemovedInvitedUser
        + ", onAddedLiveUser="
        + onAddedLiveUser
        + ", onRemovedLiveUser="
        + onRemovedLiveUser
        + ", onRoomUpdated="
        + onRoomUpdated
        + '}';
  }

  public List<String> getUserIds() {
    List<String> memberIds = new ArrayList<>();

    for (User user : live_users) {
      memberIds.add(user.getId());
    }

    for (User user : invited_users) {
      memberIds.add(user.getId());
    }

    if (initiator != null) {
      if (!memberIds.contains(initiator.getId())) memberIds.add(initiator.getId());
    }

    return memberIds;
  }

  public List<User> getAllUsers() {
    if (all_users.size() == 0) {
      all_users.addAll(live_users);
      all_users.addAll(invited_users);
    }

    return all_users;
  }

  public int nbUsersLive() {
    if (live_users == null) return 0;
    return live_users.size();
  }

  public int nbUsersInvited() {
    if (invited_users == null) return 0;
    return invited_users.size();
  }

  public int nbUsersTotal() {
    int total = 0;
    if (invited_users != null) total += invited_users.size();
    if (live_users != null) total += live_users.size();
    return total;
  }

  public int nbUsersTotalWithoutMe(String currentUserId) {
    int total = 0;

    if (invited_users != null) {
      for (User user : invited_users) {
        if (!currentUserId.equals(user.getId())) total++;
      }
    }

    if (live_users != null) {
      for (User user : live_users) {
        if (!currentUserId.equals(user.getId())) total++;
      }
    }

    return total;
  }

  public void userJoinedWebRTC(String id) {
    waitingIds.add(id);
  }

  public void userJoinedStream(String id) {
    waitingIds.remove(id);
  }

  public void userLeftWebRTC(String id) {
    waitingIds.remove(id);
  }

  public boolean isUserWaiting(String id) {
    return waitingIds.contains(id);
  }

  public boolean isUserInitiator(String id) {
    if (initiator == null) return false;
    else return initiator.getId().equals(id);
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getId() != null ? getId().hashCode() : 0);
    return result;
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Room> onRoomUpdated() {
    return onRoomUpdated.onBackpressureDrop().observeOn(AndroidSchedulers.mainThread());
  }

  @Override public int getLiveInviteSectionType() {
    return BaseSectionItemDecoration.LIVE_CHAT_MEMBERS;
  }
}
