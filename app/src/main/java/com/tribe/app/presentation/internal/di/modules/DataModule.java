package com.tribe.app.presentation.internal.di.modules;

import android.content.Context;
import android.content.SharedPreferences;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.utils.preferences.CallTagsMap;
import com.tribe.app.presentation.utils.preferences.ChallengeNotifications;
import com.tribe.app.presentation.utils.preferences.ChatShortcutData;
import com.tribe.app.presentation.utils.preferences.CounterOfCallsForGrpButton;
import com.tribe.app.presentation.utils.preferences.DaysOfUsage;
import com.tribe.app.presentation.utils.preferences.DebugMode;
import com.tribe.app.presentation.utils.preferences.FullscreenNotificationState;
import com.tribe.app.presentation.utils.preferences.FullscreenNotifications;
import com.tribe.app.presentation.utils.preferences.GameData;
import com.tribe.app.presentation.utils.preferences.GamesPlayed;
import com.tribe.app.presentation.utils.preferences.HasSoftKeys;
import com.tribe.app.presentation.utils.preferences.ImmersiveCallState;
import com.tribe.app.presentation.utils.preferences.InvisibleMode;
import com.tribe.app.presentation.utils.preferences.IsGroupCreated;
import com.tribe.app.presentation.utils.preferences.LastImOnline;
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.utils.preferences.LastSyncGameData;
import com.tribe.app.presentation.utils.preferences.LastVersionCode;
import com.tribe.app.presentation.utils.preferences.LookupResult;
import com.tribe.app.presentation.utils.preferences.MinutesOfCalls;
import com.tribe.app.presentation.utils.preferences.MissedPlayloadNotification;
import com.tribe.app.presentation.utils.preferences.MultiplayerSessions;
import com.tribe.app.presentation.utils.preferences.NewContactsTooltip;
import com.tribe.app.presentation.utils.preferences.NewWS;
import com.tribe.app.presentation.utils.preferences.NumberOfCalls;
import com.tribe.app.presentation.utils.preferences.PokeUserGame;
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.utils.preferences.PreviousDateUsage;
import com.tribe.app.presentation.utils.preferences.RoutingMode;
import com.tribe.app.presentation.utils.preferences.SelectedTrophy;
import com.tribe.app.presentation.utils.preferences.SupportIsUsed;
import com.tribe.app.presentation.utils.preferences.SupportRequestId;
import com.tribe.app.presentation.utils.preferences.SupportUserId;
import com.tribe.app.presentation.utils.preferences.Theme;
import com.tribe.app.presentation.utils.preferences.TribeState;
import com.tribe.app.presentation.utils.preferences.UISounds;
import com.tribe.app.presentation.utils.preferences.UserPhoneNumber;
import com.tribe.app.presentation.utils.preferences.Walkthrough;
import com.tribe.app.presentation.utils.preferences.WebSocketUrlOverride;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import dagger.Module;
import dagger.Provides;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Named;
import javax.inject.Singleton;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by tiago on 30/05/2016.
 */
@Module public class DataModule {

  @Provides @Singleton SharedPreferences provideTempSharedPreferences(Context context) {
    return context.getSharedPreferences("TRIBE", MODE_PRIVATE);
  }

  @Provides @Singleton @Named("persistentPreferences")
  SharedPreferences providePersistentSharedPreferences(Context context) {
    return context.getSharedPreferences("PERSISTENT_TRIBE", MODE_PRIVATE);
  }

  @Provides @Singleton RxSharedPreferences provideTempRxSharedPreferences(SharedPreferences prefs) {
    return RxSharedPreferences.create(prefs);
  }

