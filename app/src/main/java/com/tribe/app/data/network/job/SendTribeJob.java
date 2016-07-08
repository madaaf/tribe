package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.SendTribe;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.scope.SerializableSerializedSubject;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 05/07/2016.
 */
public class SendTribeJob extends BaseJob {

    @Inject
    @Named("cloudSendTribe")
    transient SendTribe cloudSendTribe;

    @Inject
    @Named("diskDeleteTribe")
    transient DeleteTribe diskDeleteTribe;

    // VARIABLES
    private Tribe tribe;
    private transient SerializableSerializedSubject<Object, Object> bus;

    public SendTribeJob(Tribe tribe, Subscriber subscriber) {
        super(new Params(Priority.HIGH).requireNetwork().groupBy(tribe.getTo().getId()));
        this.tribe = tribe;
        this.bus = new SerializableSerializedSubject<>(PublishSubject.create());
        this.bus.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        cloudSendTribe.setTribe(tribe);
        diskDeleteTribe.setTribe(tribe);
        cloudSendTribe.execute(new TribeSendSubscriber());
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        diskDeleteTribe.execute(new TribeDeleteSubscriber());
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.RETRY;
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }

    private final class TribeSendSubscriber extends DefaultSubscriber<Tribe> {

        @Override
        public void onCompleted() {
            if (bus != null)
                bus.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Tribe tribe) {
            if (bus != null && bus.hasObservers()) bus.onNext(tribe);
        }
    }

    private final class TribeDeleteSubscriber extends DefaultSubscriber<Tribe> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Tribe tribe) {
            System.out.println("LOL END TRIBE DELETE");
        }
    }
}
