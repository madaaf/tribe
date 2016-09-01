package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Params;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.data.network.exception.FileAlreadyExists;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;

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
 * Created by tiago on 08/31/2016.
 */
public abstract class DownloadVideoJob extends BaseJob {

    @Inject
    FileApi fileApi;

    public DownloadVideoJob(Params params) {
        super(params);
    }

    @Override
    public void onRun() throws Throwable {
        File file = FileUtils.getFileEnd(getFileId());

        if (file.exists() && file.length() > 0) throw new FileAlreadyExists();

        Call<ResponseBody> call = fileApi.downloadFileWithUrl(getUrl());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Observable.just("")
                            .doOnNext(s -> {
                                Log.d(getTag(), "server contacted and has file");
                                boolean writtenToDisk = writeResponseBodyToDisk(response.body());
                                Log.d(getTag(), "file download was a success? " + writtenToDisk);
                                saveResult(writtenToDisk);
                            })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(s -> {});
                } else {
                    Log.d(getTag(), "server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(getTag(), "error : " + t.getMessage());
            }
        });
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        File file = FileUtils.getFileEnd(getFileId());
        setStatus(file.exists() && file.length() > 0 ? MessageDownloadingStatus.STATUS_DOWNLOADED : MessageDownloadingStatus.STATUS_TO_DOWNLOAD);
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            File file = FileUtils.getFileEnd(getFileId());

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
                    Log.d(getTag(), "file download: " + fileSizeDownloaded + " of " + fileSize);
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

    protected abstract String getFileId();
    protected abstract String getUrl();
    protected abstract String getTag();
    protected abstract void saveResult(boolean writtenToDisk);
    protected abstract void setStatus(@MessageDownloadingStatus.Status String status);
}
