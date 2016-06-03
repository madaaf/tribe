package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Friendship;

import rx.Observable;

public interface HomeView extends View {

    void initializeClicksOnChat(Observable<Friendship> observable);
    void initializeScrollOnGrid(Observable<Integer> observable);
}
