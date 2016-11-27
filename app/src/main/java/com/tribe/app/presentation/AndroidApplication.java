package com.tribe.app.presentation;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.stetho.Stetho;
import com.tribe.app.BuildConfig;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerApplicationComponent;
import com.tribe.app.presentation.internal.di.modules.ApplicationModule;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;

import io.fabric.sdk.android.Fabric;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Android Main Application
 */
public class AndroidApplication extends Application {

    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        this.initializeInjector();
        this.initializeLeakDetection();
        this.initializeRealm();
        this.initializeStetho();
        this.initializeFacebook();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void initializeInjector() {
        this.applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return this.applicationComponent;
    }

    private void initializeLeakDetection() {
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

    private void initializeStetho() {
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
    }

    private void initializeRealm() {
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration
                .Builder()
                .schemaVersion(1)
                .migration((realm, oldVersion, newVersion) -> {
                    RealmSchema schema = realm.getSchema();

                    if (oldVersion == 0) {
                        RealmObjectSchema groupSchema = schema.get("GroupRealm");

                        RealmObjectSchema groupMemberSchema = schema.create("GroupMemberRealm")
                                .addField("id", String.class, FieldAttribute.REQUIRED);

                        if (!groupSchema.hasField("memberIdList")) {
                            groupSchema.addRealmListField("memberIdList", groupMemberSchema);
                        }

                        if (!groupSchema.hasField("adminIdList")) {
                            groupSchema.addRealmListField("adminIdList", groupMemberSchema);
                        }

                        oldVersion++;
                    }
                })
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    private void initializeFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
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
