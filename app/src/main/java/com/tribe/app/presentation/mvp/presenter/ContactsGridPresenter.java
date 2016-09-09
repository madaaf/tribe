package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.SynchroContactsJob;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import com.tribe.app.domain.interactor.user.DiskFindContactByValue;
import com.tribe.app.domain.interactor.user.DiskSearchResults;
import com.tribe.app.domain.interactor.user.FindByUsername;
import com.tribe.app.presentation.mvp.view.ContactsView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class ContactsGridPresenter implements Presenter {

    // VIEW ATTACHED
    private ContactsView contactsView;

    // USECASES
    private JobManager jobManager;
    private RxFacebook rxFacebook;
    private UseCaseDisk getDiskContactList;
    private FindByUsername findByUsername;
    private DiskSearchResults searchResults;
    private DiskFindContactByValue findContactByValue;

    // SUBSCRIBERS
    private FindByValueSubscriber findByValueSubscriber;

    @Inject
    public ContactsGridPresenter(JobManager jobManager,
                                 RxFacebook rxFacebook,
                                 @Named("diskContactList") UseCaseDisk getDiskContactList,
                                 @Named("cloudFindByUsername") FindByUsername findByUsername,
                                 @Named("diskSearchResults") DiskSearchResults diskSearchResults,
                                 @Named("diskFindContactByValue") DiskFindContactByValue diskFindContactByValue) {
        super();
        this.jobManager = jobManager;
        this.rxFacebook = rxFacebook;
        this.getDiskContactList = getDiskContactList;
        this.findByUsername = findByUsername;
        this.searchResults = diskSearchResults;
        this.findContactByValue = diskFindContactByValue;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {
        // Unused
    }

    @Override
    public void onResume() {
        // Unused
    }

    @Override
    public void onStop() {
        // Unused
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {
        findByUsername.unsubscribe();
        findContactByValue.unsubscribe();
        searchResults.unsubscribe();
        getDiskContactList.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        contactsView = (ContactsView) v;
    }

    public void findByUsername(String username) {
        findByUsername.setUsername(username);
        findByUsername.execute(new DefaultSubscriber<>());

        if (findByValueSubscriber != null)
            findByValueSubscriber.unsubscribe();

        findByValueSubscriber = new FindByValueSubscriber();
        findContactByValue.setValue(username);
        findContactByValue.execute(findByValueSubscriber);
    }

    public void loadContactList() {
        getDiskContactList.execute(new ContactListSubscriber());
        searchResults.execute(new SearchResultSubscriber());
    }

    public void loginFacebook() {
        if (!FacebookUtils.isLoggedIn()) {
            rxFacebook.requestLogin().subscribe(loginResult -> {
                if (FacebookUtils.isLoggedIn()) {
                    jobManager.addJobInBackground(new SynchroContactsJob());
                } else {
                    System.out.println("LOGIN FAIL !");
                }
            });
        } else {
            jobManager.addJobInBackground(new SynchroContactsJob());
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
            contactsView.renderContactList(contactList);
        }
    }

    private final class FindByValueSubscriber extends DefaultSubscriber<List<Contact>> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(List<Contact> contactList) {
            System.out.println("LOL : " + contactList.size());
        }
    }

    private final class SearchResultSubscriber extends DefaultSubscriber<SearchResult> {

        @Override
        public void onCompleted() {
            System.out.println("COMPLETED");
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(SearchResult searchResult) {
            System.out.println("SEARCH : " + searchResult);
        }
    }
}
