package com.tribe.app.data.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.network.util.DownloadProgressListener;
import com.tribe.app.data.network.util.DownloadProgressResponseBody;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;
import rx.subjects.AsyncSubject;

/**
 * Created by tiago on 16/10/2016.
 */
@Singleton
public class DownloadTribeService extends Service {

    private static final String RECIPIENT_ID = "RECIPIENT_ID";

    public static Intent getCallingIntent(Context context, String recipientId) {
        Intent intent = new Intent(context, DownloadTribeService.class);
        if (!StringUtils.isEmpty(recipientId)) intent.putExtra(RECIPIENT_ID, recipientId);
        return intent;
    }

    @Inject
    TribeCache tribeCache;

    @Inject
    @Named("tribeApiOKHttp")
    OkHttpClient okHttpClient;

    // VARIABLES
    private String recipientId;
    private Subscription subscription;
    private Scheduler scheduler;
    private List<String> alreadyProcessed = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        scheduler = Schedulers.from(Executors.newSingleThreadExecutor());

        initDependencyInjection();
    }

    @Override
    public void onDestroy() {
        if (subscription != null) subscription.unsubscribe();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (subscription != null) subscription.unsubscribe();
        if (intent != null && intent.hasExtra(RECIPIENT_ID)) recipientId = intent.getStringExtra(RECIPIENT_ID);
        handleStart();
        return Service.START_STICKY;
    }

    private void handleStart() {
        subscription = tribeCache
                .tribesToDownload(recipientId)
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(tribeRealmList -> !tribeRealmList.isEmpty())
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(tribeList -> {
                    List<TribeRealm> tribeRealmEnd = new ArrayList<>();
                    Map<String, List<Pair<String, Object>>> tribeUpdates = new HashMap<>();

                    for (TribeRealm tribeRealm : tribeList) {
                        if (!alreadyProcessed.contains(tribeRealm.getId())) {
                            alreadyProcessed.add(tribeRealm.getId());

                            List<Pair<String, Object>> values = new ArrayList<>();
                            values.add(Pair.create(TribeRealm.MESSAGE_DOWNLOADING_STATUS, MessageDownloadingStatus.STATUS_DOWNLOADING));
                            tribeUpdates.put(tribeRealm.getLocalId(), values);
                            tribeRealmEnd.add(tribeRealm);
                        }
                    }

                    if (tribeUpdates.size() > 0) tribeCache.update(tribeUpdates);
                    return tribeRealmEnd;
                })
                .onBackpressureBuffer()
                .observeOn(scheduler)
                .flatMap(tribeList -> Observable.from(tribeList))
                .flatMap(tribeRealm -> {
                    File file = FileUtils.getFileTemp(getApplicationContext(), tribeRealm.getId(), FileUtils.VIDEO);
                    File fileReal = FileUtils.getFile(getApplicationContext(), tribeRealm.getId(), FileUtils.VIDEO);

                    if (fileReal.exists()) {
                        return Observable.just(file);
                    }

                    if (file.exists()) file.delete();

                    Request request = new Request.Builder().url(tribeRealm.getUrl()).build();

                    final ObservableCallback callback = new ObservableCallback();
                    okHttpClient.newCall(request).enqueue(callback);

                    return callback.getObservable().map(response -> {
                        BufferedSink sink = null;
                        DownloadProgressResponseBody body = null;
                        try  {
                            sink = Okio.buffer(Okio.sink(file));
                            body = new DownloadProgressResponseBody(response.body(), new DownloadProgressListener() {
                                @Override
                                public void update(long bytesRead, long contentLength, boolean done) {
                                    //setProgress(tribeRealm.getLocalId(), bytesRead, contentLength);
                                }
                            });
                            sink.writeAll(body.source());
                            body.close();
                        } catch (IOException io) {
                            io.printStackTrace();
                        } finally {
                            if (sink != null) try {
                                sink.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (body != null) body.close();
                        }

                        return file;
                    }).onErrorResumeNext(throwable -> {
                        computeEndStatus(tribeRealm, MessageDownloadingStatus.STATUS_DOWNLOAD_ERROR);
                        return Observable.empty();
                    });
                }, (tribeRealm, file) -> {
                    if (file != null && file.exists() && file.length() > 0) {
                        FileUtils.copyFile(file.getAbsolutePath(), FileUtils.getFile(getApplicationContext(), tribeRealm.getId(), FileUtils.VIDEO).getAbsolutePath());
                        file.delete();
                    }

                    computeEndStatus(tribeRealm, MessageDownloadingStatus.STATUS_DOWNLOADED);

                    return tribeRealm;
                }, 1)
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void computeEndStatus(TribeRealm tribeRealm, @MessageDownloadingStatus.Status String status) {
        Pair<String, Object> updatePair = Pair.create(TribeRealm.MESSAGE_DOWNLOADING_STATUS, status);
        tribeCache.update(tribeRealm.getId(), updatePair);

        alreadyProcessed.remove(tribeRealm.getLocalId());
    }

    protected void setStatus(String id, @MessageDownloadingStatus.Status String status) {
        Pair<String, Object> updatePair = Pair.create(TribeRealm.MESSAGE_DOWNLOADING_STATUS, status);
        update(id, updatePair);
    }

    protected void setProgress(String id, long progress, long totalSize) {
        Pair<String, Object> updatePair = Pair.create(TribeRealm.PROGRESS, progress);
        Pair<String, Object> updatePairTotalSize = Pair.create(TribeRealm.TOTAL_SIZE, totalSize);
        update(id, updatePair, updatePairTotalSize);
    }

    protected void update(String id, Pair<String, Object>... valuesToUpdate) {
        tribeCache.update(id, valuesToUpdate);
    }

    private static class ObservableCallback implements Callback {
        private final AsyncSubject<Response> subject = AsyncSubject.create();

        public Observable<Response> getObservable() {
            return subject.asObservable();
        }

        @Override
        public void onFailure(Call call, IOException e) {
            subject.onError(OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(e, call)));
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            subject.onNext(response);
            subject.onCompleted();
        }
    }

    private void initDependencyInjection() {
        ((AndroidApplication) getApplication()).getApplicationComponent().inject(this);
    }
}
