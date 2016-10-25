package com.tribe.app.presentation.internal.di.modules;

import android.content.Context;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.ChatCacheImpl;
import com.tribe.app.data.cache.ContactCache;
import com.tribe.app.data.cache.ContactCacheImpl;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.TribeCacheImpl;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.cache.UserCacheImpl;
import com.tribe.app.data.executor.JobExecutor;
import com.tribe.app.data.network.job.BaseJob;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.chat.CloudChatDataRepository;
import com.tribe.app.data.repository.chat.DiskChatDataRepository;
import com.tribe.app.data.repository.tribe.CloudTribeDataRepository;
import com.tribe.app.data.repository.tribe.DiskTribeDataRepository;
import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.data.repository.user.contact.RxContacts;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.text.ChatRepository;
import com.tribe.app.domain.interactor.text.CloudManageChatHistory;
import com.tribe.app.domain.interactor.text.CloudMarkMessageListAsRead;
import com.tribe.app.domain.interactor.text.CloudUpdateStatuses;
import com.tribe.app.domain.interactor.text.DeleteChat;
import com.tribe.app.domain.interactor.text.GetDiskChatMessageList;
import com.tribe.app.domain.interactor.text.SendChat;
import com.tribe.app.domain.interactor.tribe.CloudMarkTribeAsSave;
import com.tribe.app.domain.interactor.tribe.CloudMarkTribeListAsRead;
import com.tribe.app.domain.interactor.tribe.ConfirmTribe;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.SendTribe;
import com.tribe.app.domain.interactor.tribe.TribeRepository;
import com.tribe.app.domain.interactor.user.CloudUpdateFriendship;
import com.tribe.app.domain.interactor.user.CloudUpdateUserListScore;
import com.tribe.app.domain.interactor.user.GetCloudMessageList;
import com.tribe.app.domain.interactor.user.GetCloudUserInfos;
import com.tribe.app.domain.interactor.user.RefreshHowManyFriends;
import com.tribe.app.domain.interactor.user.SynchroContactList;
import com.tribe.app.domain.interactor.user.UserRepository;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.UIThread;
import com.tribe.app.presentation.internal.di.scope.Theme;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.analytics.AmplitudeTagManager;
import com.tribe.app.presentation.utils.analytics.AnalyticsManager;
import com.tribe.app.presentation.utils.analytics.AppboyTagManager;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmResults;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;

/**
 * Dagger module that provides objects which will live during the application lifecycle.
 */
@Module
public class ApplicationModule {

    private final AndroidApplication application;
    private UserRealm userRealm;

    public ApplicationModule(AndroidApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return this.application;
    }

    @Provides
    @Singleton
    ThreadExecutor provideThreadExecutor(JobExecutor jobExecutor) {
        return jobExecutor;
    }

    @Provides
    @Singleton
    PostExecutionThread providePostExecutionThread(UIThread uiThread) {
        return uiThread;
    }

    @Provides
    @Singleton
    UserRepository provideCloudUserRepository(CloudUserDataRepository userDataRepository) {
        return userDataRepository;
    }

    @Provides
    @Singleton
    UserRepository provideDiskUserRepository(DiskUserDataRepository userDataRepository) {
        return userDataRepository;
    }

    @Provides
    @Singleton
    UserCache provideUserCache(UserCacheImpl userCache) {
        return userCache;
    }

    @Provides
    @Singleton
    ChatRepository provideTextRepository(CloudChatDataRepository textDataRepository) {
        return textDataRepository;
    }

    @Provides
    @Singleton
    ChatCache provideTextCache(ChatCacheImpl textCache) {
        return textCache;
    }

    @Provides
    @Singleton
    TribeCache provideTribeCache(TribeCacheImpl tribeCache) {
        return tribeCache;
    }

    @Provides
    @Singleton
    ContactCache provideContactCache(ContactCacheImpl contactCache) {
        return contactCache;
    }

    @Provides
    @Singleton
    RxContacts provideRxContacts(Context context, User user, PhoneUtils phoneUtils) {
        return new RxContacts(context, user, phoneUtils);
    }

    @Provides
    @Singleton
    TribeRepository provideCloudTribeRepository(CloudTribeDataRepository tribeDataRepository) {
        return tribeDataRepository;
    }

    @Provides
    @Singleton
    TribeRepository provideDiskTribeRepository(DiskTribeDataRepository tribeDataRepository) {
        return tribeDataRepository;
    }

    @Provides
    @Singleton
    ChatRepository provideDiskChatRepository(DiskChatDataRepository chatDataRepository) {
        return chatDataRepository;
    }

    @Provides
    @Singleton
    ChatRepository provideCloudChatRepository(CloudChatDataRepository chatDataRepository) {
        return chatDataRepository;
    }

