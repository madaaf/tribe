package com.tribe.app.presentation.internal.di.components;

import com.tribe.app.presentation.internal.di.PerActivity;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.internal.di.modules.TribeModule;
import com.tribe.app.presentation.view.activity.TribeActivity;

import dagger.Component;

/**
 * A scope {@link PerActivity} component.
 * Injects user specific Fragments / Activities.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = { ActivityModule.class, TribeModule.class })
public interface TribeComponent extends ActivityComponent {
    void inject(TribeActivity tribeActivity);
}
