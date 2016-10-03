package com.tribe.app.data.network.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.tribe.CloudMarkTribeListAsRead;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.MessageReceivingStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 05/07/2016.
 */
public class MarkTribeListAsReadJob extends BaseJob {

    @Inject
    @Named("cloudMarkTribeListAsRead")
    CloudMarkTribeListAsRead cloudMarkTribeListAsRead;

    @Inject
    TribeCache tribeCache;

    // VARIABLES
    private List<TribeMessage> tribeList;

    public MarkTribeListAsReadJob(Recipient recipient, List<TribeMessage> tribeList) {
        super(new Params(Priority.HIGH).persist().groupBy(recipient.getSubId()));
        this.tribeList = tribeList;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        Map<String, List<Pair<String, Object>>> tribeUpdates = new HashMap<>();

        for (TribeMessage tribeMessage : tribeList) {
            List<Pair<String, Object>> values = new ArrayList<>();
            values.add(Pair.create(TribeRealm.MESSAGE_RECEIVING_STATUS, MessageReceivingStatus.STATUS_SEEN));
            tribeUpdates.put(tribeMessage.getLocalId(), values);
        }

        tribeCache.update(tribeUpdates);

        cloudMarkTribeListAsRead.setTribeList(tribeList);
        cloudMarkTribeListAsRead.execute(new MarkTribeListAsReadSubscriber());
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

    private final class MarkTribeListAsReadSubscriber extends DefaultSubscriber<List<TribeMessage>> {

        @Override
        public void onCompleted() {
            if (cloudMarkTribeListAsRead != null) cloudMarkTribeListAsRead.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(List<TribeMessage> tribeList) {

        }
    }
}
