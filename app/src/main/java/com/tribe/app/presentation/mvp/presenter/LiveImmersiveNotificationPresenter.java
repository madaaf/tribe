package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.live.RemoveInvite;
import com.tribe.app.presentation.mvp.view.MVPView;
import javax.inject.Inject;

/**
 * Created by madaaflak on 03/05/2017.
 */

public class LiveImmersiveNotificationPresenter implements Presenter {

  private RemoveInvite removeInvite;

  @Inject public LiveImmersiveNotificationPresenter(RemoveInvite removeInvite) {
    this.removeInvite = removeInvite;
  }

  @Override public void onViewAttached(MVPView view) {
  }

  @Override public void onViewDetached() {
    removeInvite.unsubscribe();
  }

  public void removeInvite(String roomId, String userId) {
    removeInvite.setup(roomId, userId);
    removeInvite.execute(new DefaultSubscriber());
  }
}
