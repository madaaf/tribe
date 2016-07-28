package com.tribe.app.data.network.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.mapper.TribeRealmDataMapper;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.SendTribe;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.MessageStatus;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 05/07/2016.
 */
public class SendTribeJob extends BaseJob {

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
    private Tribe tribe;

    public SendTribeJob(Tribe tribe) {
        super(new Params(Priority.HIGH).groupBy(tribe.getTo().getId()));
        this.tribe = tribe;
    }

    @Override
    public void onAdded() {

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
        return null;
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }

    private final class TribeSendSubscriber extends DefaultSubscriber<Tribe> {

        @Override
        public void onCompleted() {
            cloudSendTribe.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
            TribeRealm tribeRealm = tribeRealmDataMapper.transform(tribe);
            tribeRealm.setMessageStatus(MessageStatus.STATUS_ERROR);
            tribeCache.update(tribeRealm);
        }

        @Override
        public void onNext(Tribe tribe) {
            TribeRealm tribeRealm = tribeRealmDataMapper.transform(tribe);
            tribeRealm.setMessageStatus(MessageStatus.STATUS_SENT);
            tribeCache.update(tribeRealm);
        }
    }
}
