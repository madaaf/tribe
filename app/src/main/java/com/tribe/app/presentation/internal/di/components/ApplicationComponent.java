package com.tribe.app.presentation.internal.di.components;

import android.content.Context;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.authorizer.TribeAuthorizer;
import com.tribe.app.data.network.job.DownloadTribeJob;
import com.tribe.app.data.network.job.SendTribeJob;
import com.tribe.app.data.network.job.UpdateTribesErrorStatusJob;
import com.tribe.app.data.network.job.UpdateTribesJob;
import com.tribe.app.data.network.job.UpdateUserJob;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.repository.tribe.CloudTribeDataRepository;
import com.tribe.app.data.repository.tribe.DiskTribeDataRepository;
import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.text.ChatRepository;
import com.tribe.app.presentation.internal.di.modules.ApplicationModule;
import com.tribe.app.presentation.internal.di.modules.NetModule;
import com.tribe.app.presentation.internal.di.scope.PerApplication;
import com.tribe.app.presentation.service.TribeFirebaseInstanceIDService;
import com.tribe.app.presentation.view.activity.BaseActivity;
import com.tribe.app.presentation.view.activity.LauncherActivity;
import com.tribe.app.presentation.view.adapter.delegate.grid.FriendshipGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.MeGridAdapterDelegate;
import com.tribe.app.presentation.view.camera.view.VisualizerView;
import com.tribe.app.presentation.view.component.TribeComponentView;
import com.tribe.app.presentation.view.component.TribePagerView;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.CameraWrapper;
import com.tribe.app.presentation.view.widget.PathView;
import com.tribe.app.presentation.view.widget.PlayerView;

import org.videolan.libvlc.LibVLC;

import javax.inject.Singleton;

import dagger.Component;
import io.realm.Realm;

/**
 * A component whose lifetime is the life of the application.
 */
@Singleton
@PerApplication
@Component(modules = {ApplicationModule.class, NetModule.class})
public interface ApplicationComponent {

    void inject(BaseActivity baseActivity);
    void inject(LauncherActivity launcherActivity);
    void inject(MeGridAdapterDelegate meGridAdapterDelegate);
    void inject(FriendshipGridAdapterDelegate friendshipGridAdapterDelegate);
    void inject(AvatarView avatarView);
    void inject(CameraWrapper cameraWrapper);
    void inject(VisualizerView visualizerView);
    void inject(PathView pathView);
    void inject(TribePagerView tribePagerView);
    void inject(PlayerView playerView);
    void inject(TribeComponentView tribeComponentView);
    void inject(SendTribeJob sendTribeJob);
    void inject(DownloadTribeJob downloadTribeJob);
    void inject(TribeFirebaseInstanceIDService instanceIDService);
    void inject(UpdateUserJob updateUserJob);
    void inject(UpdateTribesJob updateTribesJob);
    void inject(UpdateTribesErrorStatusJob updateTribesErrorStatusJob);

    //Exposed to sub-graphs.
    Context context();

    ThreadExecutor threadExecutor();

    PostExecutionThread postExecutionThread();

    CloudUserDataRepository cloudUserRepository();

    DiskUserDataRepository diskUserRepository();

    UserCache userCache();

    ChatRepository textRepository();

    TribeAuthorizer tribeAuthorizer();

    PhoneUtils phoneUtils();

    AccessToken accessToken();

    ScreenUtils screenUtils();

    User currentUser();

    CloudTribeDataRepository cloudTribeRepository();

    DiskTribeDataRepository diskTribeRepository();

    TribeCache tribeCache();

    LibVLC libVLC();

    JobManager jobManager();

    Realm realm();
}