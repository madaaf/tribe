package com.tribe.app.presentation.internal.di.modules;

import android.content.Context;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.ChatCacheImpl;
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
import com.tribe.app.data.repository.chat.ChatDataRepository;
import com.tribe.app.data.repository.tribe.CloudTribeDataRepository;
import com.tribe.app.data.repository.tribe.DiskTribeDataRepository;
import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.text.ChatRepository;
import com.tribe.app.domain.interactor.tribe.CloudMarkTribeListAsRead;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.GetCloudTribeList;
import com.tribe.app.domain.interactor.tribe.SendTribe;
import com.tribe.app.domain.interactor.tribe.TribeRepository;
import com.tribe.app.domain.interactor.user.GetCloudUserInfos;
import com.tribe.app.domain.interactor.user.UserRepository;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.UIThread;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import org.videolan.libvlc.LibVLC;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    ChatRepository provideTextRepository(ChatDataRepository textDataRepository) {
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
    Navigator provideNavigator() {
        return new Navigator();
    }

    @Provides
    @Singleton
    AccessToken provideAccessToken(Realm realm) {
        AccessToken accessToken = new AccessToken();

        final RealmResults<AccessToken> results = realm.where(AccessToken.class).findAll();
        if (results != null && results.size() > 0)
            accessToken = realm.copyFromRealm(results.get(0));

        return accessToken;
    }

    @Provides
    @Singleton
    Installation provideInstallation(Realm realm) {
        Installation installation = new Installation();

        final RealmResults<Installation> results = realm.where(Installation.class).findAll();
        if (results != null && results.size() > 0)
            installation = realm.copyFromRealm(results.get(0));

        return installation;
    }

    @Provides
    @Singleton
    User provideCurrentUser(Realm realm, AccessToken accessToken, UserRealmDataMapper userRealmDataMapper) {
        User user = new User("-1");

        final UserRealm userRealm = realm.where(UserRealm.class).equalTo("id", accessToken.getUserId()).findFirst();
        if (userRealm != null)
            user = userRealmDataMapper.transform(realm.copyFromRealm(userRealm));

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
    PaletteGrid providePaletteGrid(Context context) {
        return new PaletteGrid(context);
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
    @Singleton
    LibVLC provideLibVLC() {
        ArrayList<String> options = new ArrayList<>();
        options.add("--aout=opensles");
        options.add("--audio-time-stretch");
        //options.add("-vvv");
        return new LibVLC(options);
    }

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
    @Named("cloudGetTribes")
    UseCase provideCloudGetTribes(GetCloudTribeList getCloudTribeList) {
        return getCloudTribeList;
    }

    @Provides
    @Named("cloudUserInfos")
    UseCase provideCloudGetUserInfos(GetCloudUserInfos getCloudUserInfos) {
        return getCloudUserInfos;
    }

    @Provides
    @Named("cloudMarkTribeListAsRead")
    CloudMarkTribeListAsRead cloudMarkTribeListAsRead(CloudMarkTribeListAsRead cloudMarkTribeListAsRead) {
        return cloudMarkTribeListAsRead;
    }

    @Provides
    @Singleton
    JobManager provideJobManager() {
        Configuration.Builder builder = new Configuration.Builder(application)
            .customLogger(new CustomLogger() {
                private static final String TAG = "JOBS";
                @Override
                public boolean isDebugEnabled() {
                    return true;
                }

                @Override
                public void d(String text, Object... args) {
                    //Log.d(TAG, String.format(text, args));
                }

                @Override
                public void e(Throwable t, String text, Object... args) {
                    Log.e(TAG, String.format(text, args), t);
                }

                @Override
                public void e(String text, Object... args) {
                    Log.e(TAG, String.format(text, args));
                }

                @Override
                public void v(String text, Object... args) {

                }
            })
            .minConsumerCount(1)
            .maxConsumerCount(3)
            .loadFactor(3)
            .consumerKeepAlive(180)
            .injector(job -> {
                if (job instanceof BaseJob) {
                    ((BaseJob) job).inject(application.getApplicationComponent());
                }
            });

        return new JobManager(builder.build());
    }

    // DATES
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
}
