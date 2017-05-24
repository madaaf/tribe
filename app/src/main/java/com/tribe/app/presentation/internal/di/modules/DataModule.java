package com.tribe.app.presentation.internal.di.modules;

import android.content.Context;
import android.content.SharedPreferences;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.utils.preferences.CallTagsMap;
import com.tribe.app.presentation.utils.preferences.CounterOfCallsForGrpButton;
import com.tribe.app.presentation.utils.preferences.DebugMode;
import com.tribe.app.presentation.utils.preferences.FullscreenNotificationState;
import com.tribe.app.presentation.utils.preferences.FullscreenNotifications;
import com.tribe.app.presentation.utils.preferences.ImmersiveCallState;
import com.tribe.app.presentation.utils.preferences.InvisibleMode;
import com.tribe.app.presentation.utils.preferences.IsGroupCreated;
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.utils.preferences.LastVersionCode;
import com.tribe.app.presentation.utils.preferences.LookupResult;
import com.tribe.app.presentation.utils.preferences.MinutesOfCalls;
import com.tribe.app.presentation.utils.preferences.MissedPlayloadNotification;
import com.tribe.app.presentation.utils.preferences.NewContactsTooltip;
import com.tribe.app.presentation.utils.preferences.NumberOfCalls;
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.utils.preferences.RoutingMode;
import com.tribe.app.presentation.utils.preferences.Theme;
import com.tribe.app.presentation.utils.preferences.TribeState;
import com.tribe.app.presentation.utils.preferences.UISounds;
import com.tribe.app.presentation.utils.preferences.UserPhoneNumber;
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
    return prefs.getInteger(PreferencesUtils.THEME, 0);
  }

  @Provides @Singleton @InvisibleMode Preference<Boolean> provideInvisibleMode(
      RxSharedPreferences prefs) {
    return prefs.getBoolean(PreferencesUtils.INVISIBLE_MODE, false);
  }

  @Provides @Singleton @AddressBook Preference<Boolean> provideAddressBook(
      RxSharedPreferences prefs) {
    return prefs.getBoolean(PreferencesUtils.ADDRESS_BOOK, false);
  }

  @Provides @Singleton @LastSync Preference<Long> provideLastSync(RxSharedPreferences prefs) {
    return prefs.getLong(PreferencesUtils.LAST_SYNC, 0L);
  }

  @Provides @Singleton @NewContactsTooltip Preference<Boolean> provideNewContactsTooltip(
      RxSharedPreferences prefs) {
    return prefs.getBoolean(PreferencesUtils.NEW_CONTACT_TOOLTIP, false);
  }

  @Provides @Singleton @LastVersionCode Preference<Integer> provideLastVersionCode(Context context,
      RxSharedPreferences prefs) {
    return prefs.getInteger(PreferencesUtils.PREVIOUS_VERSION_CODE, -1);
  }

  @Provides @Singleton @NumberOfCalls Preference<Integer> provideNumberOfCalls(Context context,
      RxSharedPreferences prefs) {
    return prefs.getInteger(PreferencesUtils.NUMBER_OF_CALLS, 0);
  }

  @Provides @Singleton Preference<Integer> provideNumberOfMissedCalls(Context context,
      RxSharedPreferences prefs) {
    return prefs.getInteger(PreferencesUtils.NUMBER_OF_MISSED_CALLS, 0);
  }

  @Provides @Singleton @CounterOfCallsForGrpButton
  Preference<Integer> provideCounterOfCallsForGrpButton(Context context,
      RxSharedPreferences prefs) {
    return prefs.getInteger(PreferencesUtils.COUNTER_CALL_GRP_BTN, 0);
  }

  @Provides @Singleton @MinutesOfCalls Preference<Float> provideMinutesOfCalls(Context context,
      RxSharedPreferences prefs) {
    return prefs.getFloat(PreferencesUtils.MINUTES_OF_CALLS, 0f);
  }

  @Provides @Singleton @IsGroupCreated Preference<Boolean> provideIsGroupCreated(Context context,
      RxSharedPreferences prefs) {
    return prefs.getBoolean(PreferencesUtils.IS_GROUPE_CREATED, false);
  }

  @Provides @Singleton @ImmersiveCallState Preference<Boolean> provideImmersiveCallState(
      Context context, RxSharedPreferences prefs) {
    return prefs.getBoolean(PreferencesUtils.IS_GROUPE_CREATED, false);
  }

  @Provides @Singleton @DebugMode Preference<Boolean> provideDebugMode(RxSharedPreferences prefs) {
    return prefs.getBoolean(PreferencesUtils.DEBUG_MODE, false);
  }

  @Provides @Singleton @UISounds Preference<Boolean> provideUISounds(RxSharedPreferences prefs) {
    return prefs.getBoolean(PreferencesUtils.UI_SOUNDS, true);
  }

  @Provides @Singleton @TribeState Preference<Set<String>> provideTribeState(
      RxSharedPreferences prefs) {
    return prefs.getStringSet(PreferencesUtils.TRIBE_STATE, new HashSet<>());
  }

  @Provides @Singleton @RoutingMode Preference<String> provideRoutingMode(
      RxSharedPreferences prefs) {
    return prefs.getString(PreferencesUtils.ROUTING_MODE, TribeLiveOptions.ROUTED);
  }

  @Provides @Singleton @FullscreenNotifications Preference<Boolean> provideFullScreenNotifications(
      RxSharedPreferences prefs) {
    return prefs.getBoolean(PreferencesUtils.FULLSCREEN_NOTIFICATIONS, true);
  }

  @Provides @Singleton @FullscreenNotificationState
  Preference<Set<String>> provideFullscreenNotificationState(RxSharedPreferences prefs) {
    return prefs.getStringSet(PreferencesUtils.FULLSCREEN_NOTIFICATION_STATE, new HashSet<>());
  }

  @Provides @Singleton @MissedPlayloadNotification
  Preference<String> providedMissedPlayloadNotification(Context context,
      RxSharedPreferences prefs) {
    return prefs.getString(PreferencesUtils.MISSED_PLAYLOAD_NOTIF, "");
  }

  @Provides @Singleton @CallTagsMap Preference<String> provideCallTagsMap(
      RxSharedPreferences prefs) {
    return prefs.getString(PreferencesUtils.CALL_TAGS_MAP, "");
  }

  @Provides @Singleton @LookupResult Preference<String> provideLookupResult(
      RxSharedPreferences prefs) {
    return prefs.getString(PreferencesUtils.LOOKUP_RESULT, "");
  }

  @Provides @Singleton @UserPhoneNumber Preference<String> provideUserPhoneNumber(
      RxSharedPreferences prefs) {
    return prefs.getString(PreferencesUtils.USER_PHONE_NUMBER, null);
  }
}
