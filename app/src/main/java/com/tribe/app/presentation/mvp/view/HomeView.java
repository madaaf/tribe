package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;

import java.util.List;

import rx.Observable;

public interface HomeView extends LoadDataView {

    void initOpenTribes(Observable<Recipient> observable);
    void initClicksOnChat(Observable<Recipient> observable);
    void initOnRecordStart(Observable<String> observable);
    void initOnRecordEnd(Observable<Recipient> observable);
    void initScrollOnGrid(Observable<Integer> observable);
    void initPendingTribes(Observable<Integer> observable);
    void initPendingTribeItemSelected(Observable<List<TribeMessage>> observable);
    void initNewMessages(Observable<List<Message>> observable);
    void initClickOnPoints(Observable<android.view.View> observable);
    void initClickOnSettings(Observable<android.view.View> observable);
    void initPullToSearchActive(Observable<Boolean> observable);
}
