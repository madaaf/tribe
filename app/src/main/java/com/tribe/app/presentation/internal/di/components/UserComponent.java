package com.tribe.app.presentation.internal.di.components;

import com.tribe.app.presentation.internal.di.scope.PerActivity;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.internal.di.modules.UserModule;
import com.tribe.app.presentation.view.activity.CountryActivity;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.IntroActivity;
import com.tribe.app.presentation.view.fragment.AccessFragment;
import com.tribe.app.presentation.view.fragment.HomeGridFragment;
import com.tribe.app.presentation.view.fragment.IntroViewFragment;

import dagger.Component;

/**
 * A scope {@link PerActivity} component.
 * Injects user specific Fragments / Activities.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = {ActivityModule.class, UserModule.class})
public interface UserComponent extends ActivityComponent {
    void inject(IntroActivity introActivity);
    void inject(CountryActivity countryActivity);
    void inject(HomeGridFragment homeGridFragment);
    void inject(HomeActivity homeActivity);
    void inject(IntroViewFragment introViewFragment);
    void inject(AccessFragment accessFragment);
}
