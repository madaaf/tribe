package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.mapper.ChatRealmDataMapper;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;

import java.io.File;
import java.util.List;

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
        List<TribeRealm> tribeRealmList = tribeCache.tribesNotSeenNoObs(null);

        for (TribeRealm tribeRealm : tribeRealmList) {
            File file = FileUtils.getFileEnd(tribeRealm.getId());
            if (file.exists() && file.length() > 0) tribeRealm.setMessageDownloadingStatus(MessageDownloadingStatus.STATUS_DOWNLOADED);
        }

        tribeCache.put(tribeRealmList);
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
