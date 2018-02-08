package com.tribe.app.presentation.internal.di.components;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationManagerCompat;
import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.GameCache;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.WSService;
import com.tribe.app.data.network.authorizer.TribeAuthorizer;
import com.tribe.app.data.network.job.BaseJob;
import com.tribe.app.data.network.job.DeleteContactsABJob;
import com.tribe.app.data.network.job.DeleteContactsFBJob;
import com.tribe.app.data.network.job.DeleteRoomJob;
import com.tribe.app.data.network.job.RemoveNewStatusContactJob;
import com.tribe.app.data.network.job.SynchroContactsJob;
import com.tribe.app.data.network.job.UnhideShortcutJob;
import com.tribe.app.data.network.job.UpdateUserJob;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.BadgeRealm;
import com.tribe.app.data.repository.chat.CloudChatDataRepository;
import com.tribe.app.data.repository.chat.DiskChatDataRepository;
import com.tribe.app.data.repository.game.CloudGameDataRepository;
import com.tribe.app.data.repository.game.DiskGameDataRepository;
import com.tribe.app.data.repository.live.CloudLiveDataRepository;
import com.tribe.app.data.repository.live.DiskLiveDataRepository;
import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.presentation.TribeBroadcastReceiver;
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
import com.tribe.app.presentation.utils.preferences.CallTagsMap;
import com.tribe.app.presentation.utils.preferences.ChatShortcutData;
import com.tribe.app.presentation.utils.preferences.CounterOfCallsForGrpButton;
import com.tribe.app.presentation.utils.preferences.DebugMode;
import com.tribe.app.presentation.utils.preferences.FullscreenNotificationState;
import com.tribe.app.presentation.utils.preferences.FullscreenNotifications;
import com.tribe.app.presentation.utils.preferences.GameData;
import com.tribe.app.presentation.utils.preferences.ImmersiveCallState;
import com.tribe.app.presentation.utils.preferences.InvisibleMode;
import com.tribe.app.presentation.utils.preferences.IsGroupCreated;
import com.tribe.app.presentation.utils.preferences.LastImOnline;
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.utils.preferences.LastSyncGameData;
import com.tribe.app.presentation.utils.preferences.LastVersionCode;
import com.tribe.app.presentation.utils.preferences.LookupResult;
import com.tribe.app.presentation.utils.preferences.MinutesOfCalls;
import com.tribe.app.presentation.utils.preferences.MissedPlayloadNotification;
import com.tribe.app.presentation.utils.preferences.NewContactsTooltip;
import com.tribe.app.presentation.utils.preferences.NumberOfCalls;
import com.tribe.app.presentation.utils.preferences.RoutingMode;
import com.tribe.app.presentation.utils.preferences.SupportIsUsed;
import com.tribe.app.presentation.utils.preferences.SupportRequestId;
import com.tribe.app.presentation.utils.preferences.SupportUserId;
import com.tribe.app.presentation.utils.preferences.Theme;
import com.tribe.app.presentation.utils.preferences.TribeState;
import com.tribe.app.presentation.utils.preferences.UISounds;
import com.tribe.app.presentation.utils.preferences.UserPhoneNumber;
import com.tribe.app.presentation.utils.preferences.Walkthrough;
import com.tribe.app.presentation.view.activity.BaseActivity;
import com.tribe.app.presentation.view.activity.LauncherActivity;
import com.tribe.app.presentation.view.activity.SmsListener;
import com.tribe.app.presentation.view.adapter.delegate.base.BaseListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.base.BaseNotifAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.common.RecipientAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.ContactToInviteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.EmptyContactAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.SearchResultGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.UserToAddAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.friend.ManageShortcutListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.friend.RecipientListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.friend.UserListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.gamesfilters.FooterAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.gamesfilters.GameAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.gamesfilters.GamesFiltersAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.CallRouletteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.LiveInviteHeaderAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.LiveInviteSubHeaderAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.ShareAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.ShortcutEmptyInviteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.ShortcutInviteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.ShortcutInviteFullAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserRoomAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.leaderboard.LeaderboardDetailsAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.leaderboard.LeaderboardUserAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.newchat.ShortcutNewChatAdapterDelegate;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.component.VisualizerView;
import com.tribe.app.presentation.view.component.common.ShortcutListView;
import com.tribe.app.presentation.view.component.games.GameUserCardView;
import com.tribe.app.presentation.view.component.home.HomeWalkthroughVideoView;
import com.tribe.app.presentation.view.component.home.HomeWalkthroughView;
import com.tribe.app.presentation.view.component.home.NewGameView;
import com.tribe.app.presentation.view.component.home.TopBarContainer;
import com.tribe.app.presentation.view.component.home.TopBarLogoView;
import com.tribe.app.presentation.view.component.home.TopBarView;
import com.tribe.app.presentation.view.component.live.LiveContainer;
import com.tribe.app.presentation.view.component.live.LiveDropZoneView;
import com.tribe.app.presentation.view.component.live.LiveHangUpView;
import com.tribe.app.presentation.view.component.live.LiveInviteBottomView;
import com.tribe.app.presentation.view.component.live.LiveInviteView;
import com.tribe.app.presentation.view.component.live.LiveRowView;
import com.tribe.app.presentation.view.component.live.LiveView;
import com.tribe.app.presentation.view.component.live.LiveViewFake;
import com.tribe.app.presentation.view.component.live.TileInviteView;
import com.tribe.app.presentation.view.component.live.game.battlemusic.GameBattleMusicCategoriesView;
import com.tribe.app.presentation.view.component.live.game.battlemusic.GameBattleMusicCategoryView;
import com.tribe.app.presentation.view.component.live.game.battlemusic.GameBattleMusicPlayView;
import com.tribe.app.presentation.view.component.live.game.birdrush.BirdRush;
import com.tribe.app.presentation.view.component.live.game.common.GameAnswerView;
import com.tribe.app.presentation.view.component.live.game.common.GameAnswersView;
import com.tribe.app.presentation.view.component.live.game.trivia.GameTriviaCategoryView;
import com.tribe.app.presentation.view.fragment.BaseFragment;
import com.tribe.app.presentation.view.notification.NotificationBuilder;
import com.tribe.app.presentation.view.utils.ImageUtils;
import com.tribe.app.presentation.view.utils.MissedCallManager;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.video.LegacyMediaPlayer;
import com.tribe.app.presentation.view.widget.DiceView;
import com.tribe.app.presentation.view.widget.IntroVideoView;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import com.tribe.app.presentation.view.widget.PlayerView;
import com.tribe.app.presentation.view.widget.SyncView;
import com.tribe.app.presentation.view.widget.TooltipView;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import com.tribe.app.presentation.view.widget.picto.PictoChatView;
import com.tribe.app.presentation.view.widget.picto.PictoLiveView;
import com.tribe.app.presentation.view.widget.text.TextHomeNameActionView;
import com.tribe.app.presentation.view.widget.text.TextShortcutNameView;
import com.tribe.tribelivesdk.di.LiveModule;
import com.tribe.tribelivesdk.stream.TribeAudioManager;
import dagger.Component;
import io.realm.Realm;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * A component whose lifetime is the life of the application.
 */
