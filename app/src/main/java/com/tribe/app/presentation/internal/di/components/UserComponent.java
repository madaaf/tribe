package com.tribe.app.presentation.internal.di.components;

import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.internal.di.modules.UserModule;
import com.tribe.app.presentation.internal.di.scope.PerActivity;
import com.tribe.app.presentation.mvp.presenter.MessagePresenter;
import com.tribe.app.presentation.mvp.presenter.SearchPresenter;
import com.tribe.app.presentation.mvp.presenter.SettingsPresenter;
import com.tribe.app.presentation.utils.facebook.FacebookHiddenActivity;
import com.tribe.app.presentation.utils.mediapicker.MediaHiddenActivity;
import com.tribe.app.presentation.view.activity.AuthActivity;
import com.tribe.app.presentation.view.activity.AuthProfileActivity;
import com.tribe.app.presentation.view.activity.CountryActivity;
import com.tribe.app.presentation.view.activity.DebugActivity;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.activity.LiveImmersiveNotificationActivity;
import com.tribe.app.presentation.view.activity.MissedCallDetailActivity;
import com.tribe.app.presentation.view.activity.NewChatActivity;
import com.tribe.app.presentation.view.activity.ProfileActivity;
import com.tribe.app.presentation.view.activity.VideoActivity;
import com.tribe.app.presentation.view.component.ProfileInfoView;
import com.tribe.app.presentation.view.component.chat.ShortcutCompletionTokenView;
import com.tribe.app.presentation.view.component.common.LoadFriendsView;
import com.tribe.app.presentation.view.component.home.SearchView;
import com.tribe.app.presentation.view.component.live.BuzzView;
import com.tribe.app.presentation.view.component.live.ChasingDotsView;
import com.tribe.app.presentation.view.component.live.LiveChatButton;
import com.tribe.app.presentation.view.component.live.LiveControlsView;
import com.tribe.app.presentation.view.component.live.LiveLocalView;
import com.tribe.app.presentation.view.component.live.LiveLowConnectionView;
import com.tribe.app.presentation.view.component.live.LivePeerOverlayView;
import com.tribe.app.presentation.view.component.live.LiveRingingView;
import com.tribe.app.presentation.view.component.live.LiveRoomView;
import com.tribe.app.presentation.view.component.live.LiveStatusNameView;
import com.tribe.app.presentation.view.component.live.LiveWaveView;
import com.tribe.app.presentation.view.component.live.ScreenshotView;
import com.tribe.app.presentation.view.component.live.TileInviteView;
import com.tribe.app.presentation.view.component.live.game.AliensAttack.GameAliensAttackAlienView;
import com.tribe.app.presentation.view.component.live.game.AliensAttack.GameAliensAttackBackground;
import com.tribe.app.presentation.view.component.live.game.common.GameEngine;
import com.tribe.app.presentation.view.component.onboarding.AccessView;
import com.tribe.app.presentation.view.component.onboarding.CodeView;
import com.tribe.app.presentation.view.component.onboarding.PhoneNumberView;
import com.tribe.app.presentation.view.component.onboarding.StatusView;
import com.tribe.app.presentation.view.component.profile.ProfileView;
import com.tribe.app.presentation.view.component.settings.SettingsBlockedFriendsView;
import com.tribe.app.presentation.view.component.settings.SettingsFacebookAccountView;
import com.tribe.app.presentation.view.component.settings.SettingsManageShortcutsView;
import com.tribe.app.presentation.view.component.settings.SettingsPhoneNumberView;
import com.tribe.app.presentation.view.component.settings.SettingsProfileView;
import com.tribe.app.presentation.view.dialog_fragment.AuthenticationDialogFragment;
import com.tribe.app.presentation.view.dialog_fragment.SurpriseDialogFragment;
import com.tribe.app.presentation.view.widget.AvatarsSuperposedLayout;
import com.tribe.app.presentation.view.widget.PopupContainerView;
import com.tribe.app.presentation.view.widget.chat.ChatActivity;
import com.tribe.app.presentation.view.widget.chat.ChatView;
import com.tribe.app.presentation.view.widget.chat.PictureActivity;
import com.tribe.app.presentation.view.widget.chat.RecyclerMessageView;
import com.tribe.app.presentation.view.component.live.game.GameChallengesView;
import com.tribe.app.presentation.view.component.live.game.GameDrawView;
import com.tribe.app.presentation.view.component.live.game.GameManagerView;
import com.tribe.app.presentation.view.component.live.game.common.GameView;
import com.tribe.app.presentation.view.widget.notifications.EnjoyingTribeNotificationView;
import com.tribe.app.presentation.view.widget.notifications.ErrorNotificationView;
import com.tribe.app.presentation.view.widget.notifications.LifeNotification;
import com.tribe.app.presentation.view.widget.notifications.NotificationContainerView;
import com.tribe.app.presentation.view.widget.notifications.PermissionNotificationView;
import com.tribe.app.presentation.view.widget.notifications.RatingNotificationView;
import com.tribe.app.presentation.view.widget.notifications.UserInfosNotificationView;
import dagger.Component;

