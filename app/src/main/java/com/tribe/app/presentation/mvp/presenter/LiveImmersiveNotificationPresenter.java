package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.DeclineInvite;
import com.tribe.app.presentation.mvp.view.MVPView;
import javax.inject.Inject;

/**
 * Created by madaaflak on 03/05/2017.
 */

public class LiveImmersiveNotificationPresenter implements Presenter {

  private DeclineInvite declineInvite;

  @Inject public LiveImmersiveNotificationPresenter(DeclineInvite declineInvite) {
    this.declineInvite = declineInvite;
  }

  @Override public void onViewAttached(MVPView view) {
  }

  @Override public void onViewDetached() {
    declineInvite.unsubscribe();
  }

  public void declineInvite(String roomId) {
    declineInvite.prepare(roomId);
    declineInvite.execute(new DefaultSubscriber());
  }
}
