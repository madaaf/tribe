package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.mapper.ChatRealmDataMapper;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by tiago on 05/07/2016.
 */
public class UpdateTribeDownloadedJob extends BaseJob {

    @Inject
    TribeCache tribeCache;

    @Inject
    ChatRealmDataMapper chatRealmDataMapper;

    public UpdateTribeDownloadedJob() {
        super(new Params(Priority.HIGH).groupBy("update-tribe-downloaded").setSingleId("update-tribe-downloaded"));
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        List<TribeRealm> tribeRealmList = tribeCache.tribesDownloading();
        Map<String, List<Pair<String, Object>>> tribeUpdates = new HashMap<>();

        for (TribeRealm tribeRealm : tribeRealmList) {
            List<Pair<String, Object>> values = new ArrayList<>();
            values.add(Pair.create(TribeRealm.MESSAGE_DOWNLOADING_STATUS, MessageDownloadingStatus.STATUS_DOWNLOAD_ERROR));
            tribeUpdates.put(tribeRealm.getLocalId(), values);
        }

        tribeCache.update(tribeUpdates);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

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
}
