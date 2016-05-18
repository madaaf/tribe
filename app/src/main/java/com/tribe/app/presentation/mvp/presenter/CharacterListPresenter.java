package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.MarvelCharacter;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.marvel.GetCloudMarvelCharacterList;
import com.tribe.app.domain.interactor.marvel.GetDiskMarvelCharacterList;
import com.tribe.app.presentation.mvp.view.CharacterListView;
import com.tribe.app.presentation.mvp.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class CharacterListPresenter implements Presenter {

    private final UseCase cloudCharactersUsecase;
    private final UseCase diskCharactersUsecase;
    private boolean mIsTheCharacterRequestRunning;

    private List<MarvelCharacter> mCharacters = new ArrayList<>();
    private CharacterListView mAvengersView;

    @Inject
    public CharacterListPresenter(@Named("cloudMarvelCharactersList") UseCase cloudCharactersUsecase,
                                  @Named("diskMarvelCharactersList") UseCase mDiskCharactersUsecase) {
        this.cloudCharactersUsecase = cloudCharactersUsecase;
        this.diskCharactersUsecase = mDiskCharactersUsecase;
    }

    @Override
    public void onCreate() {
        askForCharacters();
    }


    @Override
    public void onStart() {
        // Unused
    }

    @Override
    public void onStop() {
        // Unused
    }

    @Override
    public void onPause() {
        mAvengersView.hideLoadingMoreCharactersIndicator();
        diskCharactersUsecase.unsubscribe();
        mIsTheCharacterRequestRunning = false;
    }

    @Override
    public void attachView(View v) {
        mAvengersView = (CharacterListView) v;
    }

    public void askForCharacters() {
        mIsTheCharacterRequestRunning = true;
        mAvengersView.hideErrorView();
        mAvengersView.showLoadingView();

        diskCharactersUsecase.execute(new MarvelCharacterListSubscriber());
        cloudCharactersUsecase.execute(new MarvelCharacterListSubscriber());
    }

    private final class MarvelCharacterListSubscriber extends DefaultSubscriber<List<MarvelCharacter>> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            showErrorView(e);
        }

        @Override
        public void onNext(List<MarvelCharacter> characters) {
            onCharactersReceived(characters);
        }
    }

    public void onCharactersReceived(List<MarvelCharacter> characters) {
        mCharacters.addAll(characters);
        mAvengersView.bindCharacterList(mCharacters);
        mAvengersView.showCharacterList();
        mAvengersView.hideEmptyIndicator();
        mIsTheCharacterRequestRunning = false;
    }

    public void showErrorView(Throwable error) {
        mAvengersView.showUknownErrorMessage();
        mAvengersView.hideLoadingMoreCharactersIndicator();
        mAvengersView.hideEmptyIndicator();
        mAvengersView.hideCharactersList();
    }

    public void showGenericError() {
        mAvengersView.hideLoadingIndicator();
        mAvengersView.showLightError();
    }

    public void onErrorRetryRequest() {
        if (mCharacters.isEmpty())
            askForCharacters();
    }

    public void onElementClick(int position) {
        int characterId = mCharacters.get(position).getId();
        String characterName = mCharacters.get(position).getName();
        mAvengersView.showDetailScreen(characterName, characterId);
    }
}
