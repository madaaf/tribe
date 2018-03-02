package com.tribe.app.presentation.internal.di.modules;

import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.ChatCacheImpl;
import com.tribe.app.data.cache.ContactCache;
import com.tribe.app.data.cache.ContactCacheImpl;
import com.tribe.app.data.cache.GameCache;
import com.tribe.app.data.cache.GameCacheImpl;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.data.cache.LiveCacheImpl;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.cache.UserCacheImpl;
import com.tribe.app.data.executor.JobExecutor;
import com.tribe.app.data.network.job.BaseJob;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.BadgeRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.mapper.ScoreRealmDataMapper;
import com.tribe.app.data.realm.mapper.ShortcutRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.chat.CloudChatDataRepository;
import com.tribe.app.data.repository.chat.DiskChatDataRepository;
import com.tribe.app.data.repository.game.CloudGameDataRepository;
import com.tribe.app.data.repository.game.DiskGameDataRepository;
import com.tribe.app.data.repository.live.CloudLiveDataRepository;
import com.tribe.app.data.repository.live.DiskLiveDataRepository;
import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.data.repository.user.contact.RxContacts;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.chat.ChatRepository;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.game.GameRepository;
import com.tribe.app.domain.interactor.live.LiveRepository;
import com.tribe.app.domain.interactor.user.GetCloudUserInfos;
import com.tribe.app.domain.interactor.user.SynchroContactList;
import com.tribe.app.domain.interactor.user.UserRepository;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.UIThread;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.RXZendesk.RXZendesk;
import com.tribe.app.presentation.utils.unzip.RxUnzip;
import com.tribe.app.presentation.utils.analytics.AnalyticsManager;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.utils.preferences.MissedPlayloadNotification;
import com.tribe.app.presentation.utils.preferences.SupportRequestId;
import com.tribe.app.presentation.utils.preferences.SupportUserId;
import com.tribe.app.presentation.utils.preferences.Theme;
import com.tribe.app.presentation.utils.preferences.TribeState;
import com.tribe.app.presentation.utils.preferences.UISounds;
import com.tribe.app.presentation.view.activity.SmsListener;
import com.tribe.app.presentation.view.notification.NotificationBuilder;
import com.tribe.app.presentation.view.utils.ImageUtils;
import com.tribe.app.presentation.view.utils.MissedCallManager;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.tribelivesdk.stream.TribeAudioManager;
import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmResults;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import javax.inject.Named;
import javax.inject.Singleton;
import me.leolin.shortcutbadger.ShortcutBadger;
import timber.log.Timber;

/**
 * Dagger module that provides objects which will live during the application lifecycle.
 */
@Module public class ApplicationModule {

  private final AndroidApplication application;
  private RealmResults<UserRealm> userRealm;
  private RealmResults<BadgeRealm> badgeRealmResults;
  private RealmResults<AccessToken> accessTokenResults;

  public ApplicationModule(AndroidApplication application) {
    this.application = application;
  }

  @Provides @Singleton Context provideApplicationContext() {
    return this.application;
  }

  @Provides @Singleton ThreadExecutor provideThreadExecutor(JobExecutor jobExecutor) {
    return jobExecutor;
  }

  @Provides @Singleton PostExecutionThread providePostExecutionThread(UIThread uiThread) {
    return uiThread;
  }

  @Provides @Singleton UserRepository provideCloudUserRepository(
      CloudUserDataRepository userDataRepository) {
    return userDataRepository;
  }

  @Provides @Singleton UserRepository provideDiskUserRepository(
      DiskUserDataRepository userDataRepository) {
    return userDataRepository;
  }

  @Provides @Singleton ChatRepository provideCloudChatRepository(
      CloudChatDataRepository chatDataRepository) {
    return chatDataRepository;
  }

  @Provides @Singleton ChatRepository provideDiskChatRepository(
      DiskChatDataRepository chatDataRepository) {
    return chatDataRepository;
  }

  @Provides @Singleton UserCache provideUserCache(UserCacheImpl userCache) {
    return userCache;
  }

  @Provides @Singleton ChatCache provideChatCache(ChatCacheImpl chatCache) {
    return chatCache;
  }

  @Provides @Singleton LiveCache provideLiveCache(LiveCacheImpl liveCache) {
    return liveCache;
  }

  @Provides @Singleton ContactCache provideContactCache(ContactCacheImpl contactCache) {
    return contactCache;
  }

  @Provides @Singleton GameCache provideGameCache(GameCacheImpl gameCache) {
    return gameCache;
  }

  @Provides @Singleton RxContacts provideRxContacts(Context context,
      @Named("userThreadSafe") User user, PhoneUtils phoneUtils,
      @AddressBook Preference<Boolean> addressBook) {
    return new RxContacts(context, user, phoneUtils, addressBook);
  }

  @Provides @Singleton RxUnzip provideRxUnzip(Context context) {
    return new RxUnzip(context);
  }