/**
 * A scope {@link PerActivity} component.
 * Injects user specific Fragments / Activities.
 */
@PerActivity @Component(dependencies = ApplicationComponent.class, modules = {
    ActivityModule.class, UserModule.class
}) public interface UserComponent extends ActivityComponent {

  void inject(DebugActivity debugActivity);

  void inject(ChatActivity chatActivity);

  void inject(PictureActivity pictureActivity);

  void inject(ChatView chatView);

  void inject(RecyclerMessageView chatView);

  void inject(LifeNotification lifeNotification);

  void inject(MissedCallDetailActivity missedCallDetailActivity);

  void inject(PopupContainerView popupContainerView);

  void inject(LiveImmersiveNotificationActivity liveImmersiveNotificationActivity);

  void inject(PermissionNotificationView permissionNotificationView);

  void inject(NotificationContainerView notificationContainerView);

  void inject(ScreenshotView screenshotView);

  void inject(FacebookHiddenActivity facebookHiddenActivity);

  void inject(CountryActivity countryActivity);

  void inject(HomeActivity homeActivity);

  void inject(VideoActivity videoActivity);

  void inject(ProfileInfoView profileInfoView);

  void inject(MediaHiddenActivity mediaHiddenActivity);

  void inject(AuthenticationDialogFragment authenticationDialogFragment);

  void inject(SurpriseDialogFragment surpriseDialogFragment);

  void inject(SearchPresenter searchPresenter);

  void inject(SettingsPresenter settingsPresenter);

  void inject(MessagePresenter messagePresenter);

  void inject(SettingsProfileView settingsProfileView);

  void inject(SettingsPhoneNumberView settingsPhoneNumberView);

  void inject(SettingsFacebookAccountView settingsFacebookAccountView);

  void inject(ProfileActivity profileActivity);

  void inject(ProfileView profileView);

  void inject(AuthActivity authActivity);

  void inject(PhoneNumberView phoneNumberView);

  void inject(StatusView statusView);

  void inject(CodeView codeView);

  void inject(AuthProfileActivity authProfileActivity);

  void inject(AccessView accessView);

  void inject(LoadFriendsView loadFriendsView);

  void inject(LiveActivity liveActivity);

  void inject(SearchView searchView);

  void inject(RatingNotificationView ratingNotificationView);

  void inject(EnjoyingTribeNotificationView enjoyingTribeNotificationView);

  void inject(LiveLocalView liveLocalView);

  void inject(LiveRoomView liveRoomView);

  void inject(AvatarsSuperposedLayout avatarsSuperposedLayout);

  void inject(ChasingDotsView threeDotsView);

  void inject(BuzzView buzzView);

  void inject(LiveControlsView liveControlsView);

  void inject(LiveStatusNameView liveStatusNameView);

  void inject(SettingsBlockedFriendsView settingsBlockedFriendsView);

  void inject(LiveLowConnectionView liveLowConnectionView);

  void inject(LivePeerOverlayView livePeerOverlayView);

  void inject(LiveWaveView liveWaveView);

  void inject(ErrorNotificationView errorNotificationView);

  void inject(SettingsManageShortcutsView settingsManageFriendshipsView);

  void inject(UserInfosNotificationView userInfosNotificationView);

  void inject(LiveChatButton liveChatButton);

  void inject(LiveRingingView liveRingingView);

  void inject(NewChatActivity newChatActivity);

  void inject(ShortcutCompletionTokenView shortcutCompletionTokenView);

  void inject(TileInviteView tileInviteView);

  /**
   * GAMES
   */

  void inject(GameManagerView gameManagerView);

  void inject(GameView gameView);

  void inject(GameChallengesView gameChallengesView);

  void inject(GameDrawView gameDrawView);

  void inject(GameAliensAttackBackground gameAliensAttackBackground);

  void inject(GameAliensAttackAlienView gameAliensAttackAlienView);

  void inject(GameEngine gameEngine);
}
