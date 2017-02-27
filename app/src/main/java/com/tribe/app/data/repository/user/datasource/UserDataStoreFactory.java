package com.tribe.app.data.repository.user.datasource;

import android.content.Context;
import com.tribe.app.data.cache.ContactCache;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.LoginApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.repository.user.contact.RxContacts;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import java.text.SimpleDateFormat;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;

/**
 * Factory that creates different implementations of {@link UserDataStoreFactory}.
 */
@Singleton public class UserDataStoreFactory {

  private final Context context;
  private final UserCache userCache;
  private final LiveCache liveCache;
  private final ContactCache contactCache;
  private final RxContacts rxContacts;
  private final RxFacebook rxFacebook;
  private final TribeApi tribeApi;
  private final LoginApi loginApi;
  private final AccessToken accessToken;
  private final Installation installation;
  private final ReactiveLocationProvider reactiveLocationProvider;
  private final SimpleDateFormat utcSimpleDate;

  @Inject public UserDataStoreFactory(Context context, UserCache userCache, LiveCache liveCache,
      ContactCache contactCache, RxContacts rxContacts, RxFacebook rxFacebook, TribeApi tribeApi,
      LoginApi loginApi, AccessToken accessToken, Installation installation,
      ReactiveLocationProvider reactiveLocationProvider,
      @Named("utcSimpleDate") SimpleDateFormat utcSimpleDate) {

    if (context == null || userCache == null) {
      throw new IllegalArgumentException("Constructor parameters cannot be null!");
    }

    this.context = context.getApplicationContext();
    this.userCache = userCache;
    this.liveCache = liveCache;
    this.contactCache = contactCache;
    this.rxContacts = rxContacts;
    this.rxFacebook = rxFacebook;
    this.tribeApi = tribeApi;
    this.loginApi = loginApi;
    this.accessToken = accessToken;
    this.installation = installation;
    this.reactiveLocationProvider = reactiveLocationProvider;
    this.utcSimpleDate = utcSimpleDate;
  }

  /**
   * Create {@link UserDataStore}
   */
  public UserDataStore createDiskDataStore() {
    return new DiskUserDataStore(userCache, liveCache, accessToken, contactCache);
  }

  /**
   * Create {@link UserDataStore} to retrieve data from the Cloud.
   */
  public UserDataStore createCloudDataStore() {
    return new CloudUserDataStore(this.userCache, this.contactCache, this.liveCache,
        this.rxContacts, this.rxFacebook, this.tribeApi, this.loginApi, this.accessToken,
        this.installation, this.context, this.utcSimpleDate);
  }
}