@Singleton @PerApplication
@Component(modules = { ApplicationModule.class, NetModule.class, LiveModule.class })
public interface ApplicationComponent {

  void inject(Navigator navigator);

  void inject(BaseFragment baseFragment);

  void inject(BaseActivity baseActivity);

  void inject(TribeFirebaseMessagingService tribeFirebaseMessagingService);

  void inject(AnalyticsManager analyticsManager);

  void inject(LauncherActivity launcherActivity);

  void inject(AvatarView avatarView);

  void inject(BirdRush birdRush);

  void inject(PlayerView playerView);

  void inject(TribeFirebaseInstanceIDService instanceIDService);

  void inject(IntroVideoView introVideoView);

  void inject(LegacyMediaPlayer legacyMediaPlayer);

  void inject(SearchResultGridAdapterDelegate searchResultGridAdapterDelegate);

  void inject(TopBarContainer topBarContainer);

  void inject(TopBarView topBarView);

  void inject(DiceView diceView);

  void inject(VisualizerView visualizerView);

  void inject(ActionView actionView);

  void inject(SyncView syncView);

  void inject(UserListAdapterDelegate userListAdapterDelegate);

  void inject(LiveView liveView);

  void inject(LiveContainer liveContainer);

  void inject(LiveInviteView liveInviteView);

