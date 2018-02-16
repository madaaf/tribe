package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.GetDiskFBContactInviteList;
import com.tribe.app.presentation.mvp.presenter.common.ShortcutPresenter;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.NewChatMVPView;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by tiago on 09/25/2017.
 */

public class NewChatPresenter implements Presenter {

  private NewChatMVPView chatMVPView;

  private ShortcutPresenter shortcutPresenter;
  private GetDiskFBContactInviteList getDiskFBContactInviteList;

  private FBContactListInviteSubscriber fbContactListInviteSubscriber;

  @Inject NewChatPresenter(ShortcutPresenter shortcutPresenter,
      GetDiskFBContactInviteList getDiskFBContactInviteList) {
    this.shortcutPresenter = shortcutPresenter;
    this.getDiskFBContactInviteList = getDiskFBContactInviteList;
  }

  @Override public void onViewDetached() {
    shortcutPresenter.onViewDetached();
    getDiskFBContactInviteList.unsubscribe();
    chatMVPView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    chatMVPView = (NewChatMVPView) v;
    shortcutPresenter.onViewAttached(v);
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
}
