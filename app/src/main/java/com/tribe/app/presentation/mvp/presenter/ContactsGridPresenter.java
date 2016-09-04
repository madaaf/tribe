package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.presentation.mvp.view.ContactsView;
import com.tribe.app.presentation.mvp.view.View;

import javax.inject.Inject;

public class ContactsGridPresenter implements Presenter {

    // VIEW ATTACHED
    private ContactsView contactsView;

    // USECASES
    private JobManager jobManager;

    // SUBSCRIBERS

    @Inject
    public ContactsGridPresenter(JobManager jobManager) {
        super();
        this.jobManager = jobManager;
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
}
