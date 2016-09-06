package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.SynchroContactsJob;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import com.tribe.app.presentation.mvp.view.ContactsView;
import com.tribe.app.presentation.mvp.view.View;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class ContactsGridPresenter implements Presenter {

    // VIEW ATTACHED
    private ContactsView contactsView;

    // USECASES
    private JobManager jobManager;
    private UseCaseDisk getDiskContactList;

    // SUBSCRIBERS

    @Inject
    public ContactsGridPresenter(JobManager jobManager,
                                 @Named("diskContactList") UseCaseDisk getDiskContactList) {
        super();
        this.jobManager = jobManager;
        this.getDiskContactList = getDiskContactList;
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

    }

    @Override
    public void attachView(View v) {
        contactsView = (ContactsView) v;
    }

    public void loadContactList() {
        jobManager.addJobInBackground(new SynchroContactsJob());
        getDiskContactList.execute(new ContactListSubscriber());
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
}
