package com.tribe.app.presentation.view.component.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.f2prateek.rx.preferences.Preference;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.preferences.Theme;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class SettingsThemeView extends FrameLayout {

  @BindView(R.id.imgThemeUnderline) ImageView imgThemeUnderline;

  @BindView(R.id.imageTheme1) ImageView imageTheme1;

  @BindView(R.id.imageTheme2) ImageView imageTheme2;

  @BindView(R.id.imageTheme3) ImageView imageTheme3;

  @BindView(R.id.imageTheme4) ImageView imageTheme4;

  @BindView(R.id.imageTheme5) ImageView imageTheme5;

  @BindView(R.id.imageTheme6) ImageView imageTheme6;

  @BindView(R.id.layoutTheme) LinearLayout layoutTheme;

  @Inject @Theme Preference<Integer> theme;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public SettingsThemeView(Context context) {
    super(context);
  }

  public SettingsThemeView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SettingsThemeView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public SettingsThemeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    LayoutInflater.from(getContext()).inflate(R.layout.view_settings_theme, this);
    unbinder = ButterKnife.bind(this);

    initUi();
  }

  private void initUi() {
    initDependencyInjector();

    imageTheme1.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            switch (theme.get()) {
              case 0:
                setUpUnderline(imageTheme1, false);
                break;

              case 1:
                setUpUnderline(imageTheme2, false);
                break;

              case 2:
                setUpUnderline(imageTheme3, false);
                break;

              case 3:
                setUpUnderline(imageTheme4, false);
                break;

              case 4:
                setUpUnderline(imageTheme5, false);
                break;

              case 5:
                setUpUnderline(imageTheme6, false);
                break;
              default:
                break;
            }

            imageTheme1.getViewTreeObserver().removeOnGlobalLayoutListener(this);
          }
        });

    subscriptions.add(RxView.clicks(imageTheme1).subscribe(aVoid -> {
      setUpUnderline(imageTheme1, true);
      theme.set(0);
    }));

    subscriptions.add(RxView.clicks(imageTheme2).subscribe(aVoid -> {
      setUpUnderline(imageTheme2, true);
      theme.set(1);
    }));

    subscriptions.add(RxView.clicks(imageTheme3).subscribe(aVoid -> {
      setUpUnderline(imageTheme3, true);
      theme.set(2);
    }));

    subscriptions.add(RxView.clicks(imageTheme4).subscribe(aVoid -> {
      setUpUnderline(imageTheme4, true);
      theme.set(3);
    }));

    subscriptions.add(RxView.clicks(imageTheme5).subscribe(aVoid -> {
      setUpUnderline(imageTheme5, true);
      theme.set(4);
    }));

    subscriptions.add(RxView.clicks(imageTheme6).subscribe(aVoid -> {
      setUpUnderline(imageTheme6, true);
      theme.set(5);
    }));
  }

  private void setUpUnderline(ImageView imageView, boolean animate) {
    int location[] = new int[2];
    imageView.getLocationOnScreen(location);

    if (animate) {
      imgThemeUnderline.animate()
          .x(location[0])
          .setDuration(300)
          .setInterpolator(new OvershootInterpolator(0.75f))
          .start();
    } else {
      imgThemeUnderline.setX(location[0]);
    }
  }

  @Override protected void onDetachedFromWindow() {
    unbinder.unbind();

    if (subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
      subscriptions.clear();
    }

    super.onDetachedFromWindow();
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
