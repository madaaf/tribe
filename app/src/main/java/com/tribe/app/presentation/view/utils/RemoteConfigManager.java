package com.tribe.app.presentation.view.utils;

import android.content.Context;
import com.f2prateek.rx.preferences.BuildConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.StringUtils;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 05/30/2017.
 */

public class RemoteConfigManager {

  private static RemoteConfigManager instance;

  public static RemoteConfigManager getInstance(Context context) {
    if (instance == null) {
      instance = new RemoteConfigManager(context);
    }

    return instance;
  }

  // VARIABLES
  private Context context;
  private FirebaseRemoteConfig firebaseRemoteConfig;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public RemoteConfigManager(Context context) {
    this.context = context;
    firebaseRemoteConfig = firebaseRemoteConfig.getInstance();
    FirebaseRemoteConfigSettings configSettings =
        new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build();
    firebaseRemoteConfig.setConfigSettings(configSettings);
    firebaseRemoteConfig.setDefaults(R.xml.firebase_default_config);
    fetch();
  }

  /**
   * PRIVATE
   */

  /**
   * PUBLIC
   */

  public void fetch() {
    firebaseRemoteConfig.fetch(3600).addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        firebaseRemoteConfig.activateFetched();
      }
    });
  }

  public String getString(String key, String value) {
    String result = firebaseRemoteConfig.getString(key);
    if (StringUtils.isEmpty(result)) result = value;
    return result;
  }

  public double getDouble(String key) {
    return firebaseRemoteConfig.getDouble(key);
  }

  public int getInt(String key) {
    String str = firebaseRemoteConfig.getString(key);
    if (StringUtils.isEmpty(str)) return 0;
    return Integer.parseInt(str);
  }

  /**
   * OBSERVABLES
   */
}
