package com.tribe.app.presentation.internal.di.components;

import android.content.Context;
import android.content.SharedPreferences;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.authorizer.TribeAuthorizer;
import com.tribe.app.data.network.job.BaseJob;
import com.tribe.app.data.network.job.DownloadChatVideoJob;
import com.tribe.app.data.network.job.DownloadTribeJob;
import com.tribe.app.data.network.job.MarkMessageListAsReadJob;
import com.tribe.app.data.network.job.MarkTribeAsSavedJob;
import com.tribe.app.data.network.job.MarkTribeListAsReadJob;
import com.tribe.app.data.network.job.RefreshHowManyFriendsJob;
import com.tribe.app.data.network.job.SendChatJob;
import com.tribe.app.data.network.job.SendTribeJob;
import com.tribe.app.data.network.job.SynchroContactsJob;
import com.tribe.app.data.network.job.UpdateChatHistoryJob;
import com.tribe.app.data.network.job.UpdateChatMessagesJob;
import com.tribe.app.data.network.job.UpdateFriendshipJob;
import com.tribe.app.data.network.job.UpdateMessagesErrorStatusJob;
import com.tribe.app.data.network.job.UpdateMessagesJob;
import com.tribe.app.data.network.job.UpdateMessagesVideoErrorStatusJob;
import com.tribe.app.data.network.job.UpdateScoreJob;
import com.tribe.app.data.network.job.UpdateTribeDownloadedJob;
import com.tribe.app.data.network.job.UpdateTribeListNotSeenStatusJob;
import com.tribe.app.data.network.job.UpdateTribesErrorStatusJob;
import com.tribe.app.data.network.job.UpdateUserJob;
import com.tribe.app.data.network.job.UpdateUserListScoreJob;
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
import com.tribe.app.presentation.internal.di.scope.AudioDefault;
import com.tribe.app.presentation.internal.di.scope.DistanceUnits;
import com.tribe.app.presentation.internal.di.scope.Filter;
import com.tribe.app.presentation.internal.di.scope.InvisibleMode;
import com.tribe.app.presentation.internal.di.scope.LastMessageRequest;
import com.tribe.app.presentation.internal.di.scope.LastUserRequest;
import com.tribe.app.presentation.internal.di.scope.LocationContext;
import com.tribe.app.presentation.internal.di.scope.Memories;
import com.tribe.app.presentation.internal.di.scope.PerApplication;
import com.tribe.app.presentation.internal.di.scope.Preload;
import com.tribe.app.presentation.internal.di.scope.SpeedPlayback;
import com.tribe.app.presentation.internal.di.scope.Theme;
import com.tribe.app.presentation.internal.di.scope.WeatherUnits;
import com.tribe.app.presentation.service.TribeFirebaseInstanceIDService;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.view.activity.BaseActivity;
import com.tribe.app.presentation.view.activity.LauncherActivity;
import com.tribe.app.presentation.view.adapter.delegate.contact.SearchResultGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.MeGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.RecipientGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.PhotoMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.TutorialMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.VideoMessageAdapterDelegate;
import com.tribe.app.presentation.view.camera.view.HistogramVisualizerView;
import com.tribe.app.presentation.view.component.PullToSearchContainer;
import com.tribe.app.presentation.view.component.PullToSearchView;
import com.tribe.app.presentation.view.component.SettingItemView;
import com.tribe.app.presentation.view.component.TribeComponentView;
import com.tribe.app.presentation.view.component.TribePagerView;
import com.tribe.app.presentation.view.fragment.BaseFragment;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.video.LegacyMediaPlayer;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.ButtonCardView;
import com.tribe.app.presentation.view.widget.ButtonPointsView;
import com.tribe.app.presentation.view.widget.CameraWrapper;
import com.tribe.app.presentation.view.widget.IntroVideoView;
import com.tribe.app.presentation.view.widget.LabelButton;
import com.tribe.app.presentation.view.widget.PathView;
import com.tribe.app.presentation.view.widget.PlayerView;
import com.tribe.app.presentation.view.widget.TribeVideoView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import io.realm.Realm;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;

