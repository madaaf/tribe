package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Friendship;

import rx.Observable;

public interface HomeView extends View {

    void initOpenTribes(Observable<Friendship> observable);
    void initClicksOnChat(Observable<Friendship> observable);
    void initOnRecordStart(Observable<String> observable);
    void initOnRecordEnd(Observable<Friendship> observable);
    void initScrollOnGrid(Observable<Integer> observable);
}
