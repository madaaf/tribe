package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.MarvelCharacter;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.marvel.GetCloudMarvelCharacterList;
import com.tribe.app.domain.interactor.marvel.GetDiskMarvelCharacterList;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.CharacterListView;
import com.tribe.app.presentation.mvp.view.HomeGridView;
import com.tribe.app.presentation.mvp.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class HomeGridPresenter implements Presenter {

    private final UseCase cloudCharactersUsecase;
    private final UseCase diskCharactersUsecase;
    private boolean isTheFriendRequestRunning;

    private HomeGridView homeGridView;

    @Inject
    public HomeGridPresenter(@Named("cloudMarvelCharactersList") UseCase cloudCharactersUsecase,
                             @Named("diskMarvelCharactersList") UseCase mDiskCharactersUsecase) {
        this.cloudCharactersUsecase = cloudCharactersUsecase;
        this.diskCharactersUsecase = mDiskCharactersUsecase;
    }

    @Override
    public void onCreate() {
        loadFriendList();
    }

    @Override
    public void onStart() {
        // Unused
    }

    @Override
    public void onResume() {
        // Unused
    }

    @Override
    public void onStop() {
        // Unused
    }

    @Override
    public void onPause() {
        isTheFriendRequestRunning = false;
    }

    @Override
    public void onDestroy() {
        diskCharactersUsecase.unsubscribe();
        cloudCharactersUsecase.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        homeGridView = (HomeGridView) v;
    }

    public void loadFriendList() {
        isTheFriendRequestRunning = true;
        showViewLoading();
        diskCharactersUsecase.execute(new FriendListSubscriber());
        cloudCharactersUsecase.execute(new FriendListSubscriber());
    }

    public void onTextClicked(MarvelCharacter friend) {
        homeGridView.onTextClicked(friend);
    }

    private void showFriendCollectionInView(List<MarvelCharacter> friendList) {
        this.homeGridView.renderFriendList(friendList);
    }

    private void showViewLoading() {
        this.homeGridView.showLoading();
    }

    private void hideViewLoading() {
        this.homeGridView.hideLoading();
    }

    private void showErrorMessage(ErrorBundle errorBundle) {
        String errorMessage = ErrorMessageFactory.create(this.homeGridView.context(),
                errorBundle.getException());
        this.homeGridView.showError(errorMessage);
    }

    private final class FriendListSubscriber extends DefaultSubscriber<List<MarvelCharacter>> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            hideViewLoading();
            showErrorMessage(new DefaultErrorBundle((Exception) e));
        }

        @Override
        public void onNext(List<MarvelCharacter> friendList) {
            showFriendCollectionInView(friendList);
        }
    }
}
