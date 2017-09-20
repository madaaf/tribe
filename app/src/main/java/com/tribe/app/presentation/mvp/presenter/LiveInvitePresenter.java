package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.presentation.mvp.presenter.common.RoomPresenter;
import com.tribe.app.presentation.mvp.presenter.common.ShortcutPresenter;
import com.tribe.app.presentation.mvp.view.LiveInviteMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import javax.inject.Inject;

public class LiveInvitePresenter implements Presenter {

  // COMPOSITE PRESENTERS
  private ShortcutPresenter shortcutPresenter;
  private RoomPresenter roomPresenter;

  // VIEW ATTACHED
  private LiveInviteMVPView liveInviteMVPView;

  // USECASES

  // SUBSCRIBERS

  @Inject
  public LiveInvitePresenter(RoomPresenter roomPresenter, ShortcutPresenter shortcutPresenter) {
    this.shortcutPresenter = shortcutPresenter;
    this.roomPresenter = roomPresenter;
  }

  @Override public void onViewDetached() {
    shortcutPresenter.onViewDetached();
    roomPresenter.onViewDetached();
    liveInviteMVPView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    liveInviteMVPView = (LiveInviteMVPView) v;
    shortcutPresenter.onViewAttached(v);
    roomPresenter.onViewAttached(v);
  }

  public void loadShortcuts() {
    shortcutPresenter.loadSingleShortcuts();
  }
}
