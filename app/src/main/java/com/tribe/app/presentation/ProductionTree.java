package com.tribe.app.presentation;

import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * Created by tiago on 29/12/2016.
 */

public class ProductionTree extends Timber.Tree {

  public ProductionTree(AndroidApplication app) {
    Fabric.with(app, new Crashlytics(), new Answers());
  }

  @Override protected void log(int priority, String tag, String message, Throwable t) {
    if (priority == Log.VERBOSE || priority == Log.DEBUG) return;

    Crashlytics.log(message);

    if (t != null) {
      Crashlytics.logException(t);
    }

    if (priority > Log.WARN) {
      Crashlytics.logException(new Throwable(message));
    }

    if (priority == Log.INFO) {
      Answers.getInstance().logCustom(new CustomEvent(message));
    }
  }
}
