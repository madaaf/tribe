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
import com.tribe.app.presentation.view.activity.LiveTestActivity;
import com.tribe.app.presentation.view.activity.PickYourFriendsActivity;
import com.tribe.app.presentation.view.activity.ProfileActivity;
import com.tribe.app.presentation.view.component.ProfileInfoView;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.component.common.LoadFriendsView;
import com.tribe.app.presentation.view.component.common.PickAllView;
import com.tribe.app.presentation.view.component.home.SearchView;
import com.tribe.app.presentation.view.component.live.BuzzView;
import com.tribe.app.presentation.view.component.live.LiveAudioView;
import com.tribe.app.presentation.view.component.live.LiveLocalView;
import com.tribe.app.presentation.view.component.live.LiveWaitingView;
import com.tribe.app.presentation.view.component.live.ThreeDotsView;
import com.tribe.app.presentation.view.component.onboarding.AccessView;
import com.tribe.app.presentation.view.component.onboarding.CodeView;
import com.tribe.app.presentation.view.component.onboarding.PhoneNumberView;
import com.tribe.app.presentation.view.component.onboarding.StatusView;
import com.tribe.app.presentation.view.component.profile.ProfileView;
import com.tribe.app.presentation.view.component.settings.SettingsProfileView;
import com.tribe.app.presentation.view.dialog_fragment.AuthenticationDialogFragment;
import com.tribe.app.presentation.view.dialog_fragment.ShareDialogProfileFragment;
import dagger.Component;

/**
 * A scope {@link PerActivity} component.
 * Injects user specific Fragments / Activities.
 */
@PerActivity @Component(dependencies = ApplicationComponent.class, modules = {
    ActivityModule.class, UserModule.class
}) public interface UserComponent extends ActivityComponent {

  void inject(DebugActivity debugActivity);

  void inject(FacebookHiddenActivity facebookHiddenActivity);

  void inject(CountryActivity countryActivity);

  void inject(HomeActivity homeActivity);

  void inject(ProfileInfoView profileInfoView);

  void inject(GroupPresenter groupPresenter);

  void inject(MediaHiddenActivity mediaHiddenActivity);

  void inject(AuthenticationDialogFragment authenticationDialogFragment);

  void inject(ShareDialogProfileFragment shareDialogProfileFragment);

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

  void inject(PickYourFriendsActivity pickYourFriendsActivity);

  void inject(LoadFriendsView loadFriendsView);

  void inject(PickAllView pickAllView);

  void inject(TileView tileView);

  void inject(LiveActivity liveActivity);

  void inject(LiveTestActivity liveTestActivity);

  void inject(SearchView searchView);

  void inject(LiveLocalView liveLocalView);

  void inject(LiveAudioView liveAudioView);

  void inject(LiveWaitingView liveWaitingView);

  void inject(ThreeDotsView threeDotsView);

  void inject(BuzzView buzzView);
}