/**
 * A component whose lifetime is the life of the application.
 */
@Singleton
@PerApplication
@Component(modules = {ApplicationModule.class, NetModule.class})
public interface ApplicationComponent {

    void inject(BaseFragment baseFragment);
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
    void inject(TribeFirebaseInstanceIDService instanceIDService);
    void inject(PhotoMessageAdapterDelegate photoMessageAdapterDelegate);
    void inject(TutorialMessageAdapterDelegate tutorialMessageAdapterDelegate);
    void inject(LabelButton labelButton);
    void inject(IntroVideoView introVideoView);
    void inject(LegacyMediaPlayer legacyMediaPlayer);
    void inject(VideoMessageAdapterDelegate videoMessageAdapterDelegate);
    void inject(TribeVideoView tribeVideoView);
    void inject(ButtonPointsView buttonPointsView);
    void inject(SearchResultGridAdapterDelegate searchResultGridAdapterDelegate);
    void inject(PullToSearchContainer pullToSearchContainer);
    void inject(PullToSearchView pullToSearchView);
    void inject(SettingItemView settingItemView);
    void inject(ButtonCardView buttonCardView);

    // JOBS
    void inject(BaseJob baseJob);
    void inject(SendTribeJob sendTribeJob);
    void inject(DownloadTribeJob downloadTribeJob);
    void inject(SendChatJob sendChatJob);
    void inject(MarkMessageListAsReadJob markMessageListAsReadJob);
    void inject(UpdateUserJob updateUserJob);
    void inject(UpdateMessagesJob updateMessagesJob);
    void inject(UpdateTribesErrorStatusJob updateTribesErrorStatusJob);
    void inject(MarkTribeListAsReadJob markTribeListAsReadJob);
    void inject(UpdateMessagesErrorStatusJob updateMessagesErrorStatusJob);
    void inject(UpdateTribeListNotSeenStatusJob updateTribeListNotSeenStatusJob);
    void inject(UpdateChatMessagesJob updateChatMessagesJob);
    void inject(UpdateTribeDownloadedJob updateTribeDownloadedJob);
    void inject(DownloadChatVideoJob downloadChatVideoJob);
    void inject(UpdateChatHistoryJob updateChatHistoryJob);
    void inject(SynchroContactsJob synchroContactsJob);
    void inject(RefreshHowManyFriendsJob refreshHowManyFriendsJob);
    void inject(UpdateMessagesVideoErrorStatusJob messagesVideoErrorStatusJob);
    void inject(UpdateScoreJob updateScoreJob);
    void inject(MarkTribeAsSavedJob markTribeAsSavedJob);
    void inject(UpdateFriendshipJob updateFriendshipJob);
    void inject(UpdateUserListScoreJob updateUserListScoreJob);

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

    JobManager jobManager();

    Realm realm();

    @Named("simpleDateHoursMinutes") SimpleDateFormat simpleDateHoursMinutes();

    @Named("fullLetteredDate") DateFormat fullLetteredDate();

    DateUtils dateUtils();

    PaletteGrid paletteGrid();

    @SpeedPlayback
    Preference<Float> speedPlayblack();

    @DistanceUnits
    Preference<String> distanceUnits();

    @WeatherUnits
    Preference<String> weatherUnits();

    @Memories
    Preference<Boolean> memories();

    @AudioDefault
    Preference<Boolean> audioDefault();

    @LocationContext
    Preference<Boolean> locationContext();

    @Preload
    Preference<Boolean> preload();

    @Theme
    Preference<Integer> theme();

    @Filter
    Preference<Integer> filter();

    @InvisibleMode
    Preference<Boolean> invisibleMode();

    @LastMessageRequest
    Preference<String> lastMessageRequest();

    @LastUserRequest
    Preference<String> lastUserRequest();

    RxFacebook rxFacebook();

    RxImagePicker rxImagePicker();

    SharedPreferences sharedPreferences();

    FileUtils fileUtils();

    TagManager tagManager();

    ReactiveLocationProvider reactiveLocationProvider();
}