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
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsEventLogger;
import com.digits.sdk.android.events.DigitsEventDetails;
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
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupMemberRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.LocationRealm;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.PhoneRealm;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerApplicationComponent;
import com.tribe.app.presentation.internal.di.modules.ApplicationModule;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.IntentUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.LauncherActivity;
import com.tribe.tribelivesdk.facetracking.UlseeManager;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameChallenge;
import com.tribe.tribelivesdk.game.GameDraw;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.game.GamePostIt;
import com.tribe.tribelivesdk.view.opengl.filter.FaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.filter.FilterManager;
import com.tribe.tribelivesdk.view.opengl.filter.FilterMask;
import com.tribe.tribelivesdk.view.opengl.filter.ImageFilter;
import com.tribe.tribelivesdk.view.opengl.filter.LutColorFilter;
import com.tribe.tribelivesdk.view.opengl.filter.mask.BearFaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.filter.mask.CatFaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.filter.mask.FlowerCrownFaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.filter.mask.NeonSkullFaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.filter.mask.OrangeSunglassesFaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.filter.mask.RabbitFaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.filter.mask.RoundSunglassesFaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.filter.mask.TattooFaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.filter.mask.UnicornFaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.utils.ImgSdk;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import io.branch.referral.Branch;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import io.realm.exceptions.RealmMigrationNeededException;
import java.util.ArrayList;
import java.util.List;
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
    initInjector();
    // initLeakDetection();
    initFabric();
    initRealm();
    initStetho();
    initFacebook();
    initBranch();
    initTimber();
    initAppState();
    initTakt();
    initUlsee();
    initFilters();
    initGameManager();
    ImgSdk.init(this);
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

  private void initFabric() {
    TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);

    Digits digits = new Digits.Builder().withTheme(R.style.CustomDigitsTheme)
        .withDigitsEventLogger(new DigitsEventLogger() {
          @Override public void phoneNumberSubmit(DigitsEventDetails details) {
            super.phoneNumberSubmit(details);
            Timber.d("phone number submit");
            applicationComponent.tagManager()
                .trackEvent(TagManagerUtils.KPI_Onboarding_PinConfirmed);
          }

          @Override public void confirmationCodeSubmit(DigitsEventDetails details) {
            Timber.d("pin submitted");
            applicationComponent.tagManager()
                .trackEvent(TagManagerUtils.KPI_Onboarding_PinSubmitted);
          }
        })
        .build();

    if (BuildConfig.DEBUG) {
      Fabric.with(this, new TwitterCore(authConfig), digits);
    } else {
      Fabric.with(this, new TwitterCore(authConfig), digits, new Crashlytics(), new Answers());
    }
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
    RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().schemaVersion(8)
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
        })
        .build();
    Realm.setDefaultConfiguration(realmConfiguration);

    Realm realm = null;
    try {
      realm = Realm.getDefaultInstance();
    } catch (RealmMigrationNeededException e) {
      Realm.deleteRealm(realmConfiguration);
    } finally {
      if (realm != null) realm.close();
    }
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

    List<FilterMask> filterList = new ArrayList<>();
    filterList.add(
        new LutColorFilter(this, ImageFilter.IMAGE_FILTER_TAN, "Tan", R.drawable.picto_filter_tan,
            com.tribe.tribelivesdk.R.drawable.lut_settled));
    filterList.add(new LutColorFilter(this, ImageFilter.IMAGE_FILTER_HIPSTER, "Hipster",
        R.drawable.picto_filter_hipster, com.tribe.tribelivesdk.R.drawable.lut_pola669));
    filterList.add(
        new LutColorFilter(this, ImageFilter.IMAGE_FILTER_BW, "B&W", R.drawable.picto_filter_bw,
            com.tribe.tribelivesdk.R.drawable.lut_bw));
    filterList.add(new RabbitFaceMaskFilter(this, FaceMaskFilter.FACE_MASK_RABBIT, "Rabbit",
        R.drawable.picto_mask_rabbit));
    filterList.add(
        new OrangeSunglassesFaceMaskFilter(this, FaceMaskFilter.FACE_MASK_ORANGE_SUNGLASSES,
            "Orange Sunglasses", R.drawable.picto_mask_orange_glasses));
    filterList.add(new TattooFaceMaskFilter(this, FaceMaskFilter.FACE_MASK_TATTOO, "Tattoo",
        R.drawable.picto_mask_tattoo));
    filterList.add(
        new FlowerCrownFaceMaskFilter(this, FaceMaskFilter.FACE_MASK_FLOWER_CROWN, "Flower Crown",
            R.drawable.picto_mask_flower_crown));
    filterList.add(new BearFaceMaskFilter(this, FaceMaskFilter.FACE_MASK_BEAR, "Bear",
        R.drawable.picto_mask_bear));
    filterList.add(
        new RoundSunglassesFaceMaskFilter(this, FaceMaskFilter.FACE_MASK_SUNGLASSES, "Sunglasses",
            R.drawable.picto_mask_round_glasses));
    filterList.add(new UnicornFaceMaskFilter(this, FaceMaskFilter.FACE_MASK_UNICORN, "Unicorn",
        R.drawable.picto_mask_unicorn));
    filterList.add(new CatFaceMaskFilter(this, FaceMaskFilter.FACE_MASK_CAT, "Cat",
        R.drawable.picto_mask_cat));
    filterList.add(
        new NeonSkullFaceMaskFilter(this, FaceMaskFilter.FACE_MASK_NEON_SKULL, "Neon Skull",
            R.drawable.picto_mask_skull));

    filterManager.initFilters(filterList);
  }

  private void initGameManager() {
    GameManager gameManager = GameManager.getInstance(this);
    gameManager.addGame(new GamePostIt(this, Game.GAME_POST_IT, getString(R.string.game_post_it),
        R.drawable.picto_game_post_it));
    gameManager.addGame(
        new GameChallenge(this, Game.GAME_CHALLENGE, getString(R.string.game_challenges),
            R.drawable.icon_game_challenge));
    gameManager.addGame(new GameDraw(this, Game.GAME_DRAW, "Draw ! ", R.drawable.icon_game_draw));
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
        realm1.delete(FriendshipRealm.class);
        realm1.delete(GroupMemberRealm.class);
        realm1.delete(GroupRealm.class);
        realm1.delete(Installation.class);
        realm1.delete(LocationRealm.class);
        realm1.delete(MembershipRealm.class);
        realm1.delete(PhoneRealm.class);
        realm1.delete(PinRealm.class);
        realm1.delete(SearchResultRealm.class);
        realm1.delete(UserRealm.class);
      });
    } finally {
      realm.close();
      prepareRealm();
    }

    FacebookUtils.logout();
    Digits.logout();

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
