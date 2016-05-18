package com.tribe.app.presentation.internal.di.components;

import android.content.Context;

import com.tribe.app.presentation.internal.di.PerActivity;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.internal.di.modules.MarvelCharactersModule;
import com.tribe.app.presentation.view.activity.CharacterListActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = {ActivityModule.class, MarvelCharactersModule.class})
public interface AvengersComponent extends ActivityComponent {
    void inject(CharacterListActivity activity);

    Context activityContext();
}
