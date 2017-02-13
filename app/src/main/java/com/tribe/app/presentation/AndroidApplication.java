package com.tribe.app.presentation;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.stetho.Stetho;
import com.jenzz.appstate.AppState;
import com.jenzz.appstate.AppStateListener;
import com.jenzz.appstate.AppStateMonitor;
import com.jenzz.appstate.RxAppStateMonitor;
import com.tribe.app.BuildConfig;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerApplicationComponent;
import com.tribe.app.presentation.internal.di.modules.ApplicationModule;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import io.branch.referral.Branch;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import java.util.Date;
import rx.Subscription;
import timber.log.Timber;

/**
 * Android Main Application
 */
public class AndroidApplication extends Application {

  private ApplicationComponent applicationComponent;
  private AppStateMonitor appStateMonitor;
  private AppState appState;

  @Override public void onCreate() {
    super.onCreate();
    initInjector();
    initLeakDetection();
    initRealm();
    initStetho();
    initFacebook();
    initBranch();
    initTimber();
    initAppState();
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
    if (BuildConfig.DEBUG) {
      //            if (LeakCanary.isInAnalyzerProcess(this)) {
      //                // This process is dedicated to LeakCanary for heap analysis.
      //                // You should not init your app in this process.
      //                return;
      //            }
      //
      //            LeakCanary.install(this);
    }
  }

  private void initStetho() {
    if (BuildConfig.DEBUG) {
      Stetho.initializeWithDefaults(this);
    }
  }

  private void initRealm() {
    Realm.init(this);
    RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().schemaVersion(5)
        .deleteRealmIfMigrationNeeded()
        .build();
    Realm.setDefaultConfiguration(realmConfiguration);
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

  private class SampleAppStateListener implements AppStateListener {

    @Override
    public void onAppDidEnterForeground() {
      appState = AppState.FOREGROUND;
    }

    @Override
    public void onAppDidEnterBackground() {
      appState = AppState.BACKGROUND;
    }
  }

  public AppState getAppState() {
    return appState;
  }

  public void logoutUser() {
    Realm realm = applicationComponent.realm();
    try {
      realm.beginTransaction();
      realm.deleteAll();
      realm.commitTransaction();
    } catch (IllegalStateException ex) {
      if (realm.isInTransaction()) realm.cancelTransaction();
      ex.printStackTrace();
    } finally {
      realm.close();
    }

    FacebookUtils.logout();

    SharedPreferences preferences = applicationComponent.sharedPreferences();
    preferences.edit().clear().commit();

    applicationComponent.accessToken().clear();
    applicationComponent.currentUser().clear();
    applicationComponent.tagManager().clear();

    FileUtils.deleteDir(FileUtils.getCacheDir(getApplicationContext()));
  }
}
