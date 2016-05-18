package com.tribe.app.presentation.internal.di.modules;

import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.marvel.GetCloudMarvelCharacterList;
import com.tribe.app.domain.interactor.marvel.GetDiskMarvelCharacterList;
import com.tribe.app.presentation.internal.di.PerActivity;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module that provides marvel related collaborators.
 */
@Module
public class MarvelCharactersModule {

    public MarvelCharactersModule() {
    }

    @Provides
    @PerActivity
    @Named("cloudMarvelCharactersList")
    UseCase provideGetCloudMarvelCharactersListUseCase(GetCloudMarvelCharacterList getMarvelCharacterList) {
        return getMarvelCharacterList;
    }

    @Provides
    @PerActivity
    @Named("diskMarvelCharactersList")
    UseCase provideGetDiskMarvelCharactersListUseCase(GetDiskMarvelCharacterList getMarvelCharacterList) {
        return getMarvelCharacterList;
    }
}