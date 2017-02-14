package com.tribe.app.presentation;

import com.tribe.app.domain.executor.PostExecutionThread;
import java.io.Serializable;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;

/**
 * MainThread (UI Thread) implementation based on a {@link rx.Scheduler}
 * which will execute actions on the Android UI thread
 */
@Singleton public class UIThread implements PostExecutionThread, Serializable {

  @Inject public UIThread() {
  }

  @Override public Scheduler getScheduler() {
    return AndroidSchedulers.mainThread();
  }
}
