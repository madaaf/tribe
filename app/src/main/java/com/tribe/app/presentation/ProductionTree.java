package com.tribe.app.presentation;

import android.util.Log;
import com.crashlytics.android.Crashlytics;
import timber.log.Timber;

/**
 * Created by tiago on 29/12/2016.
 */

public class ProductionTree extends Timber.Tree {

  public ProductionTree(AndroidApplication app) {

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

    //if (priority == Log.INFO) {
    //  try {
    //    Answers.getInstance().logCustom(new CustomEvent(message));
    //  } catch (Exception ex) {
    //
    //  }
    //}
  }
}
