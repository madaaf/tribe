package com.tribe.app.presentation.internal.di.components;

import com.tribe.app.presentation.internal.di.scope.PerActivity;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.internal.di.modules.ChatModule;
import com.tribe.app.presentation.view.activity.ChatActivity;

import dagger.Component;

/**
 * A scope {@link PerActivity} component.
 * Injects user specific Fragments / Activities.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = {ActivityModule.class, ChatModule.class})
public interface ChatComponent extends ActivityComponent {
    void inject(ChatActivity textActivity);
}
