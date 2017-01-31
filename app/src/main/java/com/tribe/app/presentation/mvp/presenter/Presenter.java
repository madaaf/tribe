package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.presentation.mvp.view.MVPView;

public interface Presenter {

  void onViewAttached(MVPView view);

  void onViewDetached();
}
