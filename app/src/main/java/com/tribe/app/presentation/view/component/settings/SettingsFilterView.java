package com.tribe.app.presentation.view.component.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.f2prateek.rx.preferences.Preference;
import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.preferences.Filter;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.widget.CameraWrapper;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class SettingsFilterView extends FrameLayout {

  @BindView(R.id.imgFilterUnderline) ImageView imgFilterUnderline;

  @BindView(R.id.imageFilter1) ImageView imageFilter1;

  @BindView(R.id.imageFilter2) ImageView imageFilter2;

  @BindView(R.id.imageFilter3) ImageView imageFilter3;

  @BindView(R.id.imageFilter4) ImageView imageFilter4;

  @BindView(R.id.cameraWrapper) CameraWrapper cameraWrapper;

  @Inject @Filter Preference<Integer> filter;

  @Inject TagManager tagManager;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onChangeFilter = PublishSubject.create();

  public SettingsFilterView(Context context) {
    super(context);
  }

  public SettingsFilterView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SettingsFilterView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public SettingsFilterView(Context context, AttributeSet attrs, int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    LayoutInflater.from(getContext()).inflate(R.layout.view_settings_filter, this);
    unbinder = ButterKnife.bind(this);

    initUi();
  }

  private void initUi() {
    initDependencyInjector();

    imageFilter1.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            switch (filter.get()) {
              case 0:
                setUpUnderline(imageFilter1, false);
                break;

              case 1:
                setUpUnderline(imageFilter2, false);
                break;

              case 2:
                setUpUnderline(imageFilter3, false);
                break;

              case 3:
                setUpUnderline(imageFilter4, false);
                break;

              default:
                break;
            }

            imageFilter1.getViewTreeObserver().removeOnGlobalLayoutListener(this);
          }
        });

    subscriptions.add(RxView.clicks(imageFilter1).subscribe(aVoid -> {
      setUpUnderline(imageFilter1, true);
      filter.set(0);
      updateFilter();
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerConstants.FILTER_ENABLED, TagManagerConstants.FILTER_NONE);
      tagManager.setProperty(bundle);
    }));

    subscriptions.add(RxView.clicks(imageFilter2).subscribe(aVoid -> {
      setUpUnderline(imageFilter2, true);
      filter.set(1);
      updateFilter();
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerConstants.FILTER_ENABLED, TagManagerConstants.FILTER_TAN);
      tagManager.setProperty(bundle);
    }));

    subscriptions.add(RxView.clicks(imageFilter3).subscribe(aVoid -> {
      setUpUnderline(imageFilter3, true);
      filter.set(2);
      updateFilter();
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerConstants.FILTER_ENABLED, TagManagerConstants.FILTER_BLACK_WHITE);
      tagManager.setProperty(bundle);
    }));

    subscriptions.add(RxView.clicks(imageFilter4).subscribe(aVoid -> {
      setUpUnderline(imageFilter4, true);
      filter.set(3);
      updateFilter();
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerConstants.FILTER_ENABLED, TagManagerConstants.FILTER_PIXEL);
      tagManager.setProperty(bundle);
    }));
  }

  private void setUpUnderline(ImageView imageView, boolean animate) {
    int location[] = new int[2];
    imageView.getLocationOnScreen(location);

    if (animate) {
      imgFilterUnderline.animate()
          .x(location[0])
          .setDuration(300)
          .setInterpolator(new OvershootInterpolator(0.75f))
          .start();
    } else {
      imgFilterUnderline.setX(location[0]);
    }
  }

  public void onResume() {
    subscriptions.add(Observable.
        from(PermissionUtils.PERMISSIONS_CAMERA)
        .map(permission -> RxPermissions.getInstance(getContext()).isGranted(permission))
        .toList()
        .subscribe(grantedList -> {
          boolean areAllGranted = true;

          for (Boolean granted : grantedList) {
            if (!granted) areAllGranted = false;
          }

          if (areAllGranted) {
            cameraWrapper.onResume(false);
          } else {
            cameraWrapper.showPermissions();
          }
        }));
  }

  public void onPause() {
    if (cameraWrapper != null) cameraWrapper.onPause();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    if (unbinder != null) unbinder.unbind();

    if (subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
      subscriptions.clear();
    }

    super.onDetachedFromWindow();
  }

  public void updateFilter() {
    cameraWrapper.updateFilter();
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }
}
