package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.user.LookupUsername;
import com.tribe.app.domain.interactor.user.RemoveInstall;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.presentation.mvp.view.SettingView;
import com.tribe.app.presentation.mvp.view.UpdateUserView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.utils.facebook.RxFacebook;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by horatiothomas on 8/31/16.
 */
public class SettingPresenter extends UpdateUserPresenter {

    private final RemoveInstall removeInstall;
    private final UseCase synchroContactList;

    private SettingView settingView;

    private LookupContactsSubscriber lookupContactsSubscriber;

    @Inject
    SettingPresenter(UpdateUser updateUser, @Named("lookupByUsername") LookupUsername lookupUsername,
                     RxFacebook rxFacebook, RemoveInstall removeInstall, @Named("synchroContactList") UseCase synchroContactList) {
        super(updateUser, lookupUsername, rxFacebook);
        this.removeInstall = removeInstall;
        this.synchroContactList = synchroContactList;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void attachView(View v) {
        settingView = (SettingView) v;
    }

    @Override
    public void onCreate() {

    }

    public void logout() {
        removeInstall.execute(new RemoveInstallSubscriber());
    }

    public void lookupContacts() {
        if (lookupContactsSubscriber != null) lookupContactsSubscriber.unsubscribe();
        lookupContactsSubscriber = new LookupContactsSubscriber();
        synchroContactList.execute(lookupContactsSubscriber);
    }

    public void goToLauncher() {
        this.settingView.goToLauncher();
    }

    @Override
    protected UpdateUserView getUpdateUserView() {
        return settingView;
    }

    private final class RemoveInstallSubscriber extends DefaultSubscriber<User> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {}

        @Override
        public void onNext(User user) {
            goToLauncher();
        }
    }

    private class LookupContactsSubscriber extends DefaultSubscriber<List<Contact>> {

        @Override
        public void onCompleted() { }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(List<Contact> contactList) {

        }
    }
}
