package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.RefreshHowManyFriendsJob;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.presentation.mvp.view.AccessView;
import com.tribe.app.presentation.mvp.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import rx.subscriptions.CompositeSubscription;

public class AccessPresenter implements Presenter {

    // VIEW ATTACHED
    private AccessView accessView;

    // USECASES
    private JobManager jobManager;
    private UseCase synchroContactList;

    // SUBSCRIBERS
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private LookupContactsSubscriber lookupContactsSubscriber;

    @Inject
    public AccessPresenter(JobManager jobManager,
            @Named("synchroContactList") UseCase synchroContactList) {
        super();
        this.jobManager = jobManager;
        this.synchroContactList = synchroContactList;
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
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        lookupContactsSubscriber.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        accessView = (AccessView) v;
    }

    public void lookupContacts() {
        if (lookupContactsSubscriber != null) lookupContactsSubscriber.unsubscribe();
        lookupContactsSubscriber = new LookupContactsSubscriber();
        synchroContactList.execute(lookupContactsSubscriber);
    }

    public void cancelLookupContacts() {
        synchroContactList.unsubscribe();
        lookupContactsSubscriber.unsubscribe();
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
            Map<String, User> friendList = new HashMap<>();

            for (Contact contact : contactList) {
                if (contact.getUserList() != null && contact.getUserList().size() > 0) {
                    for (User user : contact.getUserList()) {
                        if (!friendList.containsKey(user)) {
                            friendList.put(user.getId(), user);
                        }
                    }
                }
            }

            accessView.renderFriendList(new ArrayList<>(friendList.values()));
            jobManager.addJobInBackground(new RefreshHowManyFriendsJob());
        }
    }
}
