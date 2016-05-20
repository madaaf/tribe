package com.tribe.app.presentation.internal.di.modules;

import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.friendship.UseCaseGetFriendshipList;
import com.tribe.app.domain.interactor.marvel.GetCloudMarvelCharacterList;
import com.tribe.app.domain.interactor.marvel.GetDiskMarvelCharacterList;
import com.tribe.app.presentation.internal.di.PerActivity;

import dagger.Module;
import dagger.Provides;

import javax.inject.Named;

/**
 * Dagger module that provides friendship related collaborators.
 */
@Module
public class FriendshipModule {

    private int userId = -1;

    public FriendshipModule() {
    }

    public FriendshipModule(int userId) {
        this.userId = userId;
    }

    @Provides
    @PerActivity
    @Named("friendshipList")
    UseCase provideGetFriendshipListUseCase(UseCaseGetFriendshipList getFriendshipList) {
        return getFriendshipList;
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