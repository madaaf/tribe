package com.tribe.app.data.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Pair;
import com.tribe.app.data.cache.GameCache;
import com.tribe.app.data.network.util.DownloadProgressResponseBody;
import com.tribe.app.data.realm.GameFileRealm;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FileUtils;
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
import timber.log.Timber;

/**
 * Created by tiago on 03/01/2018.
 */
@Singleton public class DownloadGamesService extends Service {

  public static Intent getCallingIntent(Context context) {
    Intent intent = new Intent(context, DownloadGamesService.class);
    return intent;
  }

  @Inject GameCache gameCache;

  @Inject @Named("tribeApiOKHttp") OkHttpClient okHttpClient;

  // VARIABLES
  private Subscription subscription;
  private Scheduler scheduler;
  private List<String> alreadyProcessed = new ArrayList<>();

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();

    scheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    initDependencyInjection();
  }

  @Override public void onDestroy() {
    if (subscription != null) subscription.unsubscribe();
    super.onDestroy();
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (subscription != null) subscription.unsubscribe();
    handleStart();
    return Service.START_STICKY;
  }

  private void handleStart() {
    subscription = gameCache.getFilesToDownload()
        .debounce(500, TimeUnit.MILLISECONDS)
        .filter(tribeRealmList -> !tribeRealmList.isEmpty())
        .subscribeOn(AndroidSchedulers.mainThread())
        .map(gameFileList -> {
          List<GameFileRealm> gameFileEnd = new ArrayList<>();
          Map<String, List<Pair<String, Object>>> gameFileUpdates = new HashMap<>();

          for (GameFileRealm gameFileRealm : gameFileList) {
            if (!alreadyProcessed.contains(gameFileRealm.getUrl())) {
              alreadyProcessed.add(gameFileRealm.getUrl());
              List<Pair<String, Object>> values = new ArrayList<>();
              values.add(
                  Pair.create(GameFileRealm.DOWNLOAD_STATUS, GameFileRealm.STATUS_DOWNLOADING));
              gameFileUpdates.put(gameFileRealm.getUrl(), values);
              gameFileEnd.add(gameFileRealm);
            }
          }

          if (gameFileUpdates.size() > 0) gameCache.updateGameFiles(gameFileUpdates);
          return gameFileEnd;
        })
        .onBackpressureBuffer()
        .observeOn(scheduler)
        .flatMap(gameFileList -> Observable.from(gameFileList))
        .flatMap(gameFileRealm -> {
          File file = FileUtils.getGameFileTemp(getApplicationContext(), gameFileRealm.getGameId(),
              FileUtils.ZIP);
          File fileReal = FileUtils.getGameFile(getApplicationContext(), gameFileRealm.getGameId(),
              FileUtils.ZIP);

          if (fileReal.exists()) {
            fileReal.delete();
          }

          if (file.exists()) file.delete();

          Request request = new Request.Builder().url(gameFileRealm.getUrl()).build();

          final ObservableCallback callback = new ObservableCallback();
          okHttpClient.newCall(request).enqueue(callback);

          return callback.getObservable().map(response -> {
            BufferedSink sink = null;
            DownloadProgressResponseBody body = null;
            try {
              sink = Okio.buffer(Okio.sink(file));
              body = new DownloadProgressResponseBody(response.body(),
                  (bytesRead, contentLength, done) -> setProgress(gameFileRealm.getUrl(), bytesRead,
                      contentLength));
              sink.writeAll(body.source());
              body.close();
            } catch (IOException io) {
              io.printStackTrace();
            } finally {
              if (sink != null) {
                try {
                  sink.close();
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }

              if (body != null) body.close();
            }

            return file;
          }).onErrorResumeNext(throwable -> {
            computeEndStatus(gameFileRealm, GameFileRealm.STATUS_PENDING);
            return Observable.empty();
          });
        }, (gameFileRealm, file) -> {
          File finalFile = null;
          if (file != null && file.exists() && file.length() > 0) {
            finalFile = FileUtils.getGameFile(getApplicationContext(), gameFileRealm.getGameId(),
                FileUtils.ZIP);
            FileUtils.copyFile(file.getAbsolutePath(), finalFile.getAbsolutePath());
            Timber.d("DOWNLOADED");
            file.delete();
          }

          setPath(gameFileRealm.getUrl(), finalFile.getAbsolutePath());
          computeEndStatus(gameFileRealm, GameFileRealm.STATUS_DOWNLOADED);

          return gameFileRealm;
        }, 1)
        .toList()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();
  }

  private void computeEndStatus(GameFileRealm gameFileRealm,
      @GameFileRealm.DownloadStatus String status) {
    List<Pair<String, Object>> pairList = new ArrayList<>();
    pairList.add(Pair.create(GameFileRealm.DOWNLOAD_STATUS, status));
    gameCache.updateGameFiles(gameFileRealm.getUrl(), pairList);

    alreadyProcessed.remove(gameFileRealm.getUrl());
  }

  protected void setPath(String url, String path) {
    List<Pair<String, Object>> pairList = new ArrayList<>();
    Pair<String, Object> updatePair = Pair.create(GameFileRealm.PATH, path);
    pairList.add(updatePair);
    update(url, pairList);
  }

  protected void setStatus(String url, @GameFileRealm.DownloadStatus String status) {
    List<Pair<String, Object>> pairList = new ArrayList<>();
    Pair<String, Object> updatePair = Pair.create(GameFileRealm.DOWNLOAD_STATUS, status);
    pairList.add(updatePair);
    update(url, pairList);
  }

  protected void setProgress(String url, long progress, long totalSize) {
    List<Pair<String, Object>> pairList = new ArrayList<>();
    Pair<String, Object> updatePair = Pair.create(GameFileRealm.PROGRESS, progress);
    Pair<String, Object> updatePairTotalSize = Pair.create(GameFileRealm.TOTAL_SIZE, totalSize);
    pairList.add(updatePair);
    pairList.add(updatePairTotalSize);
    update(url, pairList);
  }

  protected void update(String url, List<Pair<String, Object>> valuesToUpdate) {
    gameCache.updateGameFiles(url, valuesToUpdate);
  }

  private static class ObservableCallback implements Callback {
    private final AsyncSubject<Response> subject = AsyncSubject.create();

    public Observable<Response> getObservable() {
      return subject.asObservable();
    }

    @Override public void onFailure(Call call, IOException e) {
      subject.onError(OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(e, call)));
    }

    @Override public void onResponse(Call call, Response response) throws IOException {
      subject.onNext(response);
      subject.onCompleted();
    }
  }

  private void initDependencyInjection() {
    ((AndroidApplication) getApplication()).getApplicationComponent().inject(this);
  }
}