  @Provides @Singleton RXZendesk provideRxZendesk(User user, DateUtils dateUtils,
      @SupportUserId Preference<String> supportUserIdPref,
      @SupportRequestId Preference<String> supportIdPref) {
    return new RXZendesk(user, dateUtils, supportUserIdPref, supportIdPref);
  }

  @Provides @Singleton Navigator provideNavigator(Context context) {
    return new Navigator(context);
  }

  @Provides @Singleton AccessToken provideAccessToken(Realm realm) {
    final AccessToken accessToken = new AccessToken();

    accessTokenResults = realm.where(AccessToken.class).findAll();

    accessTokenResults.addChangeListener(element -> {
      AccessToken accessTokenRes = realm.where(AccessToken.class).findFirst();

      if (accessTokenRes != null) {
        accessToken.copy(accessTokenRes);
        Timber.d("refresh_token : " + accessToken.getRefreshToken());
      }
    });

    if (accessTokenResults != null && accessTokenResults.size() > 0) {
      accessToken.copy(accessTokenResults.get(0));
      Timber.d("refresh_token : " + accessToken.getRefreshToken());
    }

    return accessToken;
  }

  @Provides @Singleton Installation provideInstallation() {
    Installation installation = new Installation();
    Realm realmInst = Realm.getDefaultInstance();

    final RealmResults<Installation> results = realmInst.where(Installation.class).findAll();
    if (results != null && results.size() > 0) {
      installation = realmInst.copyFromRealm(results.get(0));
    }

    realmInst.close();

    return installation;
  }

  @Provides @Singleton User provideCurrentUser(Realm realm, AccessToken accessToken,
      UserRealmDataMapper userRealmDataMapper, ShortcutRealmDataMapper shortcutRealmDataMapper,
      ScoreRealmDataMapper scoreRealmDataMapper) {
    final User user = new User("");

    userRealm = realm.where(UserRealm.class).equalTo("id", accessToken.getUserId()).findAll();
    userRealm.addChangeListener(element -> {
      UserRealm userRealmRes =
          realm.where(UserRealm.class).equalTo("id", accessToken.getUserId()).findFirst();

      if (userRealmRes != null) {
        user.copy(userRealmDataMapper.transform(realm.copyFromRealm(userRealmRes), true));
        attachDataToUser(user, realm, shortcutRealmDataMapper);
      }
    });

    if (userRealm != null && userRealm.size() > 0) {
      user.copy(userRealmDataMapper.transform(realm.copyFromRealm(userRealm.get(0)), true));
    }

    attachDataToUser(user, realm, shortcutRealmDataMapper);

    return user;
  }

  private void attachDataToUser(User user, Realm realm,
      ShortcutRealmDataMapper shortcutRealmDataMapper) {
    RealmResults<ShortcutRealm> shortcutRealmResults = realm.where(ShortcutRealm.class).findAll();

    if (user != null) {
      user.setShortcutList(shortcutRealmDataMapper.transform(shortcutRealmResults));
    }
  }

  @Provides @Singleton BadgeRealm provideBadge(Realm realm) {
    BadgeRealm newBadge = new BadgeRealm();
    badgeRealmResults = realm.where(BadgeRealm.class).findAll();
    badgeRealmResults.addChangeListener(
        element -> updateBadge(realm.where(BadgeRealm.class).findFirst()));

    if (badgeRealmResults != null && badgeRealmResults.size() > 0) {
      updateBadge(badgeRealmResults.first());
    }

    return newBadge;
  }

  private void updateBadge(BadgeRealm badge) {
    if (badge != null) {
      if (badge.getValue() == 0) {
        ShortcutBadger.removeCount(application);
      } else {
        ShortcutBadger.applyCount(application, badge.getValue());
      }
    }
  }

  @Provides @Singleton @Named("userThreadSafe") User provideCurrentUserThreadSafe(
      AccessToken accessToken, UserRealmDataMapper userRealmDataMapper) {
    final User user = new User("");
    Realm realmInst = Realm.getDefaultInstance();

    UserRealm userDB =
        realmInst.where(UserRealm.class).equalTo("id", accessToken.getUserId()).findFirst();
    if (userDB != null) {
      user.copy(userRealmDataMapper.transform(realmInst.copyFromRealm(userDB), true));
    }

    realmInst.close();

    return user;
  }

  @Provides @Singleton UserRealm provideCurrentUserRealm(AccessToken accessToken) {
    UserRealm user = new UserRealm();
    Realm realmInst = Realm.getDefaultInstance();

    final UserRealm userRealm =
        realmInst.where(UserRealm.class).equalTo("id", accessToken.getUserId()).findFirst();
    if (userRealm != null) {
      user = realmInst.copyFromRealm(userRealm);
    }

    realmInst.close();

    return user;
  }

  @Provides @Singleton Realm provideRealm() {
    Timber.d("Accessing Realm applicationModule");
    Realm realm = Realm.getDefaultInstance();
    return realm;
  }

