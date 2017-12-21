package com.tribe.app.presentation.mvp.presenter;

import android.util.Pair;

import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.domain.interactor.user.UpdateUserFacebook;
import com.tribe.app.domain.interactor.user.UpdateUserPhoneNumber;
import com.tribe.app.presentation.mvp.view.FBInfoMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.UpdateUserMVPView;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by madaaflak on 23/06/2017.
 */

public class FacebookPresenter extends UpdateUserPresenter {

  // VIEW ATTACHED
  private FBInfoMVPView profileInfoView;

  protected CompositeSubscription subscriptions = new CompositeSubscription();

  @Inject public FacebookPresenter(RxFacebook rxFacebook, UpdateUser updateUser, UpdateUserFacebook updateUserFacebook, UpdateUserPhoneNumber updateUserPhoneNumber) {
    super(updateUser, null, rxFacebook, updateUserFacebook, updateUserPhoneNumber);
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

  @Override protected UpdateUserMVPView getUpdateUserView() {
    return null;
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
      if (facebookEntity != null) {
        profileInfoView.loadFacebookInfos(facebookEntity);
        updateAgeRange(facebookEntity);
      }
    }
  }
}
