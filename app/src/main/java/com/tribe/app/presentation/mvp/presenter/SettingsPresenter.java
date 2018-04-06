package com.tribe.app.presentation.mvp.presenter;

import android.app.Activity;
import android.content.Context;
import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import com.tribe.app.domain.interactor.user.LookupUsername;
import com.tribe.app.domain.interactor.user.RemoveInstall;
import com.tribe.app.domain.interactor.user.SynchroContactList;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.domain.interactor.user.UpdateUserAge;
import com.tribe.app.domain.interactor.user.UpdateUserFacebook;
import com.tribe.app.domain.interactor.user.UpdateUserPhoneNumber;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.SettingsMVPView;
import com.tribe.app.presentation.mvp.view.UpdateUserMVPView;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by horatiothomas on 8/31/16.
 */
public class SettingsPresenter extends UpdateUserPresenter {

  private SettingsMVPView settingsView;

  private final RemoveInstall removeInstall;
  private final SynchroContactList synchroContactList;
  private UseCaseDisk getDiskContactList;
  private UseCaseDisk getDiskFBContactList;
  private JobManager jobManager;

  private LookupContactsSubscriber lookupContactsSubscriber;

  @Inject SettingsPresenter(UpdateUser updateUser,
      @Named("lookupByUsername") LookupUsername lookupUsername, RxFacebook rxFacebook,
      RemoveInstall removeInstall,  SynchroContactList synchroContactList,
      JobManager jobManager, @Named("diskContactList") UseCaseDisk getDiskContactList,
      @Named("diskFBContactList") UseCaseDisk getDiskFBContactList,
      UpdateUserFacebook updateUserFacebook, UpdateUserPhoneNumber updateUserPhoneNumber, UpdateUserAge updateUserAge) {
    super(updateUser, lookupUsername, rxFacebook, updateUserFacebook, updateUserPhoneNumber, updateUserAge);
    this.removeInstall = removeInstall;
    this.synchroContactList = synchroContactList;
    this.jobManager = jobManager;
    this.getDiskContactList = getDiskContactList;
    this.getDiskFBContactList = getDiskFBContactList;
  }

  @Override public void onViewDetached() {
    removeInstall.unsubscribe();
    synchroContactList.unsubscribe();
    getDiskContactList.unsubscribe();
    getDiskFBContactList.unsubscribe();
    settingsView = null;
    super.onViewDetached();
  }

  @Override public void onViewAttached(MVPView v) {
    settingsView = (SettingsMVPView) v;
  }

  public void logout() {
    removeInstall.execute(new RemoveInstallSubscriber());
  }

  public void lookupContacts(Activity c) {
    if (lookupContactsSubscriber != null) lookupContactsSubscriber.unsubscribe();
    lookupContactsSubscriber = new LookupContactsSubscriber();
    synchroContactList.setParams(c);
    synchroContactList.execute(lookupContactsSubscriber);
  }

  public void loadContactsFB() {
    getDiskContactList.execute(new ContactListSubscriber());
  }

  public void loadContactsAddressBook() {
    getDiskFBContactList.execute(new ContactFBListSubscriber());
  }

  public void goToLauncher() {
    this.settingsView.goToLauncher();
  }

  @Override protected UpdateUserMVPView getUpdateUserView() {
    return settingsView;
  }

  private final class RemoveInstallSubscriber extends DefaultSubscriber<User> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
    }

    @Override public void onNext(User user) {
      goToLauncher();
    }
  }

  private class LookupContactsSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(List<Contact> contactList) {
    }
  }

  private final class ContactListSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {

    }

    @Override public void onError(Throwable e) {
    }

    @Override public void onNext(List<Contact> contactList) {
      if (contactList != null) {
        settingsView.onAddressBookContactSync(contactList.size());
      }
    }
  }

  private final class ContactFBListSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {

    }

    @Override public void onError(Throwable e) {
    }

    @Override public void onNext(List<Contact> contactList) {
      if (contactList != null) {
        settingsView.onFBContactsSync(contactList.size());
      }
    }
  }
}
