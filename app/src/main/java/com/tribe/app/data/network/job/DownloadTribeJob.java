package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import okhttp3.ResponseBody;

/**
 * Created by tiago on 05/07/2016.
 */
public class DownloadTribeJob extends BaseJob {

    private static final String TAG = "DownloadTribeJob";

    @Inject
    FileApi fileApi;

    // VARIABLES
    private Tribe tribe;

    public DownloadTribeJob(Tribe tribe) {
        super(new Params(Priority.MID).requireNetwork().groupBy(
                (tribe.isToGroup() ? tribe.getTo().getId() : tribe.getFrom().getId())
        ).setSingleId(tribe.getId()));

        this.tribe = tribe;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
//        File file = FileUtils.getFileEnd(tribe.getId());
//
//        if (file.exists() && file.length() > 0) throw new Exception("Already downloading");
//
//        Call<ResponseBody> call = fileApi.downloadFileWithUrl(tribe.getUrl());
//
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "server contacted and has file");
//
//                    boolean writtenToDisk = writeResponseBodyToDisk(response.body());
//
//                    Log.d(TAG, "file download was a success? " + writtenToDisk);
//                } else {
//                    Log.d(TAG, "server contact failed");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Log.e(TAG, "error");
//            }
//        });
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        System.out.println("Cancel Reason : " + cancelReason);
        throwable.printStackTrace();
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        System.out.println("Cancel Reason : " + throwable.getMessage());
        return RetryConstraint.RETRY;
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