  void inject(UserRoomAdapterDelegate liveInviteAdapterDelegate);

  void inject(CallRouletteAdapterDelegate callRouletteAdapterDelegate);

  void inject(LiveRowView liveRowView);

  void inject(RecipientListAdapterDelegate recipientListAdapterDelegate);

  void inject(LiveNotificationView liveNotificationView);

  void inject(BaseListAdapterDelegate baseListAdapterDelegate);

  void inject(TooltipView tooltipView);

  void inject(NewGameView newChatButton);

  void inject(ManageShortcutListAdapterDelegate manageFriendshipListAdapterDelegate);

  void inject(GamesFiltersAdapterDelegate gamesFiltersAdapterDelegate);

  void inject(NewAvatarView newAvatarView);

  void inject(NotificationBuilder notificationBuilder);

  void inject(ShortcutListView homeListView);

  void inject(RecipientAdapterDelegate homeAdapterDelegate);

  void inject(PictoChatView pictoChatView);

  void inject(PictoLiveView pictoLiveView);

  void inject(TextHomeNameActionView textHomeNameStatusView);

  void inject(LiveViewFake liveViewFake);

  void inject(TextShortcutNameView textShortcutNameView);

  void inject(TopBarLogoView topBarLogoView);

  void inject(TribeBroadcastReceiver receiver);

  void inject(UserToAddAdapterDelegate userToAddAdapterDelegate);

  void inject(ContactToInviteAdapterDelegate contactToInviteAdapterDelegate);

  void inject(EmptyContactAdapterDelegate emptyContactAdapterDelegate);

  void inject(ShortcutInviteAdapterDelegate shortcutInviteAdapterDelegate);

  void inject(ShortcutEmptyInviteAdapterDelegate shortcutEmptyInviteAdapterDelegate);

  void inject(TileInviteView tileInviteView);

  void inject(LiveInviteBottomView liveInviteBottomView);

  void inject(LiveInviteHeaderAdapterDelegate shortcutInviteHeaderAdapterDelegate);

  void inject(LiveInviteSubHeaderAdapterDelegate shortcutInviteSubHeaderAdapterDelegate);

  void inject(ShortcutInviteFullAdapterDelegate shortcutInviteFullAdapterDelegate);

  void inject(LiveDropZoneView liveDropZone);

  void inject(ShortcutNewChatAdapterDelegate shortcutNewChatAdapterDelegate);

  void inject(HomeWalkthroughView homeWalkthroughView);

  void inject(HomeWalkthroughVideoView homeWalkthroughVideoView);

  void inject(GameAdapterDelegate gameAdapterDelegate);

  void inject(FooterAdapterDelegate footerAdapterDelegate);

  void inject(LiveHangUpView liveHangUpView);

  void inject(LeaderboardUserAdapterDelegate leaderboardUserAdapterDelegate);

  void inject(LeaderboardDetailsAdapterDelegate leaderboardDetailsAdapterDelegate);

  void inject(GameUserCardView userGameCardView);

  void inject(BaseNotifAdapterDelegate baseNotifAdapterDelegate);

  void inject(GameTriviaCategoryView gameCategoryView);

  void inject(GameAnswerView gameAnswerQuestionView);

  void inject(GameAnswersView gameTriviaQuestionsView);

  void inject(GameBattleMusicCategoryView gameBattleMusicCategoryView);

  void inject(GameBattleMusicCategoriesView gameBattleMusicCategoriesView);

  void inject(GameBattleMusicPlayView gameBattleMusicPlayView);

  void inject(ShareAdapterDelegate shareAdapterDelegate);

  // JOBS
  void inject(BaseJob baseJob);

  void inject(UnhideShortcutJob unhideShortcutJob);

  void inject(UpdateUserJob updateUserJob);

