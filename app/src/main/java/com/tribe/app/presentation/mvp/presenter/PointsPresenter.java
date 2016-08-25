package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.presentation.mvp.view.PointsView;
import com.tribe.app.presentation.mvp.view.View;

import javax.inject.Inject;

public class PointsPresenter implements Presenter {

    private PointsView pointsView;

    @Inject
    public PointsPresenter() {

    }

    @Override
    public void onCreate() {

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
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void attachView(View v) {
        pointsView = (PointsView) v;
    }
}
