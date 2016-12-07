package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.DeleteContactsABJob;
import com.tribe.app.data.network.job.DeleteContactsFBJob;
import com.tribe.app.data.network.job.RefreshHowManyFriendsJob;
import com.tribe.app.data.network.job.UpdateFriendshipJob;
import com.tribe.app.data.network.job.UpdateScoreJob;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import com.tribe.app.domain.interactor.user.DiskUpdateFriendship;
import com.tribe.app.domain.interactor.user.GetBlockedFriendshipList;
import com.tribe.app.domain.interactor.user.LookupUsername;
import com.tribe.app.domain.interactor.user.RemoveInstall;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.SettingsMVPView;
import com.tribe.app.presentation.mvp.view.UpdateUserMVPView;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by horatiothomas on 8/31/16.
 */
public class SettingsPresenter extends UpdateUserPresenter {

    private SettingsMVPView settingsView;

    private final DiskUpdateFriendship diskUpdateFriendship;
    private final GetBlockedFriendshipList getBlockedFriendshipList;
    private final RemoveInstall removeInstall;
    private final UseCase synchroContactList;
    private UseCaseDisk getDiskContactList;
    private UseCaseDisk getDiskFBContactList;
    private JobManager jobManager;

    private LookupContactsSubscriber lookupContactsSubscriber;
    private GetBlockedFriendshipListSubscriber getBlockedFriendshipListSubscriber;

    @Inject
    SettingsPresenter(UpdateUser updateUser,
                      @Named("lookupByUsername") LookupUsername lookupUsername,
                      RxFacebook rxFacebook,
                      RemoveInstall removeInstall,
                      @Named("synchroContactList") UseCase synchroContactList,
                      JobManager jobManager,
                      @Named("diskContactList") UseCaseDisk getDiskContactList,
                      @Named("diskFBContactList") UseCaseDisk getDiskFBContactList,
                      DiskUpdateFriendship diskUpdateFriendship,
                      GetBlockedFriendshipList getBlockedFriendshipList) {
        super(updateUser, lookupUsername, rxFacebook);
        this.removeInstall = removeInstall;
        this.synchroContactList = synchroContactList;
        this.jobManager = jobManager;
        this.getDiskContactList = getDiskContactList;
        this.getDiskFBContactList = getDiskFBContactList;
        this.diskUpdateFriendship = diskUpdateFriendship;
        this.getBlockedFriendshipList = getBlockedFriendshipList;
    }

    @Override
    public void onViewDetached() {
        removeInstall.unsubscribe();
        synchroContactList.unsubscribe();
        getDiskContactList.unsubscribe();
        getDiskFBContactList.unsubscribe();
        diskUpdateFriendship.unsubscribe();
        getBlockedFriendshipList.unsubscribe();
    }

    @Override
    public void onViewAttached(MVPView v) {
        settingsView = (SettingsMVPView) v;
        loadBlockedFriendshipList();
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
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.LOCATION, 1));
    }

    public void updateScoreRateApp() {
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.RATE_APP, 1));
    }

    public void deleteABContacts() {
        jobManager.addJobInBackground(new DeleteContactsABJob());
    }

    public void deleteFBContacts() {
        jobManager.addJobInBackground(new DeleteContactsFBJob());
    }

    public void goToLauncher() {
        this.settingsView.goToLauncher();
    }

    @Override
    protected UpdateUserMVPView getUpdateUserView() {
        return settingsView;
    }

    public void updateScoreShare() {
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.SHARE_PROFILE, 1));
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
//            int countInApp = 0;
//
//            if (contactList != null) {
//                for (Contact contact : contactList) {
//                    if (contact.getUserList() != null && contact.getUserList().size() > 0) {
//                        countInApp++;
//                    }
//                }
//            }

            if (contactList != null) {
                settingsView.onAddressBookContactSync(contactList.size());
            }
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
//            int countInApp = 0;
//
//            if (contactList != null) {
//                for (Contact contact : contactList) {
//                    if (contact.getUserList() != null && contact.getUserList().size() > 0) {
//                        countInApp++;
//                    }
//                }
//            }

            if (contactList != null) {
                settingsView.onFBContactsSync(contactList.size());
            }
        }
    }

    public void loadBlockedFriendshipList() {
        if (getBlockedFriendshipListSubscriber != null) getBlockedFriendshipListSubscriber.unsubscribe();

        getBlockedFriendshipListSubscriber = new GetBlockedFriendshipListSubscriber();
        getBlockedFriendshipList.execute(getBlockedFriendshipListSubscriber);
    }

    private class GetBlockedFriendshipListSubscriber extends DefaultSubscriber<List<Friendship>> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {}

        @Override
        public void onNext(List<Friendship> friendshipList) {
            settingsView.renderBlockedFriendshipList(friendshipList);
        }
    }

    public void updateFriendship(String friendshipId) {
        diskUpdateFriendship.prepare(friendshipId, FriendshipRealm.DEFAULT);
        diskUpdateFriendship.execute(new UpdateFriendshipSubscriber());
        jobManager.addJobInBackground(new UpdateFriendshipJob(friendshipId, FriendshipRealm.DEFAULT));
    }

    private class UpdateFriendshipSubscriber extends DefaultSubscriber<Friendship> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {}

        @Override
        public void onNext(Friendship friendship) {
            settingsView.friendshipUpdated(friendship);
        }
    }
}
