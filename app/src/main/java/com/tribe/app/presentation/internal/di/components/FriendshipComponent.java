package com.tribe.app.presentation.internal.di.components;

import com.tribe.app.presentation.internal.di.PerActivity;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.internal.di.modules.FriendshipModule;
import com.tribe.app.presentation.view.fragment.HomeGridFragment;

import dagger.Component;

/**
 * A scope {@link PerActivity} component.
 * Injects user specific Fragments / Activities.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = {ActivityModule.class, FriendshipModule.class})
public interface FriendshipComponent extends ActivityComponent {
    void inject(HomeGridFragment homeGridFragment);
    //void inject(UserDetailsFragment userDetailsFragment);
}