  @Provides @Singleton PaletteGrid providePaletteGrid(Context context,
      @Theme Preference<Integer> theme) {
    return new PaletteGrid(context, theme);
  }

  @Provides @Singleton ImageUtils provideImageUtils(Context context, ScreenUtils screenUtils) {
    return new ImageUtils(context, screenUtils);
  }

  @Provides @Singleton TagManager provideTagManager(AnalyticsManager analyticsManager) {
    return analyticsManager;
  }

  @Provides @Singleton FileUtils provideFileUtils() {
    return new FileUtils();
  }

  @Provides @Singleton PhoneUtils providePhoneUtils(Context context) {
    return new PhoneUtils(context);
  }

  @Provides @Singleton ScreenUtils provideScreenUtils(Context context) {
    return new ScreenUtils(context);
  }

  @Provides @Singleton IntentFilter provideIntentFilter() {
    return new IntentFilter();
  }

  @Provides @Singleton SmsListener provideSmsListener(Context context, IntentFilter filter) {
    return new SmsListener(context, filter);
  }

  @Provides @Singleton SoundManager provideSoundManager(Context context,
      @UISounds Preference<Boolean> uiSounds) {
    return new SoundManager(context, uiSounds);
  }

  @Provides @Singleton StateManager provideStateManager(
      @TribeState Preference<Set<String>> tribeState) {
    return new StateManager(tribeState);
  }

  @Provides @Singleton MissedCallManager provideMissedCallManager(Context context,
      @MissedPlayloadNotification Preference<String> missedPlayloadNotification) {
    return new MissedCallManager(context, missedPlayloadNotification);
  }

  @Provides @Named("cloudUserInfos") UseCase provideCloudGetUserInfos(
      GetCloudUserInfos getCloudUserInfos) {
    return getCloudUserInfos;
  }

  @Provides @Named("synchroContactList") UseCase provideSynchroContactList(
      SynchroContactList synchroContactList) {
    return synchroContactList;
  }

  @Provides @Singleton JobManager provideJobManager() {
    Configuration.Builder builder = new Configuration.Builder(application).minConsumerCount(1)
        .maxConsumerCount(3)
        .loadFactor(3)
        .consumerKeepAlive(180)
        .id("JOBS")
        .injector(job -> {
          if (job instanceof BaseJob) {
            ((BaseJob) job).inject(application.getApplicationComponent());
          }
        });

    return new JobManager(builder.build());
  }

  @Provides @Singleton RxFacebook provideRxFacebook(Context context) {
    return new RxFacebook(context);
  }

  @Provides @Singleton RxImagePicker provideRxImagePicker(Context context) {
    return new RxImagePicker(context);
  }

  @Provides @Singleton public TribeAudioManager provideTribeAudioManager(Context context) {
    return TribeAudioManager.create(context);
  }

  @Provides @Singleton
  public NotificationManagerCompat provideNotificationManagerCompat(Context context) {
    Context safeContext = ContextCompat.createDeviceProtectedStorageContext(context);

    if (safeContext == null) {
      safeContext = context;
    }

    return NotificationManagerCompat.from(safeContext);
  }

  @Provides @Singleton public NotificationBuilder provideNotificationBuilder() {
    return new NotificationBuilder(application);
  }

  // DATES
  @Provides @Singleton DateUtils provideDateUtils(
      @Named("utcSimpleDate") SimpleDateFormat utcSimpleDate, Context context) {
    return new DateUtils(utcSimpleDate, context);
  }

  @Provides @Singleton @Named("utcSimpleDate") SimpleDateFormat provideUTCSimpleDateFormat() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    return sdf;
  }

  @Provides @Singleton @Named("utcSimpleDateFull")
  SimpleDateFormat provideUTCFullSimpleDateFormat() {
    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z '(UTC)'");
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    return sdf;
  }

  @Provides @Singleton @Named("simpleDateHoursMinutes")
  SimpleDateFormat provideSimpleDateHoursMinutes() {
    return new SimpleDateFormat("hh:mm a");
  }

  @Provides @Singleton @Named("fullLetteredDate") DateFormat provideFullLetteredDate(
      Context context) {
    return DateFormat.getDateInstance(DateFormat.LONG,
        context.getResources().getConfiguration().locale);
  }

  @Provides @Singleton GameRepository provideCloudGameRepository(
      CloudGameDataRepository gameDataRepository) {
    return gameDataRepository;
  }

  @Provides @Singleton GameRepository provideDiskGameRepository(
      DiskGameDataRepository gameDataRepository) {
    return gameDataRepository;
  }

  @Provides @Singleton LiveRepository provideCloudLiveRepository(
      CloudLiveDataRepository liveDataRepository) {
    return liveDataRepository;
  }

  @Provides @Singleton LiveRepository provideDiskLiveRepository(
      DiskLiveDataRepository liveDataRepository) {
    return liveDataRepository;
  }
}
