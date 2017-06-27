package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.presentation.mvp.view.FBInfoMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by madaaflak on 23/06/2017.
 */

public class FacebookPresenter implements Presenter {

  // VIEW ATTACHED
  private FBInfoMVPView profileInfoView;
  protected final RxFacebook rxFacebook;

  protected CompositeSubscription subscriptions = new CompositeSubscription();

  @Inject public FacebookPresenter(RxFacebook rxFacebook) {
    this.rxFacebook = rxFacebook;
  }

  @Override public void onViewDetached() {
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    profileInfoView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    profileInfoView = (FBInfoMVPView) v;
  }

  public void loginFacebook() {
    if (!FacebookUtils.isLoggedIn()) {
      subscriptions.add(rxFacebook.requestLogin().subscribe(loginResult -> {
        if (FacebookUtils.isLoggedIn()) {
          profileInfoView.successFacebookLogin();
        } else {
          profileInfoView.errorFacebookLogin();
        }
      }));
    } else {
      profileInfoView.successFacebookLogin();
    }
  }

  public void loadFacebookInfos() {
    subscriptions.add(rxFacebook.requestInfos().subscribe(new FacebookInfosSubscriber()));
  }

  private class FacebookInfosSubscriber extends DefaultSubscriber<FacebookEntity> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(FacebookEntity facebookEntity) {
      if (facebookEntity != null) profileInfoView.loadFacebookInfos(facebookEntity);
    }
  }
}
