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
import com.tribe.app.domain.interactor.text.ChatRepository;
import com.tribe.app.presentation.internal.di.PerApplication;
import com.tribe.app.presentation.internal.di.modules.ApplicationModule;
import com.tribe.app.presentation.internal.di.modules.DataModule;
import com.tribe.app.presentation.internal.di.modules.NetModule;
import com.tribe.app.presentation.view.activity.BaseActivity;
import com.tribe.app.presentation.view.adapter.delegate.grid.MeGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.MeMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.UserMessageAdapterDelegate;
import com.tribe.app.presentation.view.widget.AvatarView;

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
    void inject(AvatarView avatarView);

    //Exposed to sub-graphs.
    Context context();

    ThreadExecutor threadExecutor();

    PostExecutionThread postExecutionThread();

    FriendshipRepository friendshipRepository();

    CloudMarvelDataRepository cloudMarvelRepository();

    DiskMarvelDataRepository diskMarvelRepository();

    CloudUserDataRepository cloudUserRepository();

    ChatRepository textRepository();

    MarvelAuthorizer marvelAuthorizer();

    TribeAuthorizer tribeAuthorizer();
}