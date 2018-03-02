package com.tribe.app.presentation.utils.unzip;

import android.content.Context;
import com.tribe.app.presentation.utils.FileUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.lingala.zip4j.core.ZipFile;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

@Singleton public class RxUnzip {

  private Context context;

  @Inject public RxUnzip(Context context) {
    this.context = context;
  }

  private Observable<String> unzipPathDone;

  /**
   * Run ContentResolver query and emit results to the Observable
   */
  public Observable<String> unzip(String path) {
    if (unzipPathDone == null) {
      unzipPathDone =
          Observable.create((Subscriber<? super String> subscriber) -> unzip(subscriber, path))
              .onBackpressureBuffer()
              .serialize();
    }

    return unzipPathDone;
  }

  private void unzip(Subscriber<? super String> subscriber, String path) {
    try {
      File unzippedDir = FileUtils.getGameUnzippedDir(context);

      Timber.d("Starting to unzip");

      String password =
          "ADCJDA7AvSutxFM5JELv7nFrY638wDJHabm9Kj2h6mqyBrhnvtkAYsxzyhDG4RaF3GQV564XLxaPp6M8PnQ5R3apmBZ3j9LtgHCGanAJcubZymv2dbq7NY6h7cm3RPLx";
      ZipFile zipFile = new ZipFile(path);
      if (zipFile.isEncrypted()) {
        zipFile.setPassword(password);
      }
      zipFile.extractAll(unzippedDir.getAbsolutePath());

      if (!subscriber.isUnsubscribed()) {
        subscriber.onNext(unzippedDir.getAbsolutePath());
        subscriber.onCompleted();
      }

      Timber.d("Finished unzip");
    } catch (Exception e) {
      Timber.e("Unzip Error", e);
      subscriber.onError(e);
      subscriber.onCompleted();
    }
  }
}
