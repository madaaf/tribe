package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.PointsMVPView;

import javax.inject.Inject;

public class PointsPresenter implements Presenter {

    private PointsMVPView pointsView;

    @Inject
    public PointsPresenter() {

    }

    @Override
    public void onViewAttached(MVPView v) {
        pointsView = (PointsMVPView) v;
    }

    @Override
    public void onViewDetached() {

    }
}
