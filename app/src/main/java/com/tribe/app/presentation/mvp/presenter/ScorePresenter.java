package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.ScoreMVPView;

import javax.inject.Inject;

public class ScorePresenter implements Presenter {

  private ScoreMVPView scoreView;

  @Inject public ScorePresenter() {

  }

  @Override public void onViewAttached(MVPView v) {
    scoreView = (ScoreMVPView) v;
  }

  @Override public void onViewDetached() {

  }
}
