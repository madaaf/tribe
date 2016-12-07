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
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerApplicationComponent;
import com.tribe.app.presentation.internal.di.modules.ApplicationModule;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;

import java.util.Date;
import java.util.List;

import io.branch.referral.Branch;
import io.fabric.sdk.android.Fabric;
import io.realm.DynamicRealmObject;
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
        this.initializeBranch();
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
                .schemaVersion(2)
                .migration((realm, oldVersion, newVersion) -> {
                    RealmSchema schema = realm.getSchema();

                    if (oldVersion == 0) {
                        RealmObjectSchema groupMemberSchema = schema.create("GroupMemberRealm")
                                .addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                                .addField("created_at", Date.class)
                                .addField("updated_at", Date.class)
                                .addField("display_name", String.class)
                                .addField("username", String.class)
                                .addField("picture", String.class)
                                .addField("score", int.class)
                                .addField("invisible_mode", boolean.class);

                        RealmObjectSchema groupSchema = schema.get("GroupRealm");

                        if (groupSchema.hasField("privateGroup")) groupSchema.removeField("privateGroup");

                        groupSchema.addRealmListField("members_tmp", groupMemberSchema)
                                .transform(obj -> {
                                    List<UserRealm> members = obj.get("members");

                                    if (members != null) {
                                        for (UserRealm userRealm : members) {
                                            DynamicRealmObject groupMember = realm.createObject("GroupMemberRealm");
                                            groupMember.setString("id", userRealm.getId());
                                            groupMember.setDate("created_at", userRealm.getCreatedAt());
                                            groupMember.setDate("updated_at", userRealm.getUpdatedAt());
                                            groupMember.setString("display_name", userRealm.getDisplayName());
                                            groupMember.setString("username", userRealm.getUsername());
                                            groupMember.setString("picture", userRealm.getProfilePicture());
                                            groupMember.setInt("score", userRealm.getScore());
                                            groupMember.setBoolean("invisble_mode", userRealm.isInvisibleMode());
                                            obj.getList("members_tmp").add(groupMember);
                                        }
                                    }
                                })
                                .removeField("members")
                                .renameField("members_tmp", "members");

                        groupSchema.addRealmListField("admins_tmp", groupMemberSchema)
                                .transform(obj -> {
                                    List<UserRealm> admins = obj.get("admins");

                                    if (admins != null) {
                                        for (UserRealm userRealm : admins) {
                                            DynamicRealmObject groupMember = realm.createObject("GroupMemberRealm");
                                            groupMember.setString("id", userRealm.getId());
                                            groupMember.setDate("created_at", userRealm.getCreatedAt());
                                            groupMember.setDate("updated_at", userRealm.getUpdatedAt());
                                            groupMember.setString("display_name", userRealm.getDisplayName());
                                            groupMember.setString("username", userRealm.getUsername());
                                            groupMember.setString("picture", userRealm.getProfilePicture());
                                            groupMember.setInt("score", userRealm.getScore());
                                            groupMember.setBoolean("invisble_mode", userRealm.isInvisibleMode());
                                            obj.getList("admins_tmp").add(groupMember);
                                        }
                                    }
                                })
                                .removeField("admins")
                                .renameField("admins_tmp", "admins");

                        oldVersion++;
                    }

                    if (oldVersion == 1) {
                        RealmObjectSchema userSchema = schema.get("UserRealm");

                        userSchema.addField("push_notif", boolean.class);
                    }
                })
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    private void initializeFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }

    private void initializeBranch() {
        Branch.getAutoInstance(this);
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
