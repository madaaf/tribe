package com.tribe.app.presentation.internal.di.components;

import android.content.Context;

import com.birbit.android.jobqueue.JobManager;
import com.squareup.picasso.Picasso;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.authorizer.TribeAuthorizer;
import com.tribe.app.data.network.job.DownloadTribeJob;
import com.tribe.app.data.network.job.MarkTribeListAsReadJob;
import com.tribe.app.data.network.job.SendChatJob;
import com.tribe.app.data.network.job.SendTribeJob;
import com.tribe.app.data.network.job.UpdateMessagesJob;
import com.tribe.app.data.network.job.UpdateTribesErrorStatusJob;
import com.tribe.app.data.network.job.UpdateUserJob;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.repository.chat.CloudChatDataRepository;
import com.tribe.app.data.repository.chat.DiskChatDataRepository;
import com.tribe.app.data.repository.tribe.CloudTribeDataRepository;
import com.tribe.app.data.repository.tribe.DiskTribeDataRepository;
import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.presentation.internal.di.modules.ApplicationModule;
import com.tribe.app.presentation.internal.di.modules.NetModule;
import com.tribe.app.presentation.internal.di.scope.PerApplication;
import com.tribe.app.presentation.service.TribeFirebaseInstanceIDService;
import com.tribe.app.presentation.view.activity.BaseActivity;
import com.tribe.app.presentation.view.activity.LauncherActivity;
import com.tribe.app.presentation.view.adapter.delegate.grid.MeGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.RecipientGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.PhotoMessageAdapterDelegate;
import com.tribe.app.presentation.view.camera.view.HistogramVisualizerView;
import com.tribe.app.presentation.view.component.TribeComponentView;
import com.tribe.app.presentation.view.component.TribePagerView;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.CameraWrapper;
import com.tribe.app.presentation.view.widget.PathView;
import com.tribe.app.presentation.view.widget.PlayerView;

import org.videolan.libvlc.LibVLC;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.inject.Named;
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
    void inject(RecipientGridAdapterDelegate recipientGridAdapterDelegate);
    void inject(AvatarView avatarView);
    void inject(CameraWrapper cameraWrapper);
    void inject(HistogramVisualizerView visualizerView);
    void inject(PathView pathView);
    void inject(TribePagerView tribePagerView);
    void inject(PlayerView playerView);
    void inject(TribeComponentView tribeComponentView);
    void inject(SendTribeJob sendTribeJob);
    void inject(DownloadTribeJob downloadTribeJob);
    void inject(TribeFirebaseInstanceIDService instanceIDService);
    void inject(UpdateUserJob updateUserJob);
    void inject(UpdateMessagesJob updateMessagesJob);
    void inject(UpdateTribesErrorStatusJob updateTribesErrorStatusJob);
    void inject(MarkTribeListAsReadJob markTribeListAsReadJob);
    void inject(PhotoMessageAdapterDelegate photoMessageAdapterDelegate);
    void inject(SendChatJob sendChatJob);

    //Exposed to sub-graphs.
    Context context();

    ThreadExecutor threadExecutor();

    PostExecutionThread postExecutionThread();

    CloudUserDataRepository cloudUserRepository();

    DiskUserDataRepository diskUserRepository();

    CloudTribeDataRepository cloudTribeRepository();

    DiskTribeDataRepository diskTribeRepository();

    CloudChatDataRepository cloudChatRepository();

    DiskChatDataRepository diskChatRepository();

    TribeAuthorizer tribeAuthorizer();

    PhoneUtils phoneUtils();

    AccessToken accessToken();

    ScreenUtils screenUtils();

    User currentUser();

    TribeCache tribeCache();

    UserCache userCache();

    ChatCache chatCache();

    LibVLC libVLC();

    JobManager jobManager();

    Realm realm();

    Picasso picasso();

    @Named("simpleDateHoursMinutes") SimpleDateFormat simpleDateHoursMinutes();

    @Named("fullLetteredDate") DateFormat fullLetteredDate();
}