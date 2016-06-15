package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.presentation.mvp.view.TribeView;
import com.tribe.app.presentation.mvp.view.View;

import javax.inject.Inject;

public class TribePresenter implements Presenter {

    private Tribe tribe;
    private TribeView tribeView;

    @Inject
    public TribePresenter() {

    }

    @Override
    public void onCreate() {
        // Unused
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
        // Unused
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onPause() {
        // Unused
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void attachView(View v) {
        tribeView = (TribeView) v;
    }
}
