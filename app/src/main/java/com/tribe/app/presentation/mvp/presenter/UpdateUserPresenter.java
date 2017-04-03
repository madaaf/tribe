package com.tribe.app.presentation.mvp.presenter;

import android.util.Pair;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.LookupUsername;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.presentation.mvp.view.UpdateUserMVPView;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import java.util.ArrayList;
import java.util.List;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 09/20/16.
 */
public abstract class UpdateUserPresenter implements Presenter {

  protected final LookupUsername lookupUsername;
  protected final UpdateUser updateUser;
  protected final RxFacebook rxFacebook;

  protected CompositeSubscription subscriptions = new CompositeSubscription();
  private UpdateUserSubscriber updateUserSubscriber;
  private LookupUsernameSubscriber lookupUsernameSubscriber;

  UpdateUserPresenter(UpdateUser updateUser, LookupUsername lookupUsername, RxFacebook rxFacebook) {
    this.lookupUsername = lookupUsername;
    this.updateUser = updateUser;
    this.rxFacebook = rxFacebook;
  }

  @Override public void onViewDetached() {
    updateUser.unsubscribe();
    lookupUsername.unsubscribe();
    if (lookupUsernameSubscriber != null) lookupUsernameSubscriber.unsubscribe();
    if (updateUserSubscriber != null) updateUserSubscriber.unsubscribe();
  }

  public void updateUser(String username, String displayName, String pictureUri, String fbid) {
    if (updateUserSubscriber != null) updateUserSubscriber.unsubscribe();

    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(UserRealm.DISPLAY_NAME, displayName));
    values.add(new Pair<>(UserRealm.USERNAME, username));
    if (!StringUtils.isEmpty(pictureUri)) {
      values.add(new Pair<>(UserRealm.PROFILE_PICTURE, pictureUri));
    }
    values.add(new Pair<>(UserRealm.FBID, fbid));

    getUpdateUserView().showLoading();

    updateUserSubscriber = new UpdateUserSubscriber();
    updateUser.prepare(values);
    updateUser.execute(updateUserSubscriber);
  }

  public void updateUserTribeSave(boolean tribeSave) {
    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(UserRealm.TRIBE_SAVE, String.valueOf(tribeSave)));
    updateUser.prepare(values);
    updateUser.execute(new UpdateUserSubscriber());
  }

  public void updateUserNotifications(boolean notifications) {
    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(UserRealm.PUSH_NOTIF, String.valueOf(notifications)));
    updateUser.prepare(values);
    updateUser.execute(new UpdateUserSubscriber());
  }

  public void updateUserInvisibleMode(boolean tribeSave) {
    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(UserRealm.INVISIBLE_MODE, String.valueOf(tribeSave)));
    updateUser.prepare(values);
    updateUser.execute(new UpdateUserSubscriber());
  }

  public void updateUserFacebook(String fbid) {
    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(UserRealm.FBID, String.valueOf(fbid)));
    updateUser.prepare(values);
    updateUser.execute(new UpdateUserSubscriber());
  }

  public void loginFacebook() {
    if (!FacebookUtils.isLoggedIn()) {
      rxFacebook.requestLogin().subscribe(loginResult -> {
        if (FacebookUtils.isLoggedIn()) {
          getUpdateUserView().successFacebookLogin();
        } else {
          getUpdateUserView().errorFacebookLogin();
        }
      });
    } else {
      getUpdateUserView().successFacebookLogin();
    }
  }

  public void lookupUsername(String username) {
    if (lookupUsernameSubscriber != null) lookupUsernameSubscriber.unsubscribe();

    lookupUsernameSubscriber = new LookupUsernameSubscriber();
    lookupUsername.setUsername(username);
    lookupUsername.execute(lookupUsernameSubscriber);
  }

  protected abstract UpdateUserMVPView getUpdateUserView();

  protected final class UpdateUserSubscriber extends DefaultSubscriber<User> {

    @Override public void onCompleted() {

    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
      getUpdateUserView().hideLoading();
    }

    @Override public void onNext(User user) {
      getUpdateUserView().hideLoading();
      getUpdateUserView().successUpdateUser(user);
    }
  }

  private class LookupUsernameSubscriber extends DefaultSubscriber<Boolean> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      if (getUpdateUserView() != null) getUpdateUserView().usernameResult(false);
      e.printStackTrace();
    }

    @Override public void onNext(Boolean available) {
      if (getUpdateUserView() != null) getUpdateUserView().usernameResult(available);
    }
  }
}
