package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.MarvelCharacter;

import java.util.List;

public interface CharacterListView extends View {

    void bindCharacterList(List<MarvelCharacter> avengers);

    void showCharacterList();

    void hideCharactersList();

    void showLoadingMoreCharactersIndicator();

    void hideLoadingMoreCharactersIndicator();

    void hideLoadingIndicator();

    void showLoadingView();

    void hideLoadingView();

    void showLightError();

    void hideErrorView();

    void showEmptyIndicator();

    void hideEmptyIndicator();

    void updateCharacterList(int charactersLimit);

    void showConnectionErrorMessage();

    void showServerErrorMessage();

    void showUknownErrorMessage();

    void showDetailScreen(String characterName, int characterId);
}
