package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.DeleteContactsABJob;
import com.tribe.app.data.network.job.DeleteContactsFBJob;
import com.tribe.app.data.network.job.RefreshHowManyFriendsJob;
import com.tribe.app.data.network.job.UpdateScoreJob;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import com.tribe.app.domain.interactor.user.LookupUsername;
import com.tribe.app.domain.interactor.user.RemoveInstall;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.presentation.mvp.view.SettingView;
import com.tribe.app.presentation.mvp.view.UpdateUserView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by horatiothomas on 8/31/16.
 */
public class SettingPresenter extends UpdateUserPresenter {

    private SettingView settingView;

    private final RemoveInstall removeInstall;
    private final UseCase synchroContactList;
    private UseCaseDisk getDiskContactList;
    private UseCaseDisk getDiskFBContactList;
    private JobManager jobManager;

    private LookupContactsSubscriber lookupContactsSubscriber;

    @Inject
    SettingPresenter(UpdateUser updateUser,
                     @Named("lookupByUsername") LookupUsername lookupUsername,
                     RxFacebook rxFacebook,
                     RemoveInstall removeInstall,
                     @Named("synchroContactList") UseCase synchroContactList,
                     JobManager jobManager,
                     @Named("diskContactList") UseCaseDisk getDiskContactList,
                     @Named("diskFBContactList") UseCaseDisk getDiskFBContactList) {
        super(updateUser, lookupUsername, rxFacebook);
        this.removeInstall = removeInstall;
        this.synchroContactList = synchroContactList;
        this.jobManager = jobManager;
        this.getDiskContactList = getDiskContactList;
        this.getDiskFBContactList = getDiskFBContactList;
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
        removeInstall.unsubscribe();
        synchroContactList.unsubscribe();
        getDiskContactList.unsubscribe();
        getDiskFBContactList.unsubscribe();
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

    public void loadContactsFB() {
        getDiskContactList.execute(new ContactListSubscriber());
    }

    public void loadContactsAddressBook() {
        getDiskFBContactList.execute(new ContactFBListSubscriber());
    }

    public void updateScoreLocation() {
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.LOCATION));
    }

    public void updateScoreRateApp() {
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.RATE_APP));
    }

    public void deleteABContacts() {
        jobManager.addJobInBackground(new DeleteContactsABJob());
    }

    public void deleteFBContacts() {
        jobManager.addJobInBackground(new DeleteContactsFBJob());
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
            jobManager.addJobInBackground(new RefreshHowManyFriendsJob());
        }
    }

    private final class ContactListSubscriber extends DefaultSubscriber<List<Contact>> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(List<Contact> contactList) {
            int countInApp = 0;

            if (contactList != null) {
                for (Contact contact : contactList) {
                    if (contact.getUserList() != null && contact.getUserList().size() > 0) {
                        countInApp++;
                    }
                }
            }

            settingView.onAddressBookContactSync(countInApp);
        }
    }

    private final class ContactFBListSubscriber extends DefaultSubscriber<List<Contact>> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(List<Contact> contactList) {
            int countInApp = 0;

            if (contactList != null) {
                for (Contact contact : contactList) {
                    if (contact.getUserList() != null && contact.getUserList().size() > 0) {
                        countInApp++;
                    }
                }
            }

            settingView.onFBContactsSync(countInApp);
        }
    }
}
