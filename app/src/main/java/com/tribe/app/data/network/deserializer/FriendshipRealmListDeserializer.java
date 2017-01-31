package com.tribe.app.data.network.deserializer;

import com.tribe.app.data.cache.UserCache;
import com.tribe.app.domain.entity.User;

import java.text.SimpleDateFormat;

/**
 * Created by tiago on 31/08/2016.
 */
public class FriendshipRealmListDeserializer {

  protected SimpleDateFormat utcSimpleDate;
  protected User currentUser;
  protected UserCache userCache;

  public FriendshipRealmListDeserializer(SimpleDateFormat utcSimpleDate, UserCache userCache,
      User currentUser) {
    this.utcSimpleDate = utcSimpleDate;
    this.userCache = userCache;
    this.currentUser = currentUser;
  }
}
