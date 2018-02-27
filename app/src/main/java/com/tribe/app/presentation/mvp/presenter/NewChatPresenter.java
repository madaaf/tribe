package com.tribe.app.presentation.mvp.presenter;

import android.util.Pair;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.GetDiskFBContactInviteList;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.presentation.mvp.presenter.common.ShortcutPresenter;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.NewChatMVPView;
import com.tribe.app.presentation.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by tiago on 09/25/2017.
 */

public class NewChatPresenter implements Presenter {

  private NewChatMVPView chatMVPView;

  private ShortcutPresenter shortcutPresenter;

  private GetDiskFBContactInviteList getDiskFBContactInviteList;
  protected final UpdateUser updateUser;

  private FBContactListInviteSubscriber fbContactListInviteSubscriber;
  private UpdateUserSubscriber updateUserSubscriber;

  @Inject NewChatPresenter(ShortcutPresenter shortcutPresenter,
      GetDiskFBContactInviteList getDiskFBContactInviteList, UpdateUser updateUser) {
    this.shortcutPresenter = shortcutPresenter;
    this.getDiskFBContactInviteList = getDiskFBContactInviteList;
    this.updateUser = updateUser;
  }

  @Override public void onViewDetached() {
    shortcutPresenter.onViewDetached();
    getDiskFBContactInviteList.unsubscribe();
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

  public void loadFBContactsInvite() {
    if (fbContactListInviteSubscriber != null) {
      fbContactListInviteSubscriber.unsubscribe();
    }

    fbContactListInviteSubscriber = new FBContactListInviteSubscriber();
    getDiskFBContactInviteList.execute(fbContactListInviteSubscriber);
  }

  public void loadSingleShortcuts() {
    shortcutPresenter.loadSingleShortcuts();
  }

  public void createShortcut(String... userIds) {
    shortcutPresenter.createShortcut(userIds);
  }

  private final class FBContactListInviteSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {

    }

    @Override public void onNext(List<Contact> contactList) {
      chatMVPView.onLoadFBContactsInvite(contactList);
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
