package com.tribe.app.presentation;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.stetho.Stetho;
import com.jenzz.appstate.AppState;
import com.jenzz.appstate.AppStateListener;
import com.jenzz.appstate.AppStateMonitor;
import com.jenzz.appstate.RxAppStateMonitor;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactFBRealm;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.LocationRealm;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.PhoneRealm;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.mapper.GameRealmDataMapper;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerApplicationComponent;
import com.tribe.app.presentation.internal.di.modules.ApplicationModule;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.IntentUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.LauncherActivity;
import com.tribe.tribelivesdk.facetracking.UlseeManager;
import com.tribe.tribelivesdk.filters.Filter;
import com.tribe.tribelivesdk.filters.lut3d.FilterManager;
import com.tribe.tribelivesdk.filters.lut3d.LUT3DFilter;
import com.tribe.tribelivesdk.game.GameManager;
import io.branch.referral.Branch;
import io.fabric.sdk.android.Fabric;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import io.realm.exceptions.RealmMigrationNeededException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.danlew.android.joda.JodaTimeAndroid;
import timber.log.Timber;

import static com.tribe.app.presentation.view.utils.StateManager.FACEBOOK_CONTACT_PERMISSION;

/**
 * Android Main Application
 */
public class AndroidApplication extends Application {

  // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
  private static final String TWITTER_KEY = "DZYpDkxG4R2zf7Arrdyzg1rIE";
  private static final String TWITTER_SECRET = "hwhu6tFkc1acna8gTWXJ504PjRB6QuPswWX37ASsRFThhMVxU8";

  private ApplicationComponent applicationComponent;
  private AppStateMonitor appStateMonitor;
  private AppState appState;

  @Override public void onCreate() {
    super.onCreate();
    initTimber();
    initInjector();
    // initLeakDetection();
    initRealm();
    initBadger();
    initStetho();
    initFacebook();
    initBranch();
    initAppState();
    initTakt();
    initUlsee();
    initFilters();
    initGameManager();
    JodaTimeAndroid.init(this);
  }

