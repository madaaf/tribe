package com.tribe.app.presentation.internal.di.modules;

import android.content.Context;
import android.content.SharedPreferences;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.tribe.app.presentation.internal.di.scope.DistanceUnits;
import com.tribe.app.presentation.internal.di.scope.PerApplication;
import com.tribe.app.presentation.internal.di.scope.SpeedPlayback;
import com.tribe.app.presentation.internal.di.scope.WeatherUnits;
import com.tribe.app.presentation.utils.PreferencesConstants;
import com.tribe.app.presentation.view.component.TribeComponentView;
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
    @PerApplication
    SharedPreferences provideSharedPreferences(Context context) {
        return context.getSharedPreferences("TRIBE", MODE_PRIVATE);
    }

    @Provides
    @PerApplication
    RxSharedPreferences provideRxSharedPreferences(SharedPreferences prefs) {
        return RxSharedPreferences.create(prefs);
    }

    @Provides @Singleton @SpeedPlayback
    Preference<Float> provideSpeedRate(RxSharedPreferences prefs) {
        return prefs.getFloat(PreferencesConstants.SPEED_PLAYBACK, TribeComponentView.SPEED_NORMAL);
    }

    @Provides @Singleton @DistanceUnits
    Preference<String> provideDistanceUnits(RxSharedPreferences prefs) {
        return prefs.getString(PreferencesConstants.DISTANCE_UNITS, Distance.METERS);
    }

    @Provides @Singleton @WeatherUnits
    Preference<String> provideWeatherUnits(RxSharedPreferences prefs) {
        return prefs.getString(PreferencesConstants.WEATHER_UNITS, com.tribe.app.presentation.view.utils.Weather.CELSIUS);
    }
}