    @Provides
    @Singleton
    Navigator provideNavigator() {
        return new Navigator();
    }

    @Provides
    @Singleton
    AccessToken provideAccessToken() {
        AccessToken accessToken = new AccessToken();
        Realm realmInst = Realm.getDefaultInstance();

        final RealmResults<AccessToken> results = realmInst.where(AccessToken.class).findAll();
        if (results != null && results.size() > 0) {
            accessToken = realmInst.copyFromRealm(results.get(0));
        }

        realmInst.close();

        return accessToken;
    }

    @Provides
    @Singleton
    Installation provideInstallation() {
        Installation installation = new Installation();
        Realm realmInst = Realm.getDefaultInstance();

        final RealmResults<Installation> results = realmInst.where(Installation.class).findAll();
        if (results != null && results.size() > 0)
            installation = realmInst.copyFromRealm(results.get(0));

        realmInst.close();

        return installation;
    }

    @Provides
    @Singleton
    User provideCurrentUser(Realm realm, AccessToken accessToken, UserRealmDataMapper userRealmDataMapper) {
        final User user = new User("");

        userRealm = realm.where(UserRealm.class).equalTo("id", accessToken.getUserId()).findFirst();
        if (userRealm != null) {
            userRealm.addChangeListener(element -> user.copy(userRealmDataMapper.transform(realm.copyFromRealm(userRealm), true)));

            user.copy(userRealmDataMapper.transform(realm.copyFromRealm(userRealm), true));
        }

        return user;
    }

    @Provides
    @Singleton
    @Named("userThreadSafe")
    User provideCurrentUserThreadSafe(AccessToken accessToken, UserRealmDataMapper userRealmDataMapper) {
        final User user = new User("");
        Realm realmInst = Realm.getDefaultInstance();

        UserRealm userDB = realmInst.where(UserRealm.class).equalTo("id", accessToken.getUserId()).findFirst();
        if (userDB != null)
            user.copy(userRealmDataMapper.transform(realmInst.copyFromRealm(userDB), true));

        realmInst.close();

        return user;
    }

    @Provides
    @Singleton
    UserRealm provideCurrentUserRealm(AccessToken accessToken) {
        UserRealm user = new UserRealm();
        Realm realmInst = Realm.getDefaultInstance();

        final UserRealm userRealm = realmInst.where(UserRealm.class).equalTo("id", accessToken.getUserId()).findFirst();
        if (userRealm != null) {
            user = realmInst.copyFromRealm(userRealm);
        }

        realmInst.close();

        return user;
    }

    @Provides
    @Singleton
    Realm provideRealm() {
        Realm realm = Realm.getDefaultInstance();
        return realm;
    }

    @Provides
    @Singleton
    PaletteGrid providePaletteGrid(Context context, @Theme Preference<Integer> theme) {
        return new PaletteGrid(context, theme);
    }

    @Provides
    @Singleton
    AmplitudeTagManager provideAmplitudeTagManager(Context context, @Named("userThreadSafe") User user) {
        return new AmplitudeTagManager(context, user);
    }

    @Provides
    @Singleton
    AppboyTagManager provideAppboyTagManager(Context context) {
        return new AppboyTagManager(context);
    }

    @Provides
    @Singleton
    TagManager provideTagManager(AnalyticsManager analyticsManager) {
        return analyticsManager;
    }

    @Provides
    @Singleton
    FileUtils provideFileUtils() {
        return new FileUtils();
    }

    @Provides
    @Singleton
    PhoneUtils providePhoneUtils(Context context) {
        return new PhoneUtils(context);
    }

    @Provides
    @Singleton
    ScreenUtils provideScreenUtils(Context context) {
        return new ScreenUtils(context);
    }

    @Provides
    @Singleton
    ReactiveLocationProvider provideReactiveLocationProvider(Context context) { return new ReactiveLocationProvider(context); }

    @Provides
    @Named("cloudSendTribe")
    SendTribe provideCloudSendTribe(SendTribe sendTribeDisk) {
        return sendTribeDisk;
    }

    @Provides
    @Named("diskDeleteTribe")
    DeleteTribe provideDiskDeleteTribe(DeleteTribe deleteTribeDisk) {
        return deleteTribeDisk;
    }

    @Provides
    @Named("diskConfirmTribe")
    ConfirmTribe provideDiskConfirmTribe(ConfirmTribe confirmTribeDisk) {
        return confirmTribeDisk;
    }

    @Provides
    @Named("cloudGetMessages")
    UseCase provideCloudGetMessages(GetCloudMessageList getCloudMessageList) {
        return getCloudMessageList;
    }

