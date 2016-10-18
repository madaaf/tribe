package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.birbit.android.jobqueue.Params;
import com.tribe.app.data.network.exception.FileAlreadyExists;
import com.tribe.app.data.network.util.DownloadProgressListener;
import com.tribe.app.data.network.util.DownloadProgressResponseBody;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

/**
 * Created by tiago on 08/31/2016.
 */
public abstract class DownloadVideoJob extends BaseJob {

    @Inject
    @Named("tribeApiOKHttp")
    OkHttpClient okHttpClient;

    public DownloadVideoJob(Params params) {
        super(params);
    }

    @Override
    public void onRun() throws Throwable {
        File file = FileUtils.getFile(getApplicationContext(), getFileId(), FileUtils.VIDEO);

        if (file.exists() && file.length() > 0) throw new FileAlreadyExists();
        System.out.println("THREAD : " + Thread.currentThread().getName());

        Request request = new Request.Builder().url(getUrl()).build();
        Response response = okHttpClient.newCall(request).execute();

        BufferedSink sink = null;
        try  {
            sink = Okio.buffer(Okio.sink(file));
            final DownloadProgressResponseBody body = new DownloadProgressResponseBody(response.body(), new DownloadProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    setProgress(bytesRead, contentLength);
                }
            });
            sink.writeAll(body.source());
            saveResult(true);
            body.close();
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (sink != null) sink.close();
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        System.out.println("CANCEL");
        File file = FileUtils.getFile(getApplicationContext(), getFileId(), FileUtils.VIDEO);
        setStatus(file.exists() && file.length() > 0 ? MessageDownloadingStatus.STATUS_DOWNLOADED : MessageDownloadingStatus.STATUS_TO_DOWNLOAD);
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
    }

    protected abstract String getFileId();
    protected abstract String getUrl();
    protected abstract String getTag();
    protected abstract void saveResult(boolean writtenToDisk);
    protected abstract void setStatus(@MessageDownloadingStatus.Status String status);
    protected abstract void setProgress(long progress, long totalSize);
    protected abstract void update(Pair<String, Object>... valuesToUpdate);
}
