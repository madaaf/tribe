package com.tribe.app.presentation.internal.di.modules;

import android.content.Context;
import android.content.SharedPreferences;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.tribe.app.presentation.internal.di.scope.AudioDefault;
import com.tribe.app.presentation.internal.di.scope.DistanceUnits;
import com.tribe.app.presentation.internal.di.scope.Filter;
import com.tribe.app.presentation.internal.di.scope.InvisibleMode;
import com.tribe.app.presentation.internal.di.scope.LastMessageRequest;
import com.tribe.app.presentation.internal.di.scope.LastNotifyRequest;
import com.tribe.app.presentation.internal.di.scope.LastUserRequest;
import com.tribe.app.presentation.internal.di.scope.LocationContext;
import com.tribe.app.presentation.internal.di.scope.Memories;
import com.tribe.app.presentation.internal.di.scope.Preload;
import com.tribe.app.presentation.internal.di.scope.SpeedPlayback;
import com.tribe.app.presentation.internal.di.scope.Theme;
import com.tribe.app.presentation.internal.di.scope.WeatherUnits;
import com.tribe.app.presentation.utils.PreferencesConstants;
import com.tribe.app.presentation.view.component.TribePagerView;
import com.tribe.app.presentation.view.utils.Distance;

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
    @SpeedPlayback
    Preference<Float> provideSpeedRate(RxSharedPreferences prefs) {
        return prefs.getFloat(PreferencesConstants.SPEED_PLAYBACK, TribePagerView.SPEED_NORMAL);
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
        return prefs.getBoolean(PreferencesConstants.LOCATION_CONTEXT, false);
    }

    @Provides
    @Singleton
    @AudioDefault
    Preference<Boolean> provideAudioDefault(RxSharedPreferences prefs) {
        return prefs.getBoolean(PreferencesConstants.AUDIO_DEFAULT, false);
    }

    @Provides
    @Singleton
    @Preload
    Preference<Boolean> providePreload(RxSharedPreferences prefs) {
        return prefs.getBoolean(PreferencesConstants.PRELOAD, false);
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
    @LastMessageRequest
    Preference<String> provideLastMessageRequest(RxSharedPreferences prefs) {
        return prefs.getString(PreferencesConstants.LAST_MESSAGE_REQUEST, "");
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
}