    @Provides
    @Named("cloudSendChat")
    SendChat provideCloudSendChat(SendChat sendChatDisk) {
        return sendChatDisk;
    }

    @Provides
    @Named("diskDeleteChat")
    DeleteChat provideDiskDeleteChat(DeleteChat deleteChatDisk) {
        return deleteChatDisk;
    }

    @Provides
    @Named("cloudUserInfos")
    UseCase provideCloudGetUserInfos(GetCloudUserInfos getCloudUserInfos) {
        return getCloudUserInfos;
    }

    @Provides
    @Named("synchroContactList")
    UseCase provideSynchroContactList(SynchroContactList synchroContactList) {
        return synchroContactList;
    }

    @Provides
    @Named("cloudMarkTribeListAsRead")
    CloudMarkTribeListAsRead cloudMarkTribeListAsRead(CloudMarkTribeListAsRead cloudMarkTribeListAsRead) {
        return cloudMarkTribeListAsRead;
    }

    @Provides
    @Named("diskGetChatMessages")
    GetDiskChatMessageList provideGetDiskChatMessageList(GetDiskChatMessageList getDiskChatMessageList) {
        return getDiskChatMessageList;
    }

    @Provides
    @Named("cloudMarkMessageListAsRead")
    CloudMarkMessageListAsRead cloudMarkMessageListAsRead(CloudMarkMessageListAsRead cloudMarkMessageListAsRead) {
        return cloudMarkMessageListAsRead;
    }

    @Provides
    @Named("updateStatuses")
    CloudUpdateStatuses provideUpdateStatuses(CloudUpdateStatuses cloudUpdateStatuses) {
        return cloudUpdateStatuses;
    }

    @Provides
    @Named("manageChatHistory")
    CloudManageChatHistory provideManageChatHistory(CloudManageChatHistory cloudManageChatHistory) {
        return cloudManageChatHistory;
    }

    @Provides
    @Named("refreshHowManyFriends")
    UseCase provideRefreshHowManyFriends(RefreshHowManyFriends refreshHowManyFriends) {
        return refreshHowManyFriends;
    }

    @Provides
    @Named("cloudMarkTribeAsSave")
    CloudMarkTribeAsSave provideCloudMarkTribeAsSave(CloudMarkTribeAsSave cloudMarkTribeAsSave) {
        return cloudMarkTribeAsSave;
    }

    @Provides
    @Named("cloudUpdateFriendship")
    CloudUpdateFriendship provideCloudUpdateFriendship(CloudUpdateFriendship cloudUpdateFriendship) {
        return cloudUpdateFriendship;
    }

    @Provides
    @Named("cloudUpdateUserListScore")
    CloudUpdateUserListScore provideCloudUpdateUserListScore(CloudUpdateUserListScore cloudUpdateUserListScore) {
        return cloudUpdateUserListScore;
    }

    @Provides
    @Singleton
    JobManager provideJobManager() {
        Configuration.Builder builder = new Configuration.Builder(application)
            .minConsumerCount(1)
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

    @Provides
    @Singleton
    @Named("jobManagerDownload")
    JobManager provideJobManagerDownload() {
        Configuration.Builder builder = new Configuration.Builder(application)
                .id("JOBS_DOWNLOAD")
                .maxConsumerCount(1)
                .consumerKeepAlive(180)
                .injector(job -> {
                    if (job instanceof BaseJob) {
                        ((BaseJob) job).inject(application.getApplicationComponent());
                    }
                });

        return new JobManager(builder.build());
    }

    @Provides
    @Singleton
    RxFacebook provideRxFacebook(Context context) {
        return new RxFacebook(context);
    }

    @Provides
    @Singleton
    RxImagePicker provideRxImagePicker(Context context) {
        return new RxImagePicker(context);
    }

    // DATES
    @Provides
    @Singleton
    DateUtils provideDateUtils(@Named("utcSimpleDate") SimpleDateFormat utcSimpleDate) {
        return new DateUtils(utcSimpleDate);
    }

    @Provides
    @Singleton
    @Named("utcSimpleDate")
    SimpleDateFormat provideUTCSimpleDateFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf;
    }

    @Provides
    @Singleton
    @Named("utcSimpleDateFull")
    SimpleDateFormat provideUTCFullSimpleDateFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z '(UTC)'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf;
    }

    @Provides
    @Singleton
    @Named("simpleDateHoursMinutes")
    SimpleDateFormat provideSimpleDateHoursMinutes() {
        return new SimpleDateFormat("hh:mm a");
    }

    @Provides
    @Singleton
    @Named("fullLetteredDate")
    DateFormat provideFullLetteredDate(Context context) {
        return DateFormat.getDateInstance(DateFormat.LONG, context.getResources().getConfiguration().locale);
    }
}
