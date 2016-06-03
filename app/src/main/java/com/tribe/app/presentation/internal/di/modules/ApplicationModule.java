package com.tribe.app.presentation.internal.di.modules;

import android.content.Context;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.ChatCacheImpl;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.cache.UserCacheImpl;
import com.tribe.app.data.executor.JobExecutor;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.repository.chat.ChatDataRepository;
import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.text.ChatRepository;
import com.tribe.app.domain.interactor.user.UserRepository;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.UIThread;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.utils.PaletteGrid;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmResults;

import javax.inject.Singleton;

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
    Navigator provideNavigator() {
        return new Navigator();
    }

    @Provides
    @Singleton
    AccessToken provideAccessToken() {
        AccessToken accessToken = new AccessToken();

        Realm realm = Realm.getDefaultInstance();
        final RealmResults<AccessToken> results = realm.where(AccessToken.class).findAll();
        if (results != null && results.size() > 0)
            accessToken = realm.copyFromRealm(results.get(0));
        realm.close();

        return accessToken;
    }

    @Provides
    @Singleton
    PaletteGrid providePaletteGrid(Context context) {
        return new PaletteGrid(context);
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
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(1) // always keep at least one consumer alive
                .maxConsumerCount(3) // up to 3 consumers at a time
                .loadFactor(3) // 3 jobs per consumer
                .consumerKeepAlive(120); // wait 2 minute

        return new JobManager(builder.build());
    }
}
