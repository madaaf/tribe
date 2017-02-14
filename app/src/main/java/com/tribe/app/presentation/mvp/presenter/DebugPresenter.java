package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.presentation.mvp.view.DebugMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import javax.inject.Inject;

public class DebugPresenter implements Presenter {

  // VIEW ATTACHED
  private DebugMVPView debugView;

  // USECASES

  // SUBSCRIBERS

  @Inject public DebugPresenter() {
  }

  @Override public void onViewDetached() {

  }

  @Override public void onViewAttached(MVPView v) {
    debugView = (DebugMVPView) v;
  }
}
