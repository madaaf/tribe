package com.tribe.app.data.network.job;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.mapper.TribeRealmDataMapper;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.SendTribe;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        super(new Params(Priority.HIGH).groupBy(tribe.getTo().getSubId()).setSingleId(tribe.getLocalId()));
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

        Map<String, List<Pair<String, Object>>> tribeUpdates = new HashMap<>();

        List<Pair<String, Object>> values = new ArrayList<>();

        if (tribeRealm.isToGroup() && tribeRealm.getMembershipRealm() != null) {
            values.add(Pair.create(TribeRealm.GROUP_ID_UPDATED_AT, tribeRealm.getRecordedAt()));
        } else if (!tribeRealm.isToGroup() && tribeRealm.getFriendshipRealm() != null) {
            values.add(Pair.create(TribeRealm.FRIEND_TO_ID_UPDATED_AT, tribeRealm.getRecordedAt()));
        }

        tribeUpdates.put(tribeRealm.getLocalId(), values);
        tribeCache.update(tribeUpdates);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        throwable.printStackTrace();
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
        Pair<String, Object> updatePair = Pair.create(ChatRealm.MESSAGE_SENDING_STATUS, status);
        tribeCache.update(tribeRealm.getLocalId(), updatePair);
    }

    private final class TribeSendSubscriber extends DefaultSubscriber<TribeMessage> {

        @Override
        public void onCompleted() {
            cloudSendTribe.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
            tagManager.trackEvent(TagManagerConstants.TRIBE_PENDING_ERROR);
            setStatus(MessageSendingStatus.STATUS_ERROR);
        }

        @Override
        public void onNext(TribeMessage tribe) {
            jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.SEND_RECEIVE_TRIBE, 1));
            setStatus(MessageSendingStatus.STATUS_SENT);
            Bundle bundle = new Bundle();
            bundle.putString(TagManagerConstants.TYPE, tribeRealm.isToGroup() ? TagManagerConstants.TYPE_TRIBE_GROUP : TagManagerConstants.TYPE_TRIBE_USER);
            tagManager.trackEvent(TagManagerConstants.KPI_TRIBES_SENT, bundle);
            tagManager.increment(TagManagerConstants.COUNT_TRIBES_SENT);
        }
    }
}
