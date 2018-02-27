package com.tribe.app.presentation.view.popup;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.tribe.app.presentation.view.notification.Alerter;
import com.tribe.app.presentation.view.popup.listener.PopupListener;
import com.tribe.app.presentation.view.popup.view.PopupParentView;
import com.tribe.app.presentation.view.popup.view.PopupView;
import java.lang.ref.WeakReference;
import rx.subscriptions.CompositeSubscription;

public final class PopupManager {

  private Popup popup;
  private static PopupParentView view;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  private PopupManager() {

  }

  public static class Popup {

    private int viewResource;
    private PopupView view;
    private PopupListener listener;
    private boolean dimBackground;
    private WeakReference<Activity> activityWR;
    private boolean autoDismiss;

    Popup(Builder builder) {
      this.view = builder.view;
      this.viewResource = builder.viewResource;
      this.listener = builder.listener;
      this.dimBackground = builder.dimBackground;
      this.activityWR = builder.activityWR;
      this.autoDismiss = builder.autoDismiss;
    }

    public int getViewResource() {
      return viewResource;
    }

    public PopupView getView() {
      return view;
    }

    public PopupListener getListener() {
      return listener;
    }

    public boolean isDimBackground() {
      return dimBackground;
    }

    public boolean isAutoDismiss() {
      return autoDismiss;
    }

    public WeakReference<Activity> getActivityWR() {
      return activityWR;
    }
  }

  public static class Builder {

    private PopupView view;
    private int viewResource;
    private PopupListener listener;
    private boolean dimBackground = false;
    private boolean autoDismiss = false;
    private WeakReference<Activity> activityWR;

    public Builder view(PopupView popupView) {
      this.view = popupView;
      return this;
    }

    public Builder viewResource(int viewResource) {
      this.viewResource = viewResource;
      return this;
    }

    public Builder listener(PopupListener listener) {
      this.listener = listener;
      return this;
    }

    public Builder dimBackground(boolean dimBackground) {
      this.dimBackground = dimBackground;
      return this;
    }

    public Builder activity(Activity activity) {
      this.activityWR = new WeakReference<>(activity);
      return this;
    }

    public Builder autoDismiss(boolean autoDismiss) {
      this.autoDismiss = autoDismiss;
      return this;
    }

    public Popup build() {
      return new Popup(this);
    }
  }

  public static boolean hasPopup() {
    return view != null;
  }

  public static PopupManager create(Builder builder) {
    if (builder == null || builder.activityWR == null) {
      throw new IllegalArgumentException("Activity cannot be null!");
    }

    final PopupManager popupManager = new PopupManager();

    // Clear Current popup, if one is Active
    PopupManager.clearCurrent();

    Popup popup = builder.build();
    if (popup.view == null) {
      popup.view =
          (PopupView) LayoutInflater.from(popup.activityWR.get()).inflate(popup.viewResource, null);
    }

    if (!(popup.view instanceof PopupView)) {
      throw new IllegalArgumentException("View is not instance of PopupView");
    }

    view = new PopupParentView(popup);
    popupManager.setPopup(popup);

    return popupManager;
  }

  private static void clearCurrent() {
    try {
      if (view == null || view.getWindowToken() == null) {
        Log.d(Alerter.class.getClass().getSimpleName(), "");
      } else {
        view.animate()
            .alpha(0)
            .withEndAction(() -> ((ViewGroup) view.getParent()).removeView(view))
            .start();
        Log.d(Alerter.class.getClass().getSimpleName(), "");
      }
    } catch (Exception ex) {
      Log.e(Alerter.class.getClass().getSimpleName(), Log.getStackTraceString(ex));
    }
  }

  public PopupParentView getView() {
    return view;
  }

  public void show() {
    if (popup.activityWR != null) {
      subscriptions.add(view.onDismiss().subscribe(aVoid -> {
        view = null;
        subscriptions.clear();
      }));

      popup.activityWR.get().runOnUiThread(() -> {
        view.setTouch();
        final ViewGroup decorView = getActivityDecorView();
        if (decorView != null && view.getParent() == null) {
          decorView.addView(view);
        }
      });
    }
  }

  private void setPopup(Popup popup) {
    this.popup = popup;
  }

  @Nullable private ViewGroup getActivityDecorView() {
    ViewGroup decorView = null;

    if (popup.activityWR != null && popup.activityWR.get() != null) {
      decorView = (ViewGroup) popup.activityWR.get().getWindow().getDecorView();
    }

    return decorView;
  }
}
