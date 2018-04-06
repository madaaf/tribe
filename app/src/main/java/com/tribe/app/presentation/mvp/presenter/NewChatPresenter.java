package com.tribe.app.presentation.mvp.presenter;

import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.ContactFB;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.GetContactFbList;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.presentation.mvp.presenter.common.ShortcutPresenter;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.NewChatMVPView;
import com.tribe.app.presentation.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Created by tiago on 09/25/2017.
 */

public class NewChatPresenter implements Presenter {

  private NewChatMVPView chatMVPView;

  private ShortcutPresenter shortcutPresenter;

  private GetContactFbList getContactFbList;
  protected final UpdateUser updateUser;

  private GetContactFbListeSubscriber getContactFbListeSubscriber;
  private UpdateUserSubscriber updateUserSubscriber;

  @Inject NewChatPresenter(ShortcutPresenter shortcutPresenter, UpdateUser updateUser,
      GetContactFbList getContactFbList) {
    this.shortcutPresenter = shortcutPresenter;
    this.updateUser = updateUser;
    this.getContactFbList = getContactFbList;
  }

  @Override public void onViewDetached() {
    shortcutPresenter.onViewDetached();
    getContactFbList.unsubscribe();
    updateUser.unsubscribe();
    chatMVPView = null;
    if (updateUserSubscriber != null) updateUserSubscriber.unsubscribe();
  }

  @Override public void onViewAttached(MVPView v) {
    chatMVPView = (NewChatMVPView) v;
    shortcutPresenter.onViewAttached(v);
  }

  public void updateUser(String userId, String username, String displayName, String pictureUri) {
    if (updateUserSubscriber != null) updateUserSubscriber.unsubscribe();

    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(UserRealm.DISPLAY_NAME, displayName));
    values.add(new Pair<>(UserRealm.USERNAME, username));
    if (!StringUtils.isEmpty(pictureUri)) {
      values.add(new Pair<>(UserRealm.PROFILE_PICTURE, pictureUri));
    }

    updateUserSubscriber = new UpdateUserSubscriber();
    updateUser.prepare(values);
    updateUser.execute(updateUserSubscriber);
  }

  public void getContactFbList(int number, Context c) {
    if (getContactFbListeSubscriber != null) {
      getContactFbListeSubscriber.unsubscribe();
    }

    getContactFbListeSubscriber = new GetContactFbListeSubscriber();
    getContactFbList.setParams(number, c);
    getContactFbList.execute(getContactFbListeSubscriber);
  }

  public void loadSingleShortcuts() {
    shortcutPresenter.loadSingleShortcuts();
  }

  public void createShortcut(String... userIds) {
    shortcutPresenter.createShortcut(userIds);
  }

  private final class GetContactFbListeSubscriber extends DefaultSubscriber<List<ContactFB>> {

    @Override public void onError(Throwable e) {
      Timber.e("on error GetContactFbListeSubscriber " + e.getMessage());
    }

    @Override public void onNext(List<ContactFB> contactList) {
      chatMVPView.onLoadFBContactsFbInvite(contactList);
    }
  }

  protected final class UpdateUserSubscriber extends DefaultSubscriber<User> {

    @Override public void onCompleted() {

    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(User user) {
    }
  }
}
