package com.tribe.app.presentation.internal.di.modules;

import android.content.Context;
import android.content.SharedPreferences;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.utils.preferences.AudioDefault;
import com.tribe.app.presentation.utils.preferences.DebugMode;
import com.tribe.app.presentation.utils.preferences.DistanceUnits;
import com.tribe.app.presentation.utils.preferences.Filter;
import com.tribe.app.presentation.utils.preferences.HasRatedApp;
import com.tribe.app.presentation.utils.preferences.HasReceivedPointsForCameraPermission;
import com.tribe.app.presentation.utils.preferences.InvisibleMode;
import com.tribe.app.presentation.utils.preferences.LastNotifyRequest;
import com.tribe.app.presentation.utils.preferences.LastOnlineNotification;
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.utils.preferences.LastUserRequest;
import com.tribe.app.presentation.utils.preferences.LastVersionCode;
import com.tribe.app.presentation.utils.preferences.LocationContext;
import com.tribe.app.presentation.utils.preferences.Memories;
import com.tribe.app.presentation.utils.preferences.PreferencesConstants;
import com.tribe.app.presentation.utils.preferences.ShareProfile;
import com.tribe.app.presentation.utils.preferences.Theme;
import com.tribe.app.presentation.utils.preferences.TutorialState;
import com.tribe.app.presentation.utils.preferences.UISounds;
import com.tribe.app.presentation.utils.preferences.WasAskedForPermissions;
import com.tribe.app.presentation.utils.preferences.WeatherUnits;
import com.tribe.app.presentation.view.utils.Distance;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by tiago on 30/05/2016.
 */
@Module
public class DataModule {

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Context context) {
        return context.getSharedPreferences("TRIBE", MODE_PRIVATE);
    }

    @Provides
    @Singleton
    RxSharedPreferences provideRxSharedPreferences(SharedPreferences prefs) {
        return RxSharedPreferences.create(prefs);
    }

    @Provides
    @Singleton
    @DistanceUnits
    Preference<String> provideDistanceUnits(RxSharedPreferences prefs) {
        return prefs.getString(PreferencesConstants.DISTANCE_UNITS, Distance.METERS);
    }

    @Provides
    @Singleton
    @WeatherUnits
    Preference<String> provideWeatherUnits(RxSharedPreferences prefs) {
        return prefs.getString(PreferencesConstants.WEATHER_UNITS, com.tribe.app.presentation.view.utils.Weather.CELSIUS);
    }

    @Provides
    @Singleton
    @Memories
    Preference<Boolean> provideMemories(RxSharedPreferences prefs) {
        return prefs.getBoolean(PreferencesConstants.MEMORIES, false);
    }

    @Provides
    @Singleton
    @LocationContext
    Preference<Boolean> provideLocationContext(RxSharedPreferences prefs) {
        return prefs.getBoolean(PreferencesConstants.LOCATION_CONTEXT, true);
    }

    @Provides
    @Singleton
    @AudioDefault
    Preference<Boolean> provideAudioDefault(RxSharedPreferences prefs) {
        return prefs.getBoolean(PreferencesConstants.AUDIO_DEFAULT, false);
    }

    @Provides
    @Singleton
    @Theme
    Preference<Integer> provideTheme(RxSharedPreferences prefs) {
        return prefs.getInteger(PreferencesConstants.THEME, 0);
    }

    @Provides
    @Singleton
    @InvisibleMode
    Preference<Boolean> provideInvisibleMode(RxSharedPreferences prefs) {
        return prefs.getBoolean(PreferencesConstants.INVISIBLE_MODE, false);
    }

    @Provides
    @Singleton
    @LastUserRequest
    Preference<String> provideLastUserRequest(RxSharedPreferences prefs) {
        return prefs.getString(PreferencesConstants.LAST_USER_REQUEST, "");
    }

    @Provides
    @Singleton
    @LastNotifyRequest
    Preference<String> provideLastFBNotify(RxSharedPreferences prefs) {
        return prefs.getString(PreferencesConstants.LAST_NOTIFY_REQUEST, "");
    }

    @Provides
    @Singleton
    @Filter
    Preference<Integer> provideFilter(RxSharedPreferences prefs) {
        return prefs.getInteger(PreferencesConstants.FILTER, 0);
    }

    @Provides
    @Singleton
    @ShareProfile
    Preference<Boolean> provideShareProfile(RxSharedPreferences prefs) {
        return prefs.getBoolean(PreferencesConstants.SHARE_PROFILE, false);
    }

    @Provides
    @Singleton
    @AddressBook
    Preference<Boolean> provideAddressBook(RxSharedPreferences prefs) {
        return prefs.getBoolean(PreferencesConstants.ADDRESS_BOOK, false);
    }

    @Provides
    @Singleton
    @HasReceivedPointsForCameraPermission
    Preference<Boolean> provideHasReceivedPointsForCameraPermission(RxSharedPreferences prefs) {
        return prefs.getBoolean(PreferencesConstants.HAS_RECEIVED_POINTS_FOR_CAMERA_PERMISSION, false);
    }

    @Provides
    @Singleton
    @LastSync
    Preference<Long> provideLastSync(RxSharedPreferences prefs) {
        return prefs.getLong(PreferencesConstants.LAST_SYNC, 0L);
    }

    @Provides
    @Singleton
    @WasAskedForPermissions
    Preference<Boolean> provideWasAskedForCameraPermission(RxSharedPreferences prefs) {
        return prefs.getBoolean(PreferencesConstants.WAS_ASKED_FOR_CAMERA_PERMISSION, false);
    }

    @Provides
    @Singleton
    @LastVersionCode
    Preference<Integer> provideLastVersionCode(Context context, RxSharedPreferences prefs) {
        return prefs.getInteger(PreferencesConstants.PREVIOUS_VERSION_CODE, -1);
    }

    @Provides
    @Singleton
    @HasRatedApp
    Preference<Boolean> provideHasRatedApp(RxSharedPreferences prefs) {
        return prefs.getBoolean(PreferencesConstants.HAS_RATED_APP, false);
    }

    @Provides
    @Singleton
    @TutorialState
    Preference<Set<String>> provideTutorialState(RxSharedPreferences prefs) {
        return prefs.getStringSet(PreferencesConstants.TUTORIAL_STATE, new HashSet<>());
    }

    @Provides
    @Singleton
    @DebugMode
    Preference<Boolean> provideDebugMode(RxSharedPreferences prefs) {
        return prefs.getBoolean(PreferencesConstants.DEBUG_MODE, false);
    }

    @Provides
    @Singleton
    @LastOnlineNotification
    Preference<Long> provideLastOnlineNotification(RxSharedPreferences prefs) {
        return prefs.getLong(PreferencesConstants.LAST_ONLINE_NOTIFICATION, 0L);
    }

    @Provides
    @Singleton
    @UISounds
    Preference<Boolean> provideUISounds(RxSharedPreferences prefs) {
        return prefs.getBoolean(PreferencesConstants.UI_SOUNDS, true);
    }
}
