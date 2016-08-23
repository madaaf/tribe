package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.mapper.TribeRealmDataMapper;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.utils.MessageStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tiago on 05/07/2016.
 */
public class DownloadTribeJob extends BaseJob {

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
        ).setSingleId(tribe.getId()));

        this.tribe = tribe;
    }

    @Override
    public void onAdded() {
        TribeRealm tribeRealm = tribeRealmDataMapper.transform(tribe);
        tribeRealm.setMessageStatus(MessageStatus.STATUS_LOADING);
        tribeCache.update(tribeRealm);
    }

    @Override
    public void onRun() throws Throwable {
        File file = FileUtils.getFileEnd(tribe.getId());

        //if (file.exists() && file.length() > 0) throw new FileAlreadyExists();

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
                                tribeRealm.setMessageStatus(MessageStatus.STATUS_READY);
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
        tribeRealm.setMessageStatus(file.exists() && file.length() > 0 ? MessageStatus.STATUS_READY : MessageStatus.STATUS_RECEIVED);
        tribeCache.update(tribeRealm);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            File file = FileUtils.getFileEnd(tribe.getId());

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
}
