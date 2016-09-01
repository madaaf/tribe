package com.tribe.app.data.network.job;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.mapper.TribeRealmDataMapper;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;

import javax.inject.Inject;

/**
 * Created by tiago on 05/07/2016.
 */
public class DownloadTribeJob extends DownloadVideoJob {

    private static final String TAG = "DownloadTribeJob";

    @Inject
    FileApi fileApi;

    @Inject
    TribeCache tribeCache;

    @Inject
    TribeRealmDataMapper tribeRealmDataMapper;

    // VARIABLES
    private TribeMessage tribe;

    public DownloadTribeJob(TribeMessage tribe) {
        super(new Params(Priority.HIGH).requireNetwork().groupBy(
                (tribe.isToGroup() ? tribe.getTo().getId() : tribe.getFrom().getId())
        ).setSingleId(tribe.getId()).addTags(tribe.getId()));

        this.tribe = tribe;
    }

    @Override
    public void onAdded() {
        setStatus(MessageDownloadingStatus.STATUS_DOWNLOADING);
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

    @Override
    protected String getFileId() {
        return tribe.getId();
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected String getUrl() {
        return tribe.getContent();
    }

    @Override
    protected void saveResult(boolean writtenToDisk) {
        setStatus(writtenToDisk ? MessageDownloadingStatus.STATUS_DOWNLOADED : MessageDownloadingStatus.STATUS_TO_DOWNLOAD);
    }

    @Override
    protected void setStatus(@MessageDownloadingStatus.Status String status) {
        TribeRealm tribeRealm = tribeRealmDataMapper.transform(tribe);
        tribeRealm.setMessageDownloadingStatus(status);
        tribeCache.update(tribeRealm);
    }
}
