package com.tribe.app.presentation.internal.di.components;

import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.internal.di.modules.UserModule;
import com.tribe.app.presentation.internal.di.scope.PerActivity;
import com.tribe.app.presentation.mvp.presenter.GroupPresenter;
import com.tribe.app.presentation.mvp.presenter.SearchPresenter;
import com.tribe.app.presentation.mvp.presenter.SettingsPresenter;
import com.tribe.app.presentation.utils.facebook.FacebookHiddenActivity;
import com.tribe.app.presentation.utils.mediapicker.MediaHiddenActivity;
import com.tribe.app.presentation.view.activity.AuthAccessActivity;
import com.tribe.app.presentation.view.activity.AuthActivity;
import com.tribe.app.presentation.view.activity.AuthProfileActivity;
import com.tribe.app.presentation.view.activity.CountryActivity;
import com.tribe.app.presentation.view.activity.DebugActivity;
import com.tribe.app.presentation.view.activity.GroupActivity;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.activity.LiveImmersiveNotificationActivity;
import com.tribe.app.presentation.view.activity.MissedCallDetailActivity;
import com.tribe.app.presentation.view.activity.ProfileActivity;
import com.tribe.app.presentation.view.activity.VideoActivity;
import com.tribe.app.presentation.view.component.ProfileInfoView;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.component.common.LoadFriendsView;
import com.tribe.app.presentation.view.component.home.SearchView;
import com.tribe.app.presentation.view.component.live.BuzzView;
import com.tribe.app.presentation.view.component.live.ChasingDotsView;
import com.tribe.app.presentation.view.component.live.LiveControlsView;
import com.tribe.app.presentation.view.component.live.LiveLocalView;
import com.tribe.app.presentation.view.component.live.LiveLowConnectionView;
import com.tribe.app.presentation.view.component.live.LivePeerOverlayView;
import com.tribe.app.presentation.view.component.live.LiveRoomView;
import com.tribe.app.presentation.view.component.live.LiveShareOverlayView;
import com.tribe.app.presentation.view.component.live.LiveStatusNameView;
import com.tribe.app.presentation.view.component.live.LiveWaitingAvatarView;
import com.tribe.app.presentation.view.component.live.LiveWaitingView;
import com.tribe.app.presentation.view.component.live.LiveWaveView;
import com.tribe.app.presentation.view.component.onboarding.AccessView;
import com.tribe.app.presentation.view.component.onboarding.CodeView;
import com.tribe.app.presentation.view.component.onboarding.PhoneNumberView;
import com.tribe.app.presentation.view.component.onboarding.StatusView;
import com.tribe.app.presentation.view.component.profile.ProfileView;
import com.tribe.app.presentation.view.component.settings.SettingsBlockedFriendsView;
import com.tribe.app.presentation.view.component.settings.SettingsManageFriendshipsView;
import com.tribe.app.presentation.view.component.settings.SettingsProfileView;
import com.tribe.app.presentation.view.dialog_fragment.AuthenticationDialogFragment;
import com.tribe.app.presentation.view.dialog_fragment.SurpriseDialogFragment;
import com.tribe.app.presentation.view.widget.PopupContainerView;
import com.tribe.app.presentation.view.widget.notifications.CreateGroupNotificationView;
import com.tribe.app.presentation.view.widget.notifications.EnjoyingTribeNotificationView;
import com.tribe.app.presentation.view.widget.notifications.ErrorNotificationView;
import com.tribe.app.presentation.view.widget.notifications.LifeNotification;
import com.tribe.app.presentation.view.widget.notifications.NotificationContainerView;
import com.tribe.app.presentation.view.widget.notifications.PermissionNotificationView;
import com.tribe.app.presentation.view.widget.notifications.RatingNotificationView;
import dagger.Component;

/**
 * A scope {@link PerActivity} component.
 * Injects user specific Fragments / Activities.
 */
@PerActivity @Component(dependencies = ApplicationComponent.class, modules = {
    ActivityModule.class, UserModule.class
}) public interface UserComponent extends ActivityComponent {

  void inject(DebugActivity debugActivity);

  void inject(LifeNotification lifeNotification);

  void inject(MissedCallDetailActivity missedCallDetailActivity);

  void inject(PopupContainerView popupContainerView);

  void inject(LiveImmersiveNotificationActivity liveImmersiveNotificationActivity);

  void inject(PermissionNotificationView permissionNotificationView);

  void inject(NotificationContainerView notificationContainerView);

  void inject(CreateGroupNotificationView createGroupNotificationView);

  void inject(FacebookHiddenActivity facebookHiddenActivity);

  void inject(CountryActivity countryActivity);

  void inject(HomeActivity homeActivity);

  void inject(VideoActivity videoActivity);

  void inject(ProfileInfoView profileInfoView);

  void inject(GroupPresenter groupPresenter);

  void inject(MediaHiddenActivity mediaHiddenActivity);

  void inject(AuthenticationDialogFragment authenticationDialogFragment);

  void inject(SurpriseDialogFragment surpriseDialogFragment);

  void inject(GroupActivity groupActivity);

  void inject(SearchPresenter searchPresenter);

  void inject(SettingsPresenter settingsPresenter);

  void inject(SettingsProfileView settingsProfileView);

  void inject(ProfileActivity profileActivity);

  void inject(ProfileView profileView);

  void inject(AuthActivity authActivity);

  void inject(PhoneNumberView phoneNumberView);

  void inject(StatusView statusView);

  void inject(CodeView codeView);

  void inject(AuthProfileActivity authProfileActivity);

  void inject(AuthAccessActivity authAccessActivity);

  void inject(AccessView accessView);

  void inject(LoadFriendsView loadFriendsView);

  void inject(TileView tileView);

  void inject(LiveActivity liveActivity);

  void inject(SearchView searchView);

  void inject(RatingNotificationView ratingNotificationView);

  void inject(EnjoyingTribeNotificationView enjoyingTribeNotificationView);

  void inject(LiveLocalView liveLocalView);

  void inject(LiveRoomView liveRoomView);

  void inject(LiveWaitingView liveWaitingView);

  void inject(ChasingDotsView threeDotsView);

  void inject(BuzzView buzzView);

  void inject(LiveWaitingAvatarView avatarView);

  void inject(LiveControlsView liveControlsView);

  void inject(LiveStatusNameView liveStatusNameView);

  void inject(SettingsBlockedFriendsView settingsBlockedFriendsView);

  void inject(LiveLowConnectionView liveLowConnectionView);

  void inject(LivePeerOverlayView livePeerOverlayView);

  void inject(LiveWaveView liveWaveView);

  void inject(LiveShareOverlayView liveShareOverlayView);

  void inject(ErrorNotificationView errorNotificationView);

  void inject(SettingsManageFriendshipsView settingsManageFriendshipsView);
}
