package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 15/07/2016.
 */
public class SynchroContactsJob extends BaseJob {

    private static final String TAG = "SynchroContactsJob";

    @Inject
    @Named("synchroContactList")
    UseCase synchroContactList;


    public SynchroContactsJob() {
        super(new Params(Priority.LOW).delayInMs(1000).requireNetwork().singleInstanceBy(TAG).groupBy(TAG));
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        synchroContactList.execute(new ContactListSubscriber());
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }

    private final class ContactListSubscriber extends DefaultSubscriber<List<Contact>> {

        @Override
        public void onCompleted() {
            if (synchroContactList != null) synchroContactList.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(List<Contact> contactList) {
        }
    }
}
