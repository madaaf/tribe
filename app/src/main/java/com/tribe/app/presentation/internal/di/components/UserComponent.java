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
import com.tribe.app.presentation.view.activity.BaseActionActivity;
import com.tribe.app.presentation.view.activity.CountryActivity;
import com.tribe.app.presentation.view.activity.DebugActivity;
import com.tribe.app.presentation.view.activity.GroupActivity;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.PickYourFriendsActivity;
import com.tribe.app.presentation.view.activity.PointsActivity;
import com.tribe.app.presentation.view.activity.ScoreActivity;
import com.tribe.app.presentation.view.activity.SearchUserActivity;
import com.tribe.app.presentation.view.activity.SettingsActivity;
import com.tribe.app.presentation.view.component.ProfileInfoView;
import com.tribe.app.presentation.view.component.common.LoadFriendsView;
import com.tribe.app.presentation.view.component.common.PickAllView;
import com.tribe.app.presentation.view.component.onboarding.AccessView;
import com.tribe.app.presentation.view.component.onboarding.CodeView;
import com.tribe.app.presentation.view.component.onboarding.PhoneNumberView;
import com.tribe.app.presentation.view.component.onboarding.StatusView;
import com.tribe.app.presentation.view.component.settings.SettingsBlockedHiddenView;
import com.tribe.app.presentation.view.component.settings.SettingsFilterThemeView;
import com.tribe.app.presentation.view.component.settings.SettingsFilterView;
import com.tribe.app.presentation.view.component.settings.SettingsParametersView;
import com.tribe.app.presentation.view.component.settings.SettingsProfileView;
import com.tribe.app.presentation.view.component.settings.SettingsThemeView;
import com.tribe.app.presentation.view.component.settings.SettingsView;
import com.tribe.app.presentation.view.dialog_fragment.AuthenticationDialogFragment;
import com.tribe.app.presentation.view.dialog_fragment.PointsDialogFragment;
import com.tribe.app.presentation.view.dialog_fragment.ShareDialogProfileFragment;

import dagger.Component;

/**
 * A scope {@link PerActivity} component.
 * Injects user specific Fragments / Activities.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = {ActivityModule.class, UserModule.class})
public interface UserComponent extends ActivityComponent {

    void inject(DebugActivity debugActivity);
    void inject(FacebookHiddenActivity facebookHiddenActivity);
    void inject(CountryActivity countryActivity);
    void inject(HomeActivity homeActivity);
    void inject(ScoreActivity scoreActivity);
    void inject(PointsActivity pointsActivity);
    void inject(ProfileInfoView profileInfoView);
    void inject(GroupPresenter groupPresenter);
    void inject(MediaHiddenActivity mediaHiddenActivity);
    void inject(PointsDialogFragment pointsDialogFragment);
    void inject(AuthenticationDialogFragment authenticationDialogFragment);
    void inject(ShareDialogProfileFragment shareDialogProfileFragment);
    void inject(BaseActionActivity baseActionActivity);
    void inject(GroupActivity groupActivity);
    void inject(SearchUserActivity searchUserActivity);
    void inject(SearchPresenter searchPresenter);

    void inject(SettingsActivity settingsActivity);
    void inject(SettingsPresenter settingsPresenter);
    void inject(SettingsThemeView settingsThemeView);
    void inject(SettingsFilterView settingsFilterView);
    void inject(SettingsView settingsView);
    void inject(SettingsProfileView settingsProfileView);
    void inject(SettingsFilterThemeView settingsFilterThemeView);
    void inject(SettingsParametersView settingsParametersView);
    void inject(SettingsBlockedHiddenView settingsBlockedHiddenView);

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
}
