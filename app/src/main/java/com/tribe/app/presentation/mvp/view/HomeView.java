package com.tribe.app.presentation.mvp.view;

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
    void initNewTribes(Observable<List<TribeMessage>> observable);
}
