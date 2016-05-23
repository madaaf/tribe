package com.tribe.app.presentation.internal.di.components;

import android.content.Context;

import com.tribe.app.data.network.authorizer.MarvelAuthorizer;
import com.tribe.app.data.network.authorizer.TribeAuthorizer;
import com.tribe.app.data.repository.marvel.CloudMarvelDataRepository;
import com.tribe.app.data.repository.marvel.DiskMarvelDataRepository;
import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.friendship.FriendshipRepository;
import com.tribe.app.domain.interactor.text.TextRepository;
import com.tribe.app.presentation.internal.di.PerApplication;
import com.tribe.app.presentation.internal.di.modules.ApplicationModule;
import com.tribe.app.presentation.internal.di.modules.NetModule;
import com.tribe.app.presentation.view.activity.BaseActivity;
import com.tribe.app.presentation.view.adapter.delegate.MeGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.UserGridAdapterDelegate;

import dagger.Component;

import javax.inject.Singleton;

/**
 * A component whose lifetime is the life of the application.
 */
@Singleton
@PerApplication
@Component(modules = {ApplicationModule.class, NetModule.class})
public interface ApplicationComponent {

    void inject(BaseActivity baseActivity);
    void inject(MeGridAdapterDelegate meGridAdapterDelegate);
    void inject(UserGridAdapterDelegate userGridAdapterDelegate);

    //Exposed to sub-graphs.
    Context context();

    ThreadExecutor threadExecutor();

    PostExecutionThread postExecutionThread();

    FriendshipRepository friendshipRepository();

    CloudMarvelDataRepository cloudMarvelRepository();

    DiskMarvelDataRepository diskMarvelRepository();

    CloudUserDataRepository cloudUserRepository();

    TextRepository textRepository();

    MarvelAuthorizer marvelAuthorizer();

    TribeAuthorizer tribeAuthorizer();
}