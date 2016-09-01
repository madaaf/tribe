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
<<<<<<< HEAD
        TribeRealm tribeRealm = tribeRealmDataMapper.transform(tribe);
        tribeRealm.setMessageDownloadingStatus(MessageDownloadingStatus.STATUS_DOWNLOADING);
        tribeCache.update(tribeRealm);
    }

    @Override
    public void onRun() throws Throwable {
        File file = FileUtils.getFileEnd(tribe.getId());

        if (file.exists() && file.length() > 0) throw new FileAlreadyExists();

        Call<ResponseBody> call = fileApi.downloadFileWithUrl(tribe.getContent());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Observable.just("")
                            .doOnNext(s -> {
                                Log.d(TAG, "server contacted and has file");
                                boolean writtenToDisk = writeResponseBodyToDisk(response.body());
                                Log.d(TAG, "file download was a success? " + writtenToDisk);

                                TribeRealm tribeRealm = tribeRealmDataMapper.transform(tribe);
                                tribeRealm.setMessageDownloadingStatus(writtenToDisk ? MessageDownloadingStatus.STATUS_DOWNLOADED : MessageDownloadingStatus.STATUS_TO_DOWNLOAD);
                                tribeCache.update(tribeRealm);
                            })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(s -> {});
                } else {
                    Log.d(TAG, "server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "error : " + t.getMessage());
            }
        });
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        File file = FileUtils.getFileEnd(tribe.getId());

        TribeRealm tribeRealm = tribeRealmDataMapper.transform(tribe);
        tribeRealm.setMessageDownloadingStatus(file.exists() && file.length() > 0 ? MessageDownloadingStatus.STATUS_DOWNLOADED : MessageDownloadingStatus.STATUS_TO_DOWNLOAD);

        tribeCache.update(tribeRealm);
=======
        setStatus(MessageDownloadingStatus.STATUS_DOWNLOADING);
>>>>>>> 38493649d184eaa26b8dfa06f73cb247ba524768
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