  void inject(SynchroContactsJob synchroContactsJob);

  void inject(DeleteContactsABJob deleteContactsABJob);

  void inject(DeleteContactsFBJob deleteContactsFBJob);

  void inject(RemoveNewStatusContactJob removeNewStatusContactJob);

  void inject(DeleteRoomJob deleteRoomJob);

  // SERVICES
  void inject(WSService wsService);

  //Exposed to sub-graphs.
  Context context();

  ThreadExecutor threadExecutor();

  PostExecutionThread postExecutionThread();

  CloudUserDataRepository cloudUserRepository();

  DiskUserDataRepository diskUserRepository();

  CloudGameDataRepository cloudGameRepository();

  DiskGameDataRepository diskGameRepository();

  CloudChatDataRepository cloudChatRepository();

  DiskChatDataRepository diskChatRepository();

  CloudLiveDataRepository cloudLiveRepository();

  DiskLiveDataRepository diskLiveRepository();

  TribeAuthorizer tribeAuthorizer();

  PhoneUtils phoneUtils();

  AccessToken accessToken();

  ScreenUtils screenUtils();

  Navigator navigator();

  IntentFilter IntentFilter();

  SmsListener SmsListener();

  User currentUser();

  UserCache userCache();

  LiveCache liveCache();

  ChatCache chatCache();

  GameCache gameCache();

  JobManager jobManager();

  Realm realm();

  @Named("simpleDateHoursMinutes") SimpleDateFormat simpleDateHoursMinutes();

  @Named("fullLetteredDate") DateFormat fullLetteredDate();

  DateUtils dateUtils();

  PaletteGrid paletteGrid();

  ImageUtils imageUtils();

  @Theme Preference<Integer> theme();

  @InvisibleMode Preference<Boolean> invisibleMode();

  @ImmersiveCallState Preference<Boolean> immersiveCallState();

  @AddressBook Preference<Boolean> addressBook();

  RxFacebook rxFacebook();

  RxImagePicker rxImagePicker();

  SharedPreferences sharedPreferences();

  @Named("persistentPreferences") SharedPreferences persistentSharedPreferences();

  BadgeRealm badgeRealm();

  FileUtils fileUtils();

  TagManager tagManager();

  TribeAudioManager tribeAudioManager();

  NotificationBuilder notificationBuilder();

  NotificationManagerCompat notificationManagerCompat();

  @LastSync Preference<Long> lastSync();

  @LastSyncGameData Preference<Long> lastSyncGameData();

  @LastImOnline Preference<Long> lastImOnline();

  @NewContactsTooltip Preference<Boolean> newContactsTooltip();

  @LastVersionCode Preference<Integer> lastVersionCode();

  @TribeState Preference<Set<String>> tribeState();

  @DebugMode Preference<Boolean> debugMode();

  @Walkthrough Preference<Boolean> walkthrough();

  @UISounds Preference<Boolean> uiSounds();

  @RoutingMode Preference<String> routingMode();

  @FullscreenNotifications Preference<Boolean> fullscreenNotifications();

  @IsGroupCreated Preference<Boolean> isGroupCreated();

  @NumberOfCalls Preference<Integer> numberOfCalls();

  @CounterOfCallsForGrpButton Preference<Integer> counterOfCallsForGrpButton();

  @MinutesOfCalls Preference<Float> minutesOfCalls();

  @SupportRequestId Preference<String> supportRequestId();

  @SupportUserId Preference<String> supportUserId();

  @SupportIsUsed Preference<Set<String>> supportIsUsed();

  @MissedPlayloadNotification Preference<String> missedPlayloadNotification();

  @FullscreenNotificationState Preference<Set<String>> fullscreenNotificationState();

  @ChatShortcutData Preference<String> chatShortcutData();

  @CallTagsMap Preference<String> callTagsMap();

  @LookupResult Preference<String> lookupResult();

  @UserPhoneNumber Preference<String> userPhoneNumber();

  @GameData Preference<String> gameData();

  SoundManager soundManager();

  StateManager stateManager();

  MissedCallManager missedCallManager();
}