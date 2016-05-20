package com.tribe.app.presentation.internal.di.modules;

import android.content.Context;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.tribe.app.data.cache.FriendshipCache;
import com.tribe.app.data.cache.FriendshipCacheImpl;
import com.tribe.app.data.cache.MarvelCache;
import com.tribe.app.data.cache.MarvelCacheImpl;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.cache.UserCacheImpl;
import com.tribe.app.data.executor.JobExecutor;
import com.tribe.app.data.repository.friendship.FriendshipDataRepository;
import com.tribe.app.data.repository.marvel.CloudMarvelDataRepository;
import com.tribe.app.data.repository.marvel.DiskMarvelDataRepository;
import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.data.repository.user.datasource.CloudUserDataStore;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.friendship.FriendshipRepository;
import com.tribe.app.domain.interactor.marvel.MarvelRepository;
import com.tribe.app.domain.interactor.user.UserRepository;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.UIThread;
import com.tribe.app.presentation.navigation.Navigator;

import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
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
    FriendshipRepository provideFriendshipRepository(FriendshipDataRepository friendshipDataRepository) {
        return friendshipDataRepository;
    }

    @Provides
    @Singleton
    FriendshipCache provideFriendshipCache(FriendshipCacheImpl friendshipCache) {
        return friendshipCache;
    }

    @Provides
    @Singleton
    MarvelRepository provideCloudMarvelRepository(CloudMarvelDataRepository marvelDataRepository) {
        return marvelDataRepository;
    }

    @Provides
    @Singleton
    MarvelRepository provideDiskMarvelRepository(DiskMarvelDataRepository marvelDataRepository) {
        return marvelDataRepository;
    }

    @Provides
    @Singleton
    MarvelCache provideMarvelCache(MarvelCacheImpl marvelCache) {
        return marvelCache;
    }

    @Provides
    @Singleton
    UserRepository provideCloudUserRepository(CloudUserDataRepository userDataRepository) {
        return userDataRepository;
    }

    @Provides
    @Singleton
    UserCache provideUserCache(UserCacheImpl userCache) {
        return userCache;
    }

    @Provides
    @Singleton
    Navigator provideNavigator() {
        return new Navigator();
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