  @Override protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(base);
  }

  @Override public void onTerminate() {
    super.onTerminate();
  }

  private void initInjector() {
    this.applicationComponent =
        DaggerApplicationComponent.builder().applicationModule(new ApplicationModule(this)).build();
  }

  public ApplicationComponent getApplicationComponent() {
    return this.applicationComponent;
  }

  private void initLeakDetection() {
    //if (BuildConfig.DEBUG) {
    //  if (LeakCanary.isInAnalyzerProcess(this)) {
    //    // This process is dedicated to LeakCanary for heap analysis.
    //    // You should not init your app in this process.
    //    return;
    //  }
    //
    //  LeakCanary.install(this);
    //}
  }

  private void initStetho() {
    if (BuildConfig.DEBUG) {
      Stetho.initializeWithDefaults(this);
    }
  }

  private void initRealm() {
    Realm.init(this);
    prepareRealm();
  }

  private void prepareRealm() {
    RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().schemaVersion(12)
        .migration((realm, oldVersion, newVersion) -> {
          RealmSchema schema = realm.getSchema();

          if (oldVersion == 6) {
            RealmObjectSchema contactABSchema = schema.get("ContactABRealm");
            if (!contactABSchema.hasField("isNew")) {
              contactABSchema.addField("isNew", boolean.class);
            }

            RealmObjectSchema contactFBSchema = schema.get("ContactFBRealm");
            if (!contactFBSchema.hasField("isNew")) {
              contactFBSchema.addField("isNew", boolean.class);
            }

            oldVersion++;
          }

          if (oldVersion == 7) {
            RealmObjectSchema contactABSchema = schema.get("ContactABRealm");
            if (!contactABSchema.hasField("firstName")) {
              contactABSchema.addField("firstName", String.class);
            }

            if (!contactABSchema.hasField("lastName")) {
              contactABSchema.addField("lastName", String.class);
            }

            oldVersion++;
          }

          if (oldVersion == 8) {
            if (schema.get("AudioResourceRealm") == null) {
              schema.create("AudioResourceRealm")
                  .addField("url", String.class)
                  .addField("duration", Float.class)
                  .addField("filesize", Integer.class);

              schema.create("BadgeRealm")
                  .addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                  .addField("value", int.class);

              schema.create("ImageRealm")
                  .addField("url", String.class, FieldAttribute.PRIMARY_KEY)
                  .addField("filesize", Integer.class)
                  .addField("width", String.class)
                  .addField("height", String.class)
                  .addField("duration", float.class);

              schema.create("MessageRealm")
                  .addField("localId", String.class)
                  .addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                  .addRealmObjectField("author", schema.get("UserRealm"))
                  .addRealmObjectField("user", schema.get("UserRealm"))
                  .addField("data", String.class)
                  .addField("__typename", String.class)
                  .addRealmObjectField("original", schema.get("ImageRealm"))
                  .addRealmListField("alts", schema.get("ImageRealm"))
                  .addField("action", String.class)
                  .addField("created_at", String.class)
                  .addField("threadId", String.class);

              schema.create("ShortcutLastSeenRealm")
                  .addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                  .addField("user_id", String.class)
                  .addField("date", String.class);

              schema.create("ShortcutRealm")
                  .addField("id", String.class, FieldAttribute.PRIMARY_KEY, FieldAttribute.INDEXED)
                  .addField("name", String.class)
                  .addField("picture", String.class)
                  .addField("pinned", Boolean.class, FieldAttribute.REQUIRED)
                  .addField("read", Boolean.class, FieldAttribute.REQUIRED)
                  .addField("mute", Boolean.class, FieldAttribute.REQUIRED)
                  .addField("status", String.class, FieldAttribute.INDEXED)
                  .addField("single", Boolean.class, FieldAttribute.INDEXED,
                      FieldAttribute.REQUIRED)
                  .addRealmListField("last_seen", schema.get("ShortcutLastSeenRealm"))
                  .addField("created_at", Date.class)
                  .addField("last_activity_at", Date.class)
                  .addRealmListField("members", schema.get("UserRealm"))
                  .addField("lastMessage", String.class)
                  .addField("leaveOnlineUntil", Date.class)
                  .addField("membersHash", String.class);

              schema.get("UserRealm")
                  .addRealmListField("messages", schema.get("MessageRealm"))
                  .addField("random_banned_until", Date.class)
                  .addField("random_banned_permanently", Boolean.class)
                  .removeField("friendships")
                  .removeField("memberships");

              schema.get("SearchResultRealm")
                  .addRealmObjectField("shortcutRealm", schema.get("ShortcutRealm"))
                  .removeField("friendshipRealm");

              schema.get("ContactFBRealm").addField("hasApp", boolean.class);

              schema.remove("MembershipRealm");
              schema.remove("GroupRealm");
              schema.remove("GroupMemberRealm");
              schema.remove("FriendshipRealm");
            }

            oldVersion++;
          }

          if (oldVersion == 9) {
            if (schema.get("GameRealm") == null) {
              schema.create("GameRealm")
                  .addField("id", String.class, FieldAttribute.PRIMARY_KEY, FieldAttribute.INDEXED)
                  .addField("online", boolean.class)
                  .addField("playable", boolean.class)
                  .addField("featured", boolean.class)
                  .addField("isNew", boolean.class)
                  .addField("title", String.class)
                  .addField("baseline", String.class)
                  .addField("icon", String.class)
                  .addField("banner", String.class)
                  .addField("primary_color", String.class)
                  .addField("secondary_color", String.class)
                  .addField("plays_count", int.class)
                  .addField("__typename", String.class)
                  .addField("url", String.class)
                  .addField("dataUrl", String.class);
            }

            oldVersion++;
          }

          if (oldVersion == 10) {
            schema.get("UserRealm").addField("mute_online_notif", boolean.class);

            oldVersion++;
          }
        })
        .build();

    Realm.setDefaultConfiguration(realmConfiguration);

    Realm realm = null;
    try {
      realm = Realm.getDefaultInstance();
    } catch (RealmMigrationNeededException e) {
      e.printStackTrace();
      Realm.deleteRealm(realmConfiguration);
    } finally {
      if (realm != null) {
        realm.close();
      }
    }
  }

  private void initBadger() {
    this.applicationComponent.badgeRealm();
  }

  private void initFacebook() {
    FacebookSdk.sdkInitialize(getApplicationContext());
    AppEventsLogger.activateApp(this);
  }

  private void initBranch() {
    Branch.getAutoInstance(this);
  }

  private void initTimber() {
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    } else {
      Timber.plant(new ProductionTree(this));
      Fabric.with(this, new Crashlytics(), new Answers());
    }
  }

  private void initAppState() {
    appStateMonitor = RxAppStateMonitor.create(this);
    appStateMonitor.addListener(new SampleAppStateListener());
    appStateMonitor.start();
  }

  private void initTakt() {
    //Takt.stock(this)
    //    .seat(Seat.BOTTOM_RIGHT)
    //    .interval(250)
    //    .color(Color.WHITE)
    //    .size(25f)
    //    .alpha(1f)
    //    .play();
  }

  private void initUlsee() {
    UlseeManager.getInstance(this);
  }

  private void initFilters() {
    FilterManager filterManager = FilterManager.getInstance(this);
    List<Filter> filterList = new ArrayList<>();
    filterList.add(new LUT3DFilter(this, LUT3DFilter.LUT3D_TAN, "Tan", R.drawable.picto_filter_tan,
        R.drawable.lut_settled, true));
    filterList.add(
        new LUT3DFilter(this, LUT3DFilter.LUT3D_HIPSTER, "Hipster", R.drawable.picto_filter_hipster,
            R.drawable.lut_pola669, true));
    filterList.add(new LUT3DFilter(this, LUT3DFilter.LUT3D_BW, "B&W", R.drawable.picto_filter_bw,
        R.drawable.lut_litho, true));
    filterManager.initFilters(filterList);
  }

  private void initGameManager() {
    GameManager gameManager = GameManager.getInstance(this);
    GameRealmDataMapper gameRealmDataMapper = new GameRealmDataMapper(this);
    List<GameRealm> gameRealmList = applicationComponent.gameCache().getGames();
    if (gameRealmList != null && gameRealmList.size() > 0) {
      gameManager.addGames(gameRealmDataMapper.transform(gameRealmList));
    }
  }

  private class SampleAppStateListener implements AppStateListener {

    @Override public void onAppDidEnterForeground() {
      appState = AppState.FOREGROUND;
    }

    @Override public void onAppDidEnterBackground() {
      appState = AppState.BACKGROUND;
    }
  }

  public AppState getAppState() {
    return appState;
  }

  public void logoutUser() {
    Realm realm = applicationComponent.realm();
    try {
      realm.executeTransaction(realm1 -> {
        realm1.delete(AccessToken.class);
        realm1.delete(ContactABRealm.class);
        realm1.delete(ContactFBRealm.class);
        realm1.delete(ShortcutRealm.class);
        realm1.delete(Installation.class);
        realm1.delete(LocationRealm.class);
        realm1.delete(PhoneRealm.class);
        realm1.delete(PinRealm.class);
        realm1.delete(SearchResultRealm.class);
        realm1.delete(UserRealm.class);
        realm1.delete(MessageRealm.class);
      });
    } finally {
      realm.close();
      prepareRealm();
    }

    FacebookUtils.logout();

    SharedPreferences preferences = applicationComponent.sharedPreferences();
    preferences.edit().clear().commit();

    applicationComponent.accessToken().clear();
    applicationComponent.currentUser().clear();
    //applicationComponent.currentRoomMember().clear();
    applicationComponent.tagManager().clear();
    applicationComponent.stateManager().deleteKey(FACEBOOK_CONTACT_PERMISSION);

    FileUtils.deleteDir(FileUtils.getCacheDir(getApplicationContext()));

    Intent intent = new Intent(this, HomeActivity.class);
    intent.putExtra(IntentUtils.FINISH, true);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);

    Intent intentLauncher = new Intent(this, LauncherActivity.class);
    intentLauncher.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    intentLauncher.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    intentLauncher.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    int pendingIntentId = 123456; // FAKE ID
    PendingIntent mPendingIntent = PendingIntent.getActivity(this, pendingIntentId, intentLauncher,
        PendingIntent.FLAG_CANCEL_CURRENT);
    AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
    System.exit(0);
  }
}
