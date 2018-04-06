package com.tribe.app.presentation.mvp.presenter;

import android.app.Activity;
import android.content.Context;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.user.SynchroContactList;
import com.tribe.app.presentation.mvp.view.DebugMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

public class DebugPresenter implements Presenter {

  // VIEW ATTACHED
  private DebugMVPView debugView;

  // USECASES
  private SynchroContactList synchroContactList;

  // SUBSCRIBERS
  private LookupContactsSubscriber lookupContactsSubscriber;

  @Inject public DebugPresenter( SynchroContactList synchroContactList) {
    this.synchroContactList = synchroContactList;
  }

  @Override public void onViewDetached() {
    synchroContactList.unsubscribe();
  }

  @Override public void onViewAttached(MVPView v) {
    debugView = (DebugMVPView) v;
  }

  public void lookupContacts(Activity c) {
    if (lookupContactsSubscriber != null) lookupContactsSubscriber.unsubscribe();
    lookupContactsSubscriber = new LookupContactsSubscriber();
    synchroContactList.setParams(c);
    synchroContactList.execute(lookupContactsSubscriber);
  }

  private class LookupContactsSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(List<Contact> contactList) {
      debugView.onSyncDone();
    }
  }
}
