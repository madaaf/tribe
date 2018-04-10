package com.tribe.app.presentation.view.popup;

import android.app.Activity;
import android.view.LayoutInflater;
import com.tribe.app.presentation.view.popup.listener.PopupListener;
import com.tribe.app.presentation.view.popup.view.PopupParentView;
import com.tribe.app.presentation.view.popup.view.PopupView;
import java.lang.ref.WeakReference;
import rx.subscriptions.CompositeSubscription;

public final class PopupManager {

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

  public static PopupParentView create(Builder builder) {
    if (builder == null || builder.activityWR == null) {
      throw new IllegalArgumentException("Activity cannot be null!");
    }

    Popup popup = builder.build();
    if (popup.view == null) {
      popup.view =
          (PopupView) LayoutInflater.from(popup.activityWR.get()).inflate(popup.viewResource, null);
    }

    if (!(popup.view instanceof PopupView)) {
      throw new IllegalArgumentException("View is not instance of PopupView");
    }

    PopupParentView view = new PopupParentView(popup);

    return view;
  }
}
