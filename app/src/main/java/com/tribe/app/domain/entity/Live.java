package com.tribe.app.domain.entity;

import android.support.annotation.StringDef;
import com.tribe.app.presentation.view.activity.LiveActivity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 23/02/2017.
 */

public class Live implements Serializable {

  @StringDef({ NEW_CALL, WEB, FRIEND_CALL }) public @interface LiveType {
  }

  public static final String NEW_CALL = "NEW_CALL";
  public static final String WEB = "WEB";
  public static final String FRIEND_CALL = "FRIEND_CALL";

  private @LiveType String type;
  private Room room;
  private String linkId;
  private String shortcutId;
  private boolean fromRoom = false;
  private List<User> usersOfShortcut;
  private List<User> usersOfRoom;
  private List<String> userIdsOfShortcut;
  private List<String> userIdsOfRoom;
  private String url;
  private boolean countdown = true;
  private boolean intent = false;
  private @LiveActivity.Source String source;
  private boolean isDiceDragedInRoom = false;
  private Shortcut shortcut;
  private String section;
  private String gesture;

  private transient CompositeSubscription subscriptions;
  private transient PublishSubject<Room> onRoomUpdated;
  private transient PublishSubject<Shortcut> onShortcutUpdated;

  private Live(Builder builder) {
    this.room = builder.room;
    this.fromRoom = room != null;
    this.linkId = builder.linkId;
    setUsersOfShortcut(builder.users);
    this.type = builder.type;
    this.countdown = builder.countdown;
    this.intent = builder.intent;
    this.url = builder.url;
    this.source = builder.source;
    this.isDiceDragedInRoom = builder.isDiceDragedInRoom;
    this.shortcut = builder.shortcut;
    this.gesture = builder.gesture;
    this.section = builder.section;
  }

  public void init() {
    subscriptions = new CompositeSubscription();
    onRoomUpdated = PublishSubject.create();
    onShortcutUpdated = PublishSubject.create();
  }

  public void dispose() {
    if (subscriptions != null) subscriptions.clear();
    if (room != null) room.dispose();
  }

  public Room getRoom() {
    return room;
  }

  public void setRoom(Room room) {
    this.room = room;

    if (this.room.getRoomCoordinates() != null) {
      onRoomUpdated.onNext(room);
      this.room.onRoomUpdated().subscribe(onRoomUpdated);
    }

    if (room.getShortcut() != null) {
      setShortcut(room.getShortcut());
    } else if (shortcut != null) room.setShortcut(shortcut);

    List<User> temp = new ArrayList<>();
    temp.addAll(room.getLiveUsers());
    temp.addAll(room.getInvitedUsers());
    setUsersOfRoom(temp);
  }

  public Shortcut getShortcut() {
    return shortcut;
  }

  public void setShortcut(Shortcut shortcut) {
    this.shortcut = shortcut;
    if (room != null) this.room.setShortcut(shortcut);
    if (onShortcutUpdated != null) onShortcutUpdated.onNext(this.shortcut);
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isDiceDragedInRoom() {
    return isDiceDragedInRoom;
  }

  public void setDiceDragedInRoom(boolean diceDragedInRoom) {
    isDiceDragedInRoom = diceDragedInRoom;
  }

  public String getRoomId() {
    return room != null ? room.getId() : null;
  }

  public void setUsersOfShortcut(List<User> users) {
    this.usersOfShortcut = users;

    userIdsOfShortcut = new ArrayList<>();

    if (users != null) {
      for (User user : users) {
        userIdsOfShortcut.add(user.getId());
      }
    }
  }

  public void setUsersOfRoom(List<User> users) {
    this.usersOfRoom = users;

    userIdsOfRoom = new ArrayList<>();

    if (users != null) {
      for (User user : users) {
        userIdsOfShortcut.add(user.getId());
      }
    }
  }

  public List<User> getUsersOfShortcut() {
    return usersOfShortcut;
  }

  public List<User> getUsersOfRoom() {
    return usersOfRoom;
  }

  public List<String> getUserIdsOfShortcut() {
    return userIdsOfShortcut;
  }

  public List<String> getUserIdsOfRoom() {
    return userIdsOfRoom;
  }

  public boolean hasUsers() {
    return usersOfShortcut != null && usersOfShortcut.size() > 0;
  }

  public boolean hasUser(String userId) {
    return usersOfShortcut != null && usersOfShortcut.contains(userId);
  }

  public void setCallRouletteSessionId(String sessionId) {
    setRoom(new Room(sessionId));
    setUrl(null);
  }

  public boolean isCountdown() {
    return countdown;
  }

  public void setCountdown(boolean countdown) {
    this.countdown = countdown;
  }

  public boolean isIntent() {
    return intent;
  }

  public boolean hasRoom() {
    return room != null;
  }

  public boolean fromRoom() {
    return fromRoom;
  }

  public boolean hasRoomId() {
    return room != null;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @LiveActivity.Source public String getSource() {
    return source;
  }

  public void setSource(@LiveActivity.Source String source) {
    this.source = source;
  }

  public String getGesture() {
    return gesture;
  }

  public void setGesture(String gesture) {
    this.gesture = gesture;
  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public String getLinkId() {
    return linkId;
  }

  public String getShortcutId() {
    return shortcutId;
  }

  public void setShortcutId(String shortcutId) {
    this.shortcutId = shortcutId;
  }

  public static class Builder {

    private Room room;
    private String linkId;
    private @LiveType String type;
    private List<User> users;
    private String url;
    private int color;
    private boolean countdown = true;
    private boolean intent = false;
    private boolean isDiceDragedInRoom = false;
    private @LiveActivity.Source String source;
    private String gesture;
    private String section;
    private Shortcut shortcut;

    public Builder(@LiveType String type) {
      this.type = type;
    }

    public Builder room(Room room) {
      this.room = room;
      return this;
    }

    public Builder users(User... users) {
      this.users = new ArrayList<>(Arrays.asList(users));
      return this;
    }

    public Builder shortcut(Shortcut shortcut) {
      this.shortcut = shortcut;
      if (shortcut != null && shortcut.getMembers() != null) {
        this.users(shortcut.getMembers().toArray(new User[shortcut.getMembers().size()]));
      }
      return this;
    }

    public Builder roomId(String roomId) {
      this.room = new Room(roomId);
      return this;
    }

    public Builder linkId(String linkId) {
      this.linkId = linkId;
      return this;
    }

    public Builder isDiceDragedInRoom(boolean isDiceDragedInRoom) {
      this.isDiceDragedInRoom = isDiceDragedInRoom;
      return this;
    }

    public Builder url(String url) {
      this.url = url;
      return this;
    }

    public Builder countdown(boolean countdown) {
      this.countdown = countdown;
      return this;
    }

    public Builder intent(boolean fromIntent) {
      this.intent = fromIntent;
      return this;
    }

    public Builder source(@LiveActivity.Source String source) {
      this.source = source;
      return this;
    }

    public Builder gesture(String gesture) {
      this.gesture = gesture;
      return this;
    }

    public Builder section(String section) {
      this.section = section;
      return this;
    }

    public Live build() {
      return new Live(this);
    }
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Room> onRoomUpdated() {
    return onRoomUpdated.onBackpressureDrop().observeOn(AndroidSchedulers.mainThread());
  }

  public Observable<Shortcut> onShortcutUpdated() {
    return onShortcutUpdated;
  }
}
