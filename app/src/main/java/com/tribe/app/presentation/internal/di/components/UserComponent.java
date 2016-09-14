package com.tribe.app.presentation.internal.di.components;

import com.tribe.app.presentation.internal.di.scope.PerActivity;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.internal.di.modules.UserModule;
import com.tribe.app.presentation.mvp.presenter.SettingPresenter;
import com.tribe.app.presentation.utils.facebook.FacebookHiddenActivity;
import com.tribe.app.presentation.view.activity.CountryActivity;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.IntroActivity;
import com.tribe.app.presentation.view.activity.PointsActivity;
import com.tribe.app.presentation.view.activity.ScoreActivity;
import com.tribe.app.presentation.view.activity.SettingActivity;
import com.tribe.app.presentation.view.component.ProfileInfoView;
import com.tribe.app.presentation.view.component.SettingItemView;
import com.tribe.app.presentation.view.component.SettingThemeView;
import com.tribe.app.presentation.view.dialog_fragment.ShareDialogFragment;
import com.tribe.app.presentation.view.fragment.AccessFragment;
import com.tribe.app.presentation.view.fragment.ContactsGridFragment;
import com.tribe.app.presentation.view.fragment.GroupsGridFragment;
import com.tribe.app.presentation.view.fragment.HomeGridFragment;
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
    void inject(IntroActivity introActivity);
    void inject(FacebookHiddenActivity facebookHiddenActivity);
    void inject(CountryActivity countryActivity);
    void inject(HomeGridFragment homeGridFragment);
    void inject(ContactsGridFragment contactsGridFragment);
    void inject(HomeActivity homeActivity);
    void inject(ScoreActivity scoreActivity);
    void inject(PointsActivity pointsActivity);
    void inject(IntroViewFragment introViewFragment);
    void inject(AccessFragment accessFragment);
    void inject(ProfileInfoFragment profileInfoFragment);
    void inject(SettingActivity settingActivity);
    void inject(SettingItemView settingItemView);
    void inject(SettingFragment settingFragment);
    void inject(SettingPresenter settingPresenter);
    void inject(ProfileInfoView profileInfoView);
    void inject(SettingThemeView settingThemeView);
    void inject(SettingBlockFragment settingBlockFragment);
    void inject(GroupsGridFragment groupsGridFragment);
    void inject(ShareDialogFragment shareDialogFragment);
}
