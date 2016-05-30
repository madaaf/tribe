package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.MarvelCharacter;

import rx.Observable;

public interface HomeView extends View {

    void initializeClicksOnChat(Observable<MarvelCharacter> observable);
    void initializeScrollOnGrid(Observable<Integer> observable);
}
