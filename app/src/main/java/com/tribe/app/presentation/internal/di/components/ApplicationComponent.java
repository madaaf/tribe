package com.tribe.app.presentation.internal.di.components;

import android.content.Context;
import android.content.SharedPreferences;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.DownloadTribeService;
import com.tribe.app.data.network.authorizer.TribeAuthorizer;
import com.tribe.app.data.network.job.BaseJob;
import com.tribe.app.data.network.job.DeleteContactsABJob;
import com.tribe.app.data.network.job.DeleteContactsFBJob;
import com.tribe.app.data.network.job.DeleteMessageJob;
import com.tribe.app.data.network.job.DownloadChatVideoJob;
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
import com.tribe.app.data.network.job.UpdateTribeToDownloadJob;
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
import com.tribe.app.presentation.internal.di.scope.PerApplication;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.service.TribeFirebaseInstanceIDService;
import com.tribe.app.presentation.service.TribeFirebaseMessagingService;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.analytics.AnalyticsManager;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.utils.preferences.AudioDefault;
import com.tribe.app.presentation.utils.preferences.DebugMode;
import com.tribe.app.presentation.utils.preferences.DistanceUnits;
import com.tribe.app.presentation.utils.preferences.Filter;
import com.tribe.app.presentation.utils.preferences.HasRatedApp;
import com.tribe.app.presentation.utils.preferences.HasReceivedPointsForCameraPermission;
import com.tribe.app.presentation.utils.preferences.InvisibleMode;
import com.tribe.app.presentation.utils.preferences.LastMessageRequest;
import com.tribe.app.presentation.utils.preferences.LastOnlineNotification;
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.utils.preferences.LastUserRequest;
import com.tribe.app.presentation.utils.preferences.LastVersionCode;
import com.tribe.app.presentation.utils.preferences.LocationContext;
import com.tribe.app.presentation.utils.preferences.Memories;
import com.tribe.app.presentation.utils.preferences.Preload;
import com.tribe.app.presentation.utils.preferences.PushNotifications;
import com.tribe.app.presentation.utils.preferences.ShareProfile;
import com.tribe.app.presentation.utils.preferences.SpeedPlayback;
import com.tribe.app.presentation.utils.preferences.Theme;
import com.tribe.app.presentation.utils.preferences.TribeSentCount;
import com.tribe.app.presentation.utils.preferences.TutorialState;
import com.tribe.app.presentation.utils.preferences.UISounds;
import com.tribe.app.presentation.utils.preferences.WasAskedForCameraPermission;
import com.tribe.app.presentation.utils.preferences.WeatherUnits;
import com.tribe.app.presentation.view.activity.BaseActivity;
import com.tribe.app.presentation.view.activity.LauncherActivity;
import com.tribe.app.presentation.view.adapter.delegate.contact.SearchResultGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.friend.MemberListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.friend.UserListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.MeGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.RecipientGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.PhotoMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.TutorialMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.VideoMessageAdapterDelegate;
import com.tribe.app.presentation.view.camera.view.GlPreview;
import com.tribe.app.presentation.view.camera.view.HistogramVisualizerView;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.component.FilterView;
import com.tribe.app.presentation.view.component.RatingView;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.component.TopBarContainer;
import com.tribe.app.presentation.view.component.TopBarView;
import com.tribe.app.presentation.view.component.TribeComponentView;
import com.tribe.app.presentation.view.component.TribePagerView;
import com.tribe.app.presentation.view.component.VisualizerView;
import com.tribe.app.presentation.view.component.group.AddMembersGroupView;
import com.tribe.app.presentation.view.component.group.CreateGroupView;
import com.tribe.app.presentation.view.component.group.GroupSuggestionView;
import com.tribe.app.presentation.view.component.group.MembersGroupView;
import com.tribe.app.presentation.view.component.group.SettingsGroupView;
import com.tribe.app.presentation.view.component.group.UpdateGroupView;
import com.tribe.app.presentation.view.fragment.BaseFragment;
import com.tribe.app.presentation.view.tutorial.Tutorial;
import com.tribe.app.presentation.view.tutorial.TutorialManager;
import com.tribe.app.presentation.view.utils.ImageUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.video.LegacyMediaPlayer;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.ButtonCardView;
import com.tribe.app.presentation.view.widget.ButtonPointsView;
import com.tribe.app.presentation.view.widget.CameraWrapper;
import com.tribe.app.presentation.view.widget.IntroVideoView;
import com.tribe.app.presentation.view.widget.LabelButton;
import com.tribe.app.presentation.view.widget.PathView;
import com.tribe.app.presentation.view.widget.PlayerView;
import com.tribe.app.presentation.view.widget.SyncView;
import com.tribe.app.presentation.view.widget.TextViewAnimatedDots;
import com.tribe.app.presentation.view.widget.TribeVideoView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

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

    void inject(Navigator navigator);
    void inject(Tutorial tutorial);

    void inject(BaseFragment baseFragment);
    void inject(BaseActivity baseActivity);
    void inject(TribeFirebaseMessagingService tribeFirebaseMessagingService);
    void inject(AnalyticsManager analyticsManager);

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
    void inject(TopBarContainer topBarContainer);
    void inject(TopBarView topBarView);
    void inject(FilterView filterView);

    void inject(ButtonCardView buttonCardView);
    void inject(GlPreview glPreview);
    void inject(VisualizerView visualizerView);
    void inject(RatingView ratingView);
    void inject(TextViewAnimatedDots textViewAnimatedDots);
    void inject(TileView tileView);
    void inject(CreateGroupView tileView);
    void inject(GroupSuggestionView groupSuggestionView);
    void inject(AddMembersGroupView addMembersGroupView);
    void inject(ActionView actionView);
    void inject(SettingsGroupView settingsGroupView);
    void inject(UpdateGroupView updateGroupView);
    void inject(MembersGroupView membersGroupView);
    void inject(MemberListAdapterDelegate memberListAdapterDelegate);
    void inject(SyncView syncView);
    void inject(UserListAdapterDelegate userListAdapterDelegate);

    // JOBS
    void inject(BaseJob baseJob);
    void inject(SendTribeJob sendTribeJob);
    void inject(SendChatJob sendChatJob);
    void inject(MarkMessageListAsReadJob markMessageListAsReadJob);
    void inject(UpdateUserJob updateUserJob);
    void inject(UpdateMessagesJob updateMessagesJob);
    void inject(UpdateTribesErrorStatusJob updateTribesErrorStatusJob);
    void inject(MarkTribeListAsReadJob markTribeListAsReadJob);
    void inject(UpdateMessagesErrorStatusJob updateMessagesErrorStatusJob);
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
    void inject(DeleteMessageJob deleteMessageJob);
    void inject(UpdateTribeToDownloadJob updateTribeToDownloadJob);
    void inject(DeleteContactsABJob deleteContactsABJob);
    void inject(DeleteContactsFBJob deleteContactsFBJob);

    // SERVICES
    void inject(DownloadTribeService downloadTribeService);

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

    @Named("jobManagerDownload") JobManager jobManagerDownload();

    Realm realm();

    @Named("simpleDateHoursMinutes") SimpleDateFormat simpleDateHoursMinutes();

    @Named("fullLetteredDate") DateFormat fullLetteredDate();

    DateUtils dateUtils();

    PaletteGrid paletteGrid();

    ImageUtils imageUtils();

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

    @AddressBook
    Preference<Boolean> addressBook();

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

    @ShareProfile
    Preference<Boolean> shareProfile();

    @HasReceivedPointsForCameraPermission
    Preference<Boolean> hasReceivedPointsForCameraPermission();

    @WasAskedForCameraPermission
    Preference<Boolean> wasAskedForCameraPermission();

    @LastSync
    Preference<Long> lastSync();

    @TribeSentCount
    Preference<Integer> tribeSentCount();

    @LastVersionCode
    Preference<Integer> lastVersionCode();

    @HasRatedApp
    Preference<Boolean> hasRatedApp();

    @TutorialState
    Preference<Set<String>> tutorialState();

    @DebugMode
    Preference<Boolean> debugMode();

    @LastOnlineNotification
    Preference<Long> lastOnlineNotification();

    @UISounds
    Preference<Boolean> uiSounds();

    @PushNotifications
    Preference<Boolean> pushNotifications();

    SoundManager soundManager();

    TutorialManager tutorialManager();
}