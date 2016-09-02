package com.tribe.app.data.network.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.mapper.TribeRealmDataMapper;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.SendTribe;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 05/07/2016.
 */
public class  SendTribeJob extends BaseJob {

    @Inject
    @Named("cloudSendTribe")
    SendTribe cloudSendTribe;

    @Inject
    @Named("diskDeleteTribe")
    DeleteTribe diskDeleteTribe;

    @Inject
    TribeCache tribeCache;

    @Inject
    TribeRealmDataMapper tribeRealmDataMapper;

    // VARIABLES
    private TribeMessage tribe;
    private TribeRealm tribeRealm;

    public SendTribeJob(TribeMessage tribe) {
        super(new Params(Priority.HIGH).groupBy(tribe.getTo().getId()).setSingleId(tribe.getLocalId()));
        this.tribe = tribe;
    }

    @Override
    public void onAdded() {
        tribeRealm = tribeRealmDataMapper.transform(tribe);
        setStatus(MessageSendingStatus.STATUS_PENDING);
    }

    @Override
    public void onRun() throws Throwable {
        cloudSendTribe.setTribe(tribe);
        cloudSendTribe.execute(new TribeSendSubscriber());
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }

    protected void setStatus(@MessageSendingStatus.Status String status) {
        tribeRealm.setMessageSendingStatus(status);
        tribeCache.update(tribeRealm);
    }

    private final class TribeSendSubscriber extends DefaultSubscriber<TribeMessage> {

        @Override
        public void onCompleted() {
            cloudSendTribe.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
            setStatus(MessageSendingStatus.STATUS_ERROR);
        }

        @Override
        public void onNext(TribeMessage tribe) {
            setStatus(MessageSendingStatus.STATUS_SENT);
        }
    }
}
