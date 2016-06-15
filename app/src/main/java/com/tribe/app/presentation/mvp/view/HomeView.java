package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Friendship;

import rx.Observable;

public interface HomeView extends View {

    void initClicksOnChat(Observable<Friendship> observable);
    void initOnRecordStart(Observable<Friendship> observable);
    void initOnRecordEnd(Observable<Friendship> observable);
    void initScrollOnGrid(Observable<Integer> observable);
}
