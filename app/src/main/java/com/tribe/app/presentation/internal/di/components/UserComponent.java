package com.tribe.app.presentation.internal.di.components;

import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.internal.di.modules.UserModule;
import com.tribe.app.presentation.internal.di.scope.PerActivity;
import com.tribe.app.presentation.mvp.presenter.GroupPresenter;
import com.tribe.app.presentation.mvp.presenter.SearchPresenter;
import com.tribe.app.presentation.mvp.presenter.SettingsPresenter;
import com.tribe.app.presentation.utils.facebook.FacebookHiddenActivity;
import com.tribe.app.presentation.utils.mediapicker.MediaHiddenActivity;
import com.tribe.app.presentation.view.activity.BaseActionActivity;
import com.tribe.app.presentation.view.activity.CountryActivity;
import com.tribe.app.presentation.view.activity.DebugActivity;
import com.tribe.app.presentation.view.activity.GroupActivity;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.IntroActivity;
import com.tribe.app.presentation.view.activity.PointsActivity;
import com.tribe.app.presentation.view.activity.ScoreActivity;
import com.tribe.app.presentation.view.activity.SearchUserActivity;
import com.tribe.app.presentation.view.activity.SettingActivity;
import com.tribe.app.presentation.view.activity.SettingsActivity;
import com.tribe.app.presentation.view.component.ProfileInfoView;
import com.tribe.app.presentation.view.component.SettingItemView;
import com.tribe.app.presentation.view.component.settings.SettingsBlockedHiddenView;
import com.tribe.app.presentation.view.component.settings.SettingsFilterThemeView;
import com.tribe.app.presentation.view.component.settings.SettingsFilterView;
import com.tribe.app.presentation.view.component.settings.SettingsParametersView;
import com.tribe.app.presentation.view.component.settings.SettingsProfileView;
import com.tribe.app.presentation.view.component.settings.SettingsThemeView;
import com.tribe.app.presentation.view.component.settings.SettingsView;
import com.tribe.app.presentation.view.dialog_fragment.PointsDialogFragment;
import com.tribe.app.presentation.view.dialog_fragment.ShareDialogProfileFragment;
import com.tribe.app.presentation.view.fragment.AccessFragment;
import com.tribe.app.presentation.view.fragment.IntroViewFragment;
import com.tribe.app.presentation.view.fragment.ProfileInfoFragment;
import com.tribe.app.presentation.view.fragment.SettingBlockFragment;
import com.tribe.app.presentation.view.fragment.SettingFragment;

import dagger.Component;

/**
 * A scope {@link PerActivity} component.
 * Injects user specific Fragments / Activities.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = {ActivityModule.class, UserModule.class})
public interface UserComponent extends ActivityComponent {
    void inject(DebugActivity debugActivity);
    void inject(IntroActivity introActivity);
    void inject(FacebookHiddenActivity facebookHiddenActivity);
    void inject(CountryActivity countryActivity);
    void inject(HomeActivity homeActivity);
    void inject(ScoreActivity scoreActivity);
    void inject(PointsActivity pointsActivity);
    void inject(IntroViewFragment introViewFragment);
    void inject(AccessFragment accessFragment);
    void inject(ProfileInfoFragment profileInfoFragment);
    void inject(ProfileInfoView profileInfoView);
    void inject(GroupPresenter groupPresenter);
    void inject(MediaHiddenActivity mediaHiddenActivity);
    void inject(PointsDialogFragment pointsDialogFragment);
    void inject(ShareDialogProfileFragment shareDialogProfileFragment);
    void inject(BaseActionActivity baseActionActivity);
    void inject(GroupActivity groupActivity);
    void inject(SearchUserActivity searchUserActivity);
    void inject(SearchPresenter searchPresenter);

    void inject(SettingActivity settingActivity);
    void inject(SettingItemView settingItemView);
    void inject(SettingFragment settingFragment);
    void inject(SettingsPresenter settingsPresenter);
    void inject(SettingsActivity settingsActivity);
    void inject(SettingsThemeView settingsThemeView);
    void inject(SettingBlockFragment settingBlockFragment);
    void inject(SettingsFilterView settingsFilterView);
    void inject(SettingsView settingsView);
    void inject(SettingsProfileView settingsProfileView);
    void inject(SettingsFilterThemeView settingsFilterThemeView);
    void inject(SettingsParametersView settingsParametersView);
    void inject(SettingsBlockedHiddenView settingsBlockedHiddenView);
}
