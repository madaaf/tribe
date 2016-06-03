package com.tribe.app.presentation.internal.di.components;

import com.tribe.app.presentation.internal.di.PerActivity;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.internal.di.modules.UserModule;
import com.tribe.app.presentation.view.activity.IntroActivity;
import com.tribe.app.presentation.view.fragment.HomeGridFragment;

import dagger.Component;

/**
 * A scope {@link com.tribe.app.presentation.internal.di.PerActivity} component.
 * Injects user specific Fragments / Activities.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = {ActivityModule.class, UserModule.class})
public interface UserComponent extends ActivityComponent {
    void inject(IntroActivity introActivity);
    void inject(HomeGridFragment homeGridFragment);
}
