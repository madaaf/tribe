package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.presentation.mvp.view.ScoreView;
import com.tribe.app.presentation.mvp.view.View;

import javax.inject.Inject;

public class ScorePresenter implements Presenter {

    private ScoreView scoreView;

    @Inject
    public ScorePresenter() {

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
        scoreView = (ScoreView) v;
    }
}
