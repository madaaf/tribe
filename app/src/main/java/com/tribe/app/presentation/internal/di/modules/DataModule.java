package com.tribe.app.presentation.internal.di.modules;

import android.content.Context;
import android.content.SharedPreferences;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.utils.preferences.DebugMode;
import com.tribe.app.presentation.utils.preferences.InvisibleMode;
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.utils.preferences.LastVersionCode;
import com.tribe.app.presentation.utils.preferences.PreferencesConstants;
import com.tribe.app.presentation.utils.preferences.RoutingMode;
import com.tribe.app.presentation.utils.preferences.Theme;
import com.tribe.app.presentation.utils.preferences.TribeState;
import com.tribe.app.presentation.utils.preferences.UISounds;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import dagger.Module;
import dagger.Provides;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Singleton;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by tiago on 30/05/2016.
 */
@Module public class DataModule {

  @Provides @Singleton SharedPreferences provideSharedPreferences(Context context) {
    return context.getSharedPreferences("TRIBE", MODE_PRIVATE);
  }

  @Provides @Singleton RxSharedPreferences provideRxSharedPreferences(SharedPreferences prefs) {
    return RxSharedPreferences.create(prefs);
  }

  @Provides @Singleton @Theme Preference<Integer> provideTheme(RxSharedPreferences prefs) {
    return prefs.getInteger(PreferencesConstants.THEME, 0);
  }

  @Provides @Singleton @InvisibleMode Preference<Boolean> provideInvisibleMode(
      RxSharedPreferences prefs) {
    return prefs.getBoolean(PreferencesConstants.INVISIBLE_MODE, false);
  }

  @Provides @Singleton @AddressBook Preference<Boolean> provideAddressBook(
      RxSharedPreferences prefs) {
    return prefs.getBoolean(PreferencesConstants.ADDRESS_BOOK, false);
  }

  @Provides @Singleton @LastSync Preference<Long> provideLastSync(RxSharedPreferences prefs) {
    return prefs.getLong(PreferencesConstants.LAST_SYNC, 0L);
  }

  @Provides @Singleton @LastVersionCode Preference<Integer> provideLastVersionCode(Context context,
      RxSharedPreferences prefs) {
    return prefs.getInteger(PreferencesConstants.PREVIOUS_VERSION_CODE, -1);
  }

  @Provides @Singleton @DebugMode Preference<Boolean> provideDebugMode(RxSharedPreferences prefs) {
    return prefs.getBoolean(PreferencesConstants.DEBUG_MODE, false);
  }

  @Provides @Singleton @UISounds Preference<Boolean> provideUISounds(RxSharedPreferences prefs) {
    return prefs.getBoolean(PreferencesConstants.UI_SOUNDS, true);
  }

  @Provides @Singleton @TribeState Preference<Set<String>> provideTribeState(
      RxSharedPreferences prefs) {
    return prefs.getStringSet(PreferencesConstants.TRIBE_STATE, new HashSet<>());
  }

  @Provides @Singleton @RoutingMode Preference<String> provideRoutingMode(
      RxSharedPreferences prefs) {
    return prefs.getString(PreferencesConstants.ROUTING_MODE, TribeLiveOptions.ROUTED);
  }
}
