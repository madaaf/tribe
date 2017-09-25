package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.presentation.mvp.presenter.common.ShortcutPresenter;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.NewChatMVPView;
import javax.inject.Inject;

/**
 * Created by tiago on 09/25/2017.
 */

public class NewChatPresenter implements Presenter {

  private NewChatMVPView chatMVPView;

  private ShortcutPresenter shortcutPresenter;

  @Inject NewChatPresenter(ShortcutPresenter shortcutPresenter) {
    this.shortcutPresenter = shortcutPresenter;
  }

  @Override public void onViewDetached() {
    shortcutPresenter.onViewDetached();
    chatMVPView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    chatMVPView = (NewChatMVPView) v;
    shortcutPresenter.onViewAttached(v);
  }

  public void loadSingleShortcuts() {
    shortcutPresenter.loadSingleShortcuts();
  }
}