  @Provides @Singleton @Named("persistentRxPreferences")
  RxSharedPreferences providePersistentRxSharedPreferences(
      @Named("persistentPreferences") SharedPreferences prefs) {
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

  @Provides @Singleton @LastSyncGameData Preference<Long> provideLastSyncGameData(
      RxSharedPreferences prefs) {
    return prefs.getLong(PreferencesUtils.LAST_SYNC_GAME_DATA, 0L);
  }

  @Provides @Singleton @LastImOnline Preference<Long> provideLastImOnline(
      RxSharedPreferences prefs) {
    return prefs.getLong(PreferencesUtils.LAST_IM_ONLINE, 0L);
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

  @Provides @Singleton @SupportRequestId Preference<String> provideSupportRequestId(Context context,
      RxSharedPreferences prefs) {
    return prefs.getString(PreferencesUtils.SUPPORT_REQUEST_ID, "");
  }

  @Provides @Singleton @SupportUserId Preference<String> provideSupportUserId(Context context,
      RxSharedPreferences prefs) {
    return prefs.getString(PreferencesUtils.SUPPORT_USER_ID, "");
  }

  @Provides @Singleton @SupportIsUsed Preference<Set<String>> provideSupportIsUsed(Context context,
      RxSharedPreferences prefs) {
    return prefs.getStringSet(PreferencesUtils.SUPPORT_IS_USED, new HashSet<>());
  }

  @Provides @Singleton @PokeUserGame Preference<String> providePokeUserGame(Context context,
      RxSharedPreferences prefs) {
    return prefs.getString(PreferencesUtils.POKE_USER_GAME, "");
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

  @Provides @Singleton @HasSoftKeys Preference<Boolean> provideHasSoftKeys(
      RxSharedPreferences prefs) {
    return prefs.getBoolean(PreferencesUtils.HAS_SOFT_KEYS, false);
  }

  // We add the previous preferences, in case the user has already seen the
  // walkthrough with the previous preferences
  @Provides @Singleton @Walkthrough Preference<Boolean> provideWalkthrough(
      @Named("persistentRxPreferences") RxSharedPreferences prefs, RxSharedPreferences oldPrefs) {
    if (oldPrefs.getBoolean(PreferencesUtils.WALKTHROUGH).get()) {
      prefs.getBoolean(PreferencesUtils.WALKTHROUGH, false).set(true);
    }

    return prefs.getBoolean(PreferencesUtils.WALKTHROUGH, false);
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

  @Provides @Singleton @WebSocketUrlOverride Preference<String> provideWebsocketUrl(
      RxSharedPreferences prefs) {
    return prefs.getString(PreferencesUtils.WEBSOCKET_URL, "");
  }

  @Provides @Singleton @NewWS Preference<Boolean> provideNewWS(RxSharedPreferences prefs) {
    return prefs.getBoolean(PreferencesUtils.NEW_WS, true);
  }

  @Provides @Singleton @FullscreenNotifications Preference<Boolean> provideFullScreenNotifications(
      RxSharedPreferences prefs) {
    return prefs.getBoolean(PreferencesUtils.FULLSCREEN_NOTIFICATIONS, true);
  }

  @Provides @Singleton @FullscreenNotificationState
  Preference<Set<String>> provideFullscreenNotificationState(RxSharedPreferences prefs) {
    return prefs.getStringSet(PreferencesUtils.FULLSCREEN_NOTIFICATION_STATE, new HashSet<>());
  }

  @Provides @Singleton @ChallengeNotifications Preference<String> providedChallengeNotifications(
      Context context, RxSharedPreferences prefs) {
    return prefs.getString(PreferencesUtils.CHALLENGE_NOTIF, null);
  }

  @Provides @Singleton @ChatShortcutData Preference<String> provideChatShortcutData(
      RxSharedPreferences prefs) {
    return prefs.getString(PreferencesUtils.CHAT_SHORTCUT_DATA, "");
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

  @Provides @Singleton @GameData Preference<String> provideGameData(RxSharedPreferences prefs) {
    return prefs.getString(PreferencesUtils.GAME_DATA, "");
  }

  @Provides @Singleton @GamesPlayed Preference<Set<String>> provideGamesPlayed(
      RxSharedPreferences prefs) {
    return prefs.getStringSet(PreferencesUtils.GAMES_PLAYED, new HashSet<>());
  }

  @Provides @Singleton @MultiplayerSessions Preference<Integer> provideMultiplayerSessions(
      RxSharedPreferences prefs) {
    return prefs.getInteger(PreferencesUtils.MULTIPLAYER_SESSIONS);
  }

  @Provides @Singleton @DaysOfUsage Preference<Integer> provideDaysOfUsage(
      RxSharedPreferences prefs) {
    return prefs.getInteger(PreferencesUtils.DAYS_OF_USAGE);
  }

  @Provides @Singleton @SelectedTrophy Preference<String> provideSelectedTrophy(
      RxSharedPreferences prefs) {
    return prefs.getString(PreferencesUtils.SELECTED_TROPHY, UserRealm.NOOB);
  }

  @Provides @Singleton @PreviousDateUsage Preference<Long> providePreviousDateUsage(
      RxSharedPreferences prefs) {
    return prefs.getLong(PreferencesUtils.PREVIOUS_DATE_USAGE, 0L);
  }
}